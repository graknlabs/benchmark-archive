<template>
  <el-container class="is-vertical overview-section">
    <el-row
      v-for="graphType in graphTypes"
      :key="graphType"
      class="panel"
    >
      <commits-chart
        :graph-type="graphType"
        :executions="completedExecutions"
        :graphs="filterGraphs(graphType)"
      />
    </el-row>
  </el-container>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import CommitsChart from './ChartCommits.vue';
import ExecutionDataFormatters from '@/util/ExecutionDataFormatters';

export default {
  name: 'Overview',

  components: { CommitsChart },

  data() {
    return {
      numberOfCompletedExecutions: 8,

      completedExecutions: null,

      graphTypes: [],

      graphs: null,
    };
  },

  async created() {
    await this.fetchGraphs();
    this.graphTypes = [...new Set(this.graphs.map(graph => graph.type))];
  },

  methods: {
    // fetch the last n executions for which, we'd like to populate the charts
    async fetchGraphs() {
      this.completedExecutions = (await BenchmarkClient.getLatestCompletedExecutions(
        this.numberOfCompletedExecutions,
      )).reverse();

      const graphs = await BenchmarkClient.getExecutionsSpans(
        this.completedExecutions,
      );

      this.graphs = ExecutionDataFormatters.flattenGraphs(graphs);
    },

    filterGraphs(graphType) {
      return this.graphs.filter(graph => graph.type === graphType);
    },
  },
};
</script>

<style lang="scss" scoped>
@import "./src/assets/css/variables.scss";

.el-container {
  min-height: 100%;
}

.panel {
  margin-bottom: $margin-default;

  &:last-child {
    margin-bottom: 0;
  }
}
</style>
