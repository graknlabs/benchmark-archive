<template>
  <el-card v-loading="loading">
    <div
      slot="header"
      class="clearfix"
    >
      <span>{{ graphType | formatTitle }}</span>
      <div class="actions">
        <scale-selector
          title="Scale"
          :items="scales.map(scale => ({ text: scale, value: scale }))"
          :default-item="{ text: scales[0], value: scales[0] }"
          @item-selected="onScaleSelection"
        />
      </div>
    </div>
    <e-chart
      :autoresize="true"
      :options="chartOoptions"
      @click="redirectToInspect"
    />
  </el-card>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import Util from './util';
import EChart from 'vue-echarts';
import 'echarts/lib/chart/line';
import 'echarts/lib/component/tooltip';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/legendScroll';
import 'echarts/lib/component/dataZoom';
import ScaleSelector from '@/components/Selector.vue';
import EDF from '@/util/ExecutionDataFormatters';

const { getLegendsData, getChartData } = Util;
const { addExecutionIdAndScaleToQuerySpan, flattenQuerySpans } = EDF;

export default {
  components: { EChart, ScaleSelector },

  filters: {
    formatTitle(graphType) {
      const presentableName = graphType
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1));
      return presentableName.join(' ');
    },
  },

  props: {
    graphType: {
      type: String,
      required: true,
    },

    executions: {
      type: Array,
      required: true,
    },

    graphs: {
      type: Array,
      required: true,
    },
  },

  data() {
    return {
      scales: [],

      legendsData: [],

      chartOoptions: {},

      selectedScale: 0,

      queries: [],

      querySpans: [],

      loading: true,
    };
  },

  watch: {
    selectedScale(val, previous) {
      if (val === previous) return;
      this.drawChart();
    },
  },

  async created() {
    // get the set of scales to populaste the scale selector of the chart
    this.scales = [...new Set(this.graphs.map(graph => graph.scale))].sort(
      (a, b) => a - b,
    );
    this.selectedScale = this.scales[0];

    // fetch querySpans to use in processing the chart's data
    const querySpansResp = await Promise.all(
      this.graphs.map(graph => BenchmarkClient.getSpans(
        `{ querySpans( parentId: "${
          graph.id
        }" limit: 500){ id parentId name duration tags { query type repetition repetitions }} }`,
      )),
    );
    const querySpans = querySpansResp.map(resp => resp.data.querySpans);
    this.querySpans = flattenQuerySpans(querySpans);

    // extract the query values to use in processing cahrt's data and legends
    this.queries = [...new Set(this.querySpans.map(query => query.value))];

    this.$nextTick(() => {
      this.drawChart();
    });
  },

  methods: {
    onScaleSelection(scale) {
      this.selectedScale = scale;
      this.loading = true;
    },

    redirectToInspect(args) {
      const currentQuery = Object.keys(this.legendsData).filter(
        x => this.legendsData[x] === args.seriesName,
      )[0];

      this.$router.push({
        path: `inspect/${args.data.executionId}?graph=${this.graphType}&scale=${
          this.selectedScale
        }&query=${currentQuery}`,
      });
    },

    async drawChart() {
      const querySpansWithExecutionAndScale = addExecutionIdAndScaleToQuerySpan(
        this.graphs,
        this.querySpans,
      );

      const chartData = getChartData(
        this.queries,
        querySpansWithExecutionAndScale,
        this.executions,
        this.selectedScale,
      );

      this.legendsData = getLegendsData(this.queries);

      const series = chartData.map((data) => {
        const maxStdDeviation = 45;

        return {
          name: this.legendsData[data.query],
          type: 'line',
          data: data.times.map(dataItem => ({
            value: Number(dataItem.avgTime).toFixed(3),
            symbolSize: Math.min(dataItem.stdDeviation / 10, maxStdDeviation) + 5,
            symbol: 'circle',
            stdDeviation: dataItem.stdDeviation,
            repetitions: dataItem.repetitions,
            executionId: dataItem.executionId,
            itemStyle: {
              // draw the chart node with a different color when its standard deviation exceeds the given maximum amount
              color: dataItem.stdDeviation / 10 > maxStdDeviation ? '#666' : null,
            },
          })),
          smooth: true,
          emphasis: { label: { show: false }, itemStyle: { color: 'yellow' } },
          showAllSymbol: true,
          tooltip: {
            formatter: args => `
                    query: ${args.seriesName}
                    <br> avgTime: ${Number(args.data.value).toFixed(3)} ms
                    <br> stdDeviation: ${Number(args.data.stdDeviation).toFixed(3)} ms
                    <br> repetitions: ${args.data.repetitions}`,
          },
        };
      });

      if (chartData.length) {
        const xData = chartData[0].times.map(x => ({
          value: x.commit.substring(0, 15),
          commit: x.commit,
        }));

        this.chartOoptions = {
          tooltip: {
            show: true,
            trigger: 'item',
          },
          legend: {
            type: 'scroll',
            orient: 'horizontal',
            left: 10,
            bottom: 0,
            data: Object.values(this.legendsData).sort(),
            tooltip: {
              show: true,
              showDelay: 500,
              triggerOn: 'mousemove',
              formatter: args => Object.keys(this.legendsData).filter(
                x => this.legendsData[x] === args.name,
              ),
            },
          },
          calculable: true,
          xAxis: [
            {
              type: 'category',
              boundaryGap: false,
              data: xData,
              triggerEvent: true,
            },
          ],
          yAxis: [
            {
              type: 'value',
              axisLabel: {
                formatter: '{value} ms',
              },
            },
          ],
          series,
          dataZoom: [
            {
              type: 'inside',
              zoomOnMouseWheel: 'ctrl',
              filterMode: 'none',
              orient: 'vertical',
            },
          ],
          grid: {
            left: 70,
            top: 20,
            right: 70,
            bottom: 70,
          },
        };
      }
      this.loading = false;
    },
  },
};
</script>

<style lang="scss" scoped>
.actions {
  float: right;
}

.echarts {
  width: 100%;
  height: 500px;
}
</style>
