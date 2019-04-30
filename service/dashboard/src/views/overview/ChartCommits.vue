<template>
  <el-card
    v-loading="loading"
    class="box-card"
  >
    <div
      slot="header"
      class="clearfix"
    >
      <span>{{ graphName | formatTitle }}</span>
      <div class="actions">
        <scale-selector
          title="Scale"
          :items="scales"
          :defaultItem="scales[0]"
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
import QueriesUtil from './QueriesUtil';
import EChart from 'vue-echarts';
import 'echarts/lib/chart/line';
import 'echarts/lib/component/tooltip';
import 'echarts/lib/component/legend';
import 'echarts/lib/component/legendScroll';
import ScaleSelector from '@/components/Selector.vue';

export default {
  components: { EChart, ScaleSelector },

  filters: {
    formatTitle(graphName) {
      const presentableName = graphName
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1));
      return presentableName.join(' ');
    },
  },

  props: {
    graphName: {
      type: String,
      required: true,
    },

    executions: {
      type: Array,
      required: true,
    },

    executionSpans: {
      type: Array,
      required: true,
    },
  },

  data() {
    return {
      scales: [],
      legendsMap: [],
      chartOoptions: {},
      selectedScale: 0,
      querySpans: [],
      queries: [],
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
    this.scales = [
      ...new Set(this.executionSpans.map(span => span.tags.graphScale)),
    ].sort((a, b) => a - b).map(scale => ({ text: scale, value: scale }));

    this.selectedScale = this.scales[0].value;

    this.querySpans = await fetchQuerySpans(this.executionSpans);
    this.queries = uniqueQueriesSortedArray(this.querySpans);

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
      const currentQuery = Object.keys(this.legendsMap).filter(
        x => this.legendsMap[x] === args.seriesName,
      )[0];

      this.$router.push({
        path: `inspect/${args.data.executionId}?graph=${this.graphName}&scale=${this.selectedScale}&query=${currentQuery}`,
      });
    },

    async drawChart() {
      const dataAndSeries = QueriesUtil.buildQueriesTimes(
        this.queries,
        this.querySpans,
        this.executions,
        this.selectedScale,
      );

      this.legendsMap = QueriesUtil.buildQueriesMap(this.queries);

      const series = dataAndSeries.map(data => ({
        name: this.legendsMap[data.query],
        type: 'line',
        data: data.times.map(x => ({
          value: Number(x.avgTime).toFixed(3),
          symbolSize: Math.min(x.stdDeviation / 10, 45) + 5,
          symbol: 'circle',
          stdDeviation: x.stdDeviation,
          repetitions: x.repetitions,
          executionId: x.executionId,
        })),
        smooth: true,
        emphasis: { label: { show: false }, itemStyle: { color: 'yellow' } },
        showAllSymbol: true,
        tooltip: {
          formatter: args => `
                    query: ${args.seriesName}
                    <br> avgTime: ${Number(args.data.value).toFixed(3)} ms
                    <br> stdDeviation: ${Number(args.data.stdDeviation).toFixed(
          3,
        )} ms
                    <br> repetitions: ${args.data.repetitions}`,
        },
      }));

      const xData = dataAndSeries[0].times.map(x => ({
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
          data: Object.values(this.legendsMap).sort(),
          tooltip: {
            show: true,
            showDelay: 500,
            triggerOn: 'mousemove',
            formatter: args => Object.keys(this.legendsMap).filter(
              x => this.legendsMap[x] === args.name,
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

      this.loading = false;
    },
  },
};

/**
 * Helper functions
 */
function getQuerySpansRequest(id) {
  return BenchmarkClient.getSpans(
    `{ querySpans( parentId: "${id}" limit: 500){ id name duration tags { query type repetition repetitions }} }`,
  );
}

function uniqueQueriesSortedArray(querySpans) {
  return [...new Set(querySpans.map(span => span.tags.query))].sort();
}

async function fetchQuerySpans(executionSpans) {
  // eslint-disable-next-line
  const querySpanPromises = executionSpans.map(executionSpan => getQuerySpansRequest(executionSpan.id).then(resp => resp.data.querySpans.map(qs => Object.assign(
    {
      executionName: executionSpan.tags.executionName,
      scale: executionSpan.tags.graphScale,
    },
    qs,
  ))));
  const responses = await Promise.all(querySpanPromises);
  return responses.reduce((acc, resp) => acc.concat(resp), []);
}
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
