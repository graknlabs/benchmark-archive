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
      <sortby-selector
        title="Sort by"
        :items="columns"
        :defaultItem="{ text: 'Started At', value: 'executionStartedAt'}"
        @item-selected="onSortbySelection"
      />
      <el-radio-group value="Asc" size="mini" @change="onSortTypeSelection">
        <el-radio-button name="sort-type" label="Asc"></el-radio-button>
        <el-radio-button name="sort-type" label="Desc"></el-radio-button>
      </el-radio-group>
    </el-header>

    <div class="executions-list">
      <execution-card v-for="exec in executions" :key="exec.id" :execution="exec"/>
    </div>
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
      columns: [
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
      ]
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
      "{ executions { id " +
        this.columns.map(item => item.value).join(" ") +
        "} }"
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
        if (x === null) return 1;
        if (y === null) return -1;
        if (x === y) return 0;
        if (x < y) return -1;
        if (x > y) return 1;
      });
    },

    onSortTypeSelection() {
      this.executions.reverse();
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

.executions-list {
  margin-top: $height-topBar;
}

.el-header {
  height: $height-topBar;
  width: 100%;

  background-color: $color-default-bg;
  border-bottom: 1px solid $color-light-border;

  align-items: center;
  display: flex;
  margin-top: -$margin-default;
  margin-right: -$margin-default;
  margin-bottom: $margin-default;
  margin-left: -$margin-default;
  position: fixed;
  padding: $padding-default;
  z-index: 2;

  .el-radio-group {
    margin-left: $margin-default;
  }
}
</style>
