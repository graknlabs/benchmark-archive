<template>
  <section v-loading="loading" class="el-container is-vertical page-container">
    <!-- <el-row>
        <el-popover
          v-model="popoverVisible"
          placement="right-start"
          title="Create Execution"
          width="200"
          trigger="manual"
        >
          <el-row>
            Repo URL: <el-input v-model="newExecution.repoUrl" />
          </el-row>
          <el-row>
            Commit: <el-input v-model="newExecution.commit" />
          </el-row>
          <div style="text-align: right; margin: 0">
            <el-button
              size="mini"
              type="text"
              @click="popoverVisible = false"
            >
              cancel
            </el-button>
            <el-button
              type="primary"
              size="mini"
              @click="triggerExecution"
            >
              send
            </el-button>
          </div>
          <el-button
            slot="reference"
            type="success"
            circle
            icon="el-icon-plus"
            @click="popoverVisible = !popoverVisible"
          />
        </el-popover>
        <el-button type="success" circle icon="el-icon-plus"></el-button>
    </el-row>-->

    <el-header>
      <sortby-selector title="Sort by" :items="columns" :defaultItem="{ text: 'Started At', value: 'executionStartedAt'}" @item-selected="onSortbySelection"/>
    </el-header>

    <execution-card v-for="exec in executions" :key="exec.id" :execution="exec"/>
  </section>
</template>

<script>
import ExecutionCard from "./ExecutionCard.vue";
import BenchmarkClient from "@/util/BenchmarkClient";
import SortbySelector from "@/components/Selector.vue";

export default {
  name: "ExecutionsPage",
  components: { SortbySelector, ExecutionCard },
  data() {
    return {
      loading: true,
      // popoverVisible: false,
      executions: [],
      columns: []
      // newExecution: {
      //   commit: undefined,
      //   repoUrl: undefined
      // },
    };
  },

  created() {
    this.columns = [
      {
        text: "Commit",
        value: "commit"
      },
      {
        text: "Status",
        value: "status"
      },
      {
        text: "Initialised At",
        value: "executionInitialisedAt"
      },
      {
        text: "Started At",
        value: "executionStartedAt"
      },
      {
        text: "Completed At",
        value: "executionCompletedAt"
      }
    ];

    BenchmarkClient.getExecutions(
      "{ executions { id " + this.columns.map(item => item.value).join(" ") + "} }"
    ).then(execs => {
      this.executions = execs.data.executions;
      this.loading = false;
    });
  },

  methods: {
    onSortbySelection(column) {
      this.executions.sort(function(a, b) {
        var x = a[column];
        var y = b[column];
        return x < y ? -1 : x > y ? 1 : 0;
      });
    }
    // triggerExecution() {
    //   BenchmarkClient.triggerExecution(this.newExecution)
    //     .then(() => {
    //       this.$notify({
    //         title: "Success",
    //         message: "New Execution triggered successfully!",
    //         type: "success"
    //       });
    //     })
    //     .catch(() => {
    //       this.$notify.error({
    //         title: "Error",
    //         message: "It was not possible to trigger new Execution."
    //       });
    //     });
    //   this.newExecution.commit = undefined;
    // }
  }
};
</script>

<style scoped lang="scss">
@import "./src/assets/css/variables.scss";

section {
  min-height: 100%;
}

.cards-row {
  margin-bottom: 20px;
}

.el-header {
  border-bottom: 1px solid $color-light-border;
  padding: $padding-default;
  margin-top: -$margin-default;
  margin-right: -$margin-default;
  margin-bottom: $margin-default;
  margin-left: -$margin-default;
}
</style>
