<template>
  <section>
    <div v-if="execution">
      <execution-card
        :execution="execution"
        :columns="executionColumns"
      />
    </div>

    <div v-if="graphs && querySpans">
      <graph-tabs
        :graphs="graphs"
        :query-spans="querySpans"
        :pre-selected-graph-type="preSelectedGraphType"
        :pre-selected-query="preSelectedQuery"
        :pre-selected-scale="preSelectedScale"
      />
    </div>
  </section>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import ExecutionCard from '@/views/Executions/components/ExecutionCard';
import GraphTabs from './components/GraphTabs';
import EDM from '@/util/ExecutionDataFormatters';

const { flattenGraphs, flattenQuerySpans } = EDM;

export default {
  name: 'Inspect',

  components: { GraphTabs, ExecutionCard },

  data() {
    return {
      executionId: this.$route.params.executionId,

      execution: null,

      graphs: null,

      querySpans: null,

      preSelectedGraphType: this.$route.query.graph,

      preSelectedQuery: this.$route.query.query,

      preSelectedScale: parseInt(this.$route.query.scale, 0),

      executionColumns: [
        {
          text: 'Status',
          value: 'status',
        },
        {
          text: 'Repository',
          value: 'repoUrl',
        },
        {
          text: 'Commit',
          value: 'commit',
        },
        {
          text: 'PR',
          value: 'prUrl',
        },
        {
          text: 'Started At',
          value: 'executionStartedAt',
        },
        {
          text: 'Completed At',
          value: 'executionCompletedAt',
        },
      ],
    };
  },

  async created() {
    this.fetchExecution();
    this.fetchQueries();
  },

  methods: {
    async fetchExecution() {
      const executionResp = await BenchmarkClient.getExecutions(
        `{ executionById (id: "${this.executionId}"){ id prNumber
          ${this.executionColumns.map(column => column.value).join(' ')}
        } }`,
      );

      this.execution = executionResp.data.executionById;
    },

    async fetchQueries() {
      const graphsResp = await BenchmarkClient.getSpans(
        `{ executionSpans( executionName: "${
          this.executionId
        }"){ id name duration tags { graphType executionName graphScale description }} }`,
      );
      const graphs = graphsResp.data.executionSpans;
      this.graphs = flattenGraphs(graphs);

      const queriesResponse = await Promise.all(
        graphs.map(graph => BenchmarkClient.getSpans(
          `{ querySpans( parentId: "${
            graph.id
          }" limit: 500){ id parentId name duration tags { query type repetition repetitions }} }`,
        )),
      );
      const querySpans = queriesResponse.map(resp => resp.data.querySpans);
      this.querySpans = flattenQuerySpans(querySpans);
    },
  },
};
</script>
