<template>
  <section
    v-loading="loading"
    class="content is-vertical"
  >
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

    <div class="header">
      <b-button-toolbar>
        <div class="toolbar-item">
          <sortby-selector
            title="Sort by"
            :items="columns"
            :default-item="{ text: 'Initialised At', value: 'executionInitialisedAt'}"
            @update:selected-item="sortExecutions"
          />
        </div>

        <div class="toolbar-item">
          <b-form-radio-group
            v-model="sortType"
            :options="sortOptions"
            size="sm"
            buttons
            button-variant="outline-primary"
            @change="onSortTypeSelection"
          />
        </div>
      </b-button-toolbar>
    </div>

    <div class="cards-list executions-list">
      <execution-card
        v-for="exec in executions"
        :key="exec.id"
        :execution="exec"
        :columns="columns"
        :click-path="'inspect/' + exec.id"
        @remove:execution="removeExecution"
        @stop:execution="stopExecution"
      />
    </div>
  </section>
</template>

<script>
import ExecutionCard from './components/ExecutionCard';
import BenchmarkClient from '@/util/BenchmarkClient';
import SortbySelector from '@/components/Selector.vue';

export default {
  name: 'ExecutionsPage',
  components: { SortbySelector, ExecutionCard },
  data() {
    return {
      loading: {
        show: true,
        fullscreen: true,
      },

      // popoverVisible: false,

      executions: [],

      sortColumn: 'executionInitialisedAt',

      sortType: 'desc',

      sortOptions: [
        { text: 'Asc', value: 'asc' },
        { text: 'Desc', value: 'desc' },
      ],

      columns: [
        {
          text: 'Commit',
          value: 'commit',
        },
        {
          text: 'Status',
          value: 'status',
        },
        {
          text: 'Initialised At',
          value: 'executionInitialisedAt',
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
      // newExecution: {
      //   commit: undefined,
      //   repoUrl: undefined
      // },
    };
  },

  async created() {
    await this.fetchExecutions();
  },

  methods: {
    async fetchExecutions() {
      const executionsResp = await BenchmarkClient.getExecutions(
        `{ executions (limit: 1000) { id vmName ${
          this.columns.map(item => item.value).join(' ')
        }} }`,
      );
      this.executions = executionsResp.data.executions;
      this.sortExecutions(this.sortColumn);
      this.loading.show = false;
    },

    sortExecutions(column) {
      this.sortColumn = column;
      const { sortType } = this;
      this.executions.sort((a, b) => {
        const x = a[column];
        const y = b[column];
        if (x === null) return 1;
        if (y === null) return -1;
        if (x === y) return 0;
        if (sortType === 'asc') return x < y ? -1 : 1;
        if (sortType === 'desc') return x < y ? 1 : -1;
        return false;
      });
    },

    onSortTypeSelection() {
      this.executions.reverse();
    },

    removeExecution(executionId) {
      this.executions = this.executions.filter(execution => execution.id !== executionId);
    },

    stopExecution(executionId) {
      const execIndex = this.executions.findIndex(execution => execution.id === executionId);
      this.executions[execIndex].status = 'STOPPED';
    },
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
  },
};
</script>

<style lang="scss" src="./style.scss"></style>


<style scoped lang="scss">
.executions-list {
  margin-top: $height-topBar;
}
</style>
