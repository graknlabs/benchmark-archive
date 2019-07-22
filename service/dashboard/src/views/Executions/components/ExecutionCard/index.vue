<template>
  <div
    class="card card-secondary execution-card"
    @click="onCardClick"
  >
    <div class="execution-details">
      <template v-if="shouldRenderColumn('status')">
        <b-badge
          :variant="getStatusVariant(execution.status)"
          class="status"
        >
          {{ execution.status }}
        </b-badge>
      </template>

      <template v-if="shouldRenderColumn('repoUrl')">
        <a
          v-b-tooltip.hover
          :href="execution.repoUrl"
          :title="getTooltipFor('repoUrl')"
        >{{ execution.repoUrl | substringRepo }}</a>
      </template>

      <template v-if="shouldRenderColumn('commit')">
        <a
          v-b-tooltip.hover
          :href="execution.repoUrl + '/commit/' + execution.commit"
          :title="getTooltipFor('commit')"
          target="_blank"
        >{{ execution.commit.slice(0, 15) }}</a>
      </template>

      <template v-if="shouldRenderColumn('prUrl')">
        <a
          v-b-tooltip.hover
          :title="getTooltipFor('prUrl')"
          :href="execution.prUrl"
          target="_blank"
        >#{{ execution.prNumber }}</a>
      </template>

      <template v-if="shouldRenderColumn('executionInitialisedAt')">
        <span
          v-b-tooltip.hover
          :title="getTooltipFor('executionInitialisedAt')"
        >{{ execution.executionInitialisedAt | parseDate }}</span>
      </template>

      <template v-if="shouldRenderColumn('executionStartedAt')">
        <span
          v-b-tooltip.hover
          :title="getTooltipFor('executionStartedAt')"
          style="width: 135px; text-align: center;"
        >{{ execution.executionStartedAt | parseDate }}</span>
      </template>

      <template v-if="shouldRenderColumn('executionCompletedAt')">
        <span
          v-b-tooltip.hover
          :title="getTooltipFor('executionCompletedAt')"
          style="width: 135px; text-align: center;"
        >{{ execution.executionCompletedAt | parseDate }}</span>
      </template>

      <b-dropdown
        text="Actions"
        variant="primary"
        size="sm"
        right
      >
        <b-dropdown-item
          :disabled="!isInProgress"
          @click="copyStringToClipboard(execution.vmName)"
        >
          GCloud command
        </b-dropdown-item>
        <b-dropdown-item
          :disabled="!isInProgress"
          @click="stopExecution(execution)"
        >
          Stop
        </b-dropdown-item>
        <b-dropdown-item @click="deleteExecution(execution)">
          Delete
        </b-dropdown-item>
      </b-dropdown>
    </div>
  </div>
</template>

<script>
import copy from 'copy-to-clipboard';
import BenchmarkClient from '@/util/BenchmarkClient';

export default {
  filters: {
    substringRepo(repoUrl) {
      if (!repoUrl) return '';
      return repoUrl.substring(19);
    },
  },

  props: {
    execution: {
      type: Object,
      required: true,
    },

    columns: {
      type: Array,
      required: true,
    },

    clickPath: {
      type: String,
      required: false,
      default: '#',
    },
  },

  computed: {
    isInProgress() {
      return (
        this.execution.status === 'INITIALISING'
        || this.execution.status === 'RUNNING'
      );
    },
  },

  methods: {
    onCardClick(e) {
      if (e.target.tagName !== 'A') { // the clicked element on the card is not a link
        this.$router.push({ path: this.clickPath });
      }
    },

    getStatusVariant(status) {
      const lcStatus = status.toLowerCase();
      if (lcStatus === 'initialising') { return 'warning'; }
      if (lcStatus === 'running') { return 'primary'; }
      if (lcStatus === 'completed') { return 'success'; }
      if (lcStatus === 'stopped') { return 'secondary'; }
      if (lcStatus === 'failed') { return 'danger'; }
      return 'light';
    },

    deleteExecution(execution) {
      this.$bvModal.msgBoxConfirm('Are you sure you want to delete this execution?').then(
        (isConfirmed) => {
          if (isConfirmed) {
            BenchmarkClient.deleteExecution(execution)
              .then(() => {
                this.$message({
                  showClose: true,
                  message: 'The execution was deleted successfully.',
                  type: 'success',
                });
                this.$emit('remove:execution', execution.id);
              })
              .catch(() => {
                this.$message({
                  showClose: true,
                  message:
                    'Deleting the execution failed. Check the console logs to find out why.',
                  type: 'error',
                });
              });
          }
        },
      );
    },

    stopExecution(execution) {
      this.$bvModal.msgBoxConfirm('Are you sure you want to stop this execution?').then(
        (isConfirmed) => {
          if (isConfirmed) {
            BenchmarkClient.stopExecution(execution)
              .then(() => {
                this.$message({
                  showClose: true,
                  message: 'The execution was stopped successfully.',
                  type: 'success',
                });
                this.$emit('stop:execution', execution.id);
              })
              .catch(() => {
                this.$message({
                  showClose: true,
                  message:
                  'Stoping the execution failed. Check the console logs to find out why.',
                  type: 'error',
                });
              });
          }
        },
      );
    },

    copyStringToClipboard(vmName) {
      copy(`gcloud compute ssh ubuntu@${vmName} --zone=us-east1-b`);
    },

    shouldRenderColumn(columnToRender) {
      return this.columns.some(column => column.value === columnToRender);
    },

    getTooltipFor(columnToTooltip) {
      return this.columns.filter(column => column.value === columnToTooltip)[0]
        .text;
    },
  },
};
</script>

<style lang="scss" src="./style.scss"></style>
