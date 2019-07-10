<template>
  <b-container fluid>
    <b-row
      v-for="graphType in graphTypes"
      :key="graphType"
      class="overview-graph"
      no-gutters
    >
      <commits-chart
        :graph-type="graphType"
        :executions="completedExecutions"
        :graphs="filterGraphs(graphType)"
      />
    </b-row>
  </b-container>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import CommitsChart from './components/ChartCommits';
import EDF from '@/util/ExecutionDataFormatters';

const { flattenGraphs } = EDF;

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

      this.graphs = flattenGraphs(graphs);
    },

    filterGraphs(graphType) {
      return this.graphs.filter(graph => graph.type === graphType);
    },
  },
};
</script>

<style lang="scss" scopped src="./style.scss"></style>
