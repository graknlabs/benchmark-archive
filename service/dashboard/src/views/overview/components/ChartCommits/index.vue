<template>
  <el-card v-loading="loading">
    <div
      slot="header"
      class="clearfix"
    >
      <el-row
        type="flex"
        justify="space-between"
        align="middle"
      >
        <span>{{ graphType | formatTitle }}</span>
        <el-row
          type="flex"
          justify="end"
          align="middle"
        >
          <el-checkbox-group
            v-model="chartSelectedQueryTypes"
            class="flexItem"
            size="mini"
            @change="toggleChartQueryType"
          >
            <el-checkbox-button
              v-for="queryType in chartQueryTypes"
              :key="queryType"
              :label="queryType"
            >
              {{ queryType }}
            </el-checkbox-button>
          </el-checkbox-group>
          <scale-selector
            title="Scale"
            :items="scales.map(scale => ({ text: scale, value: scale }))"
            :default-item="{ text: scales[0], value: scales[0] }"
            class="flexItem"
            @item-selected="onScaleSelection"
          />
        </el-row>
      </el-row>
      <e-chart
        ref="commitsChart"
        :autoresize="true"
        :options="chartOoptions"
        @click="redirectToInspect"
      />
    </div>
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

const { getCommitsChartOptions, getLegendsData } = Util;
const { flattenQuerySpans } = EDF;

export default {
  /* eslint-disable guard-for-in */
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

      commitsChart: null,

      chartSelectedQueryTypes: ['Match', 'Insert', 'MatchInsert', 'Compute'],

      chartQueryTypes: ['Match', 'Insert', 'MatchInsert', 'Compute'],
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

  mounted() {
    this.commitsChart = this.$refs.commitsChart;
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
      this.legendsData = getLegendsData(this.queries);
      this.chartOoptions = await getCommitsChartOptions(this.graphs, this.querySpans, this.queries, this.executions, this.selectedScale);
      this.loading = false;
    },

    toggleChartQueryType(selectedQueryTypes) {
      if (!selectedQueryTypes.length) return;
      const legends = Object.values(this.legendsData);
      const selectedLegendQueryTypes = selectedQueryTypes.map(type => `${type.charAt(0).toLowerCase() + type.slice(1)}Query`);

      legends.forEach((legend) => {
        const legendQueryType = legend.replace(/[0-9]/g, '');
        if (selectedLegendQueryTypes.includes(legendQueryType)) {
          this.commitsChart.dispatchAction({
            type: 'legendSelect',
            name: legend,
          });
        } else {
          this.commitsChart.dispatchAction({
            type: 'legendUnSelect',
            name: legend,
          });
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
@import "./src/assets/css/variables.scss";

.flexItem {
  margin-right: $margin-default;

  &:last-child {
    margin: 0;
  }
}

.echarts {
  width: 100%;
  height: 500px;
}
</style>
