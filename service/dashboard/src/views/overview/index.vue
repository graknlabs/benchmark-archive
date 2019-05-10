<template>
  <el-container
    v-loading="loading"
    class="is-vertical overview-section"
  >
    <el-row
      v-for="graphType in graphTypes"
      :key="graphType"
      class="panel"
    >
      <commits-chart
        :graph-type="graphType"
        :executions="completedExecutions"
        :graphs="filterGraphs(graphType)"
        :query-spans="filterQueries(graphType)"
      />
    </el-row>
  </el-container>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import CommitsChart from './ChartCommits.vue';
import ExecutionDataModifiers from '@/util/ExecutionDataModifiers';

export default {
  name: 'OverviewPage',

  components: { CommitsChart },

  data() {
    return {
      numberOfCompletedExecutions: 8,

      completedExecutions: null,

      graphTypes: [],

      graphs: null,

      queries: null,

      loading: true,
    };
  },

  async created() {
    await this.fetchData();
    this.graphTypes = [...new Set(this.graphs.map(graph => graph.type))];
    this.loading = false;
  },

  methods: {
    async fetchData() {
      this.completedExecutions = (await BenchmarkClient.getLatestCompletedExecutions(
        this.numberOfCompletedExecutions,
      )).reverse();

      const graphs = await BenchmarkClient.getExecutionsSpans(
        this.completedExecutions,
      );
      this.graphs = ExecutionDataModifiers.flattenGraphs(graphs);

      const queriesResponse = await Promise.all(
        this.graphs.map(graph => BenchmarkClient.getSpans(
          `{ querySpans( parentId: "${
            graph.id
          }" limit: 500){ id parentId name duration tags { query type repetition repetitions }} }`,
        )),
      );
      const queries = queriesResponse.map(resp => resp.data.querySpans);
      this.queries = ExecutionDataModifiers.flattenQuerySpans(queries);
    },

    filterGraphs(graphType) {
      return this.graphs.filter(graph => graph.type === graphType);
    },

    filterQueries(graphType) {
      const queries = [];
      const graphsOfInterest = this.filterGraphs(graphType);
      graphsOfInterest.forEach((graph) => {
        this.queries
          .filter(query => query.parentId === graph.id)
          .forEach((query) => {
            queries.push(query);
          });
      });

      return queries;
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
