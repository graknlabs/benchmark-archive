<template>
  <section>
    <div v-if="execution">
      <execution-card class="execution-card" :execution="execution" :columns="executionColumns"/>
    </div>

    <tabular-view
      :graph-names="graphNames"
      :spans="spans"
      :pre-selected-graph-name="preSelectedGraphName"
      :pre-selected-query="preSelectedQuery"
      :pre-selected-scale="preSelectedScale"
    />
  </section>
</template>

<script>
import BenchmarkClient from "@/util/BenchmarkClient";
import ExecutionCard from "@/views/executions/ExecutionCard.vue";
import TabularView from "./TabularView/TabularView.vue";

export default {
  components: { TabularView, ExecutionCard },

  data() {
    return {
      executionId: this.$route.params.executionId,

      execution: null,

      spans: [],

      graphNames: [],

      preSelectedGraphName: this.$route.query.graph,

      preSelectedQuery: this.$route.query.query,

      preSelectedScale: parseInt(this.$route.query.scale, 0),

      executionColumns: [
        {
          text: "Status",
          value: "status"
        },
        {
          text: "Repository",
          value: "repoUrl"
        },
        {
          text: "Commit",
          value: "commit"
        },
        {
          text: "PR",
          value: "prUrl"
        },
        {
          text: "Started At",
          value: "executionStartedAt"
        },
        {
          text: "Completed At",
          value: "executionCompletedAt"
        }
      ]
    };
  },

  created() {
    this.fetchExecution();
    this.fetchSpans();
  },

  methods: {
    fetchExecution() {
      BenchmarkClient.getExecutions(
        `{ executionById (id: "${
          this.executionId
        }"){ id prNumber
          ${this.executionColumns.map(column => column.value).join(" ")}
        } }`
      ).then(resp => {
        this.execution = resp.data.executionById;
      });
    },

    fetchSpans() {
      BenchmarkClient.getSpans(
        `{ executionSpans( executionName: "${
          this.executionId
        }"){ id name duration tags { graphType executionName graphScale }} }`
      ).then(resp => {
        this.spans = resp.data.executionSpans;

        this.graphNames = Array.from(
          new Set(this.spans.map(span => span.tags.graphType))
        );
      });
    }
  }
};
</script>

<style scoped lang="scss">
@import "./src/assets/css/variables.scss";

.el-container {
  background-color: #f4f3ef;
}
h2 {
  margin-bottom: 10px;
}
.query-select {
  min-width: 400px;
}
.scale-select {
  max-width: 100px;
}
</style>
