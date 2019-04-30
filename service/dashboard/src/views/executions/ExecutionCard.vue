<template>
  <el-card>
    <el-row>
      <el-col :span="2">
        <div :class="'status status-' + execution.status.toLowerCase()">
          {{ execution.status }}
        </div>
      </el-col>
      <el-col :span="5">
        <router-link :to="'inspect/' + execution.id">
          {{ execution.id.slice(0, 15) }}
        </router-link>
      </el-col>
      <el-tooltip
        class="item"
        effect="dark"
        content="Initialised at"
        placement="top"
      >
        <el-col :span="5">
          {{ execution.executionInitialisedAt | parseDate }}
        </el-col>
      </el-tooltip>
      <el-tooltip
        class="item"
        effect="dark"
        content="Started at"
        placement="top"
      >
        <el-col :span="5">
          {{ execution.executionStartedAt | parseDate }}
        </el-col>
      </el-tooltip>
      <el-tooltip
        class="item"
        effect="dark"
        content="Completed at"
        placement="top"
      >
        <el-col :span="5">
          {{ execution.executionCompletedAt | parseDate }}
        </el-col>
      </el-tooltip>
      <el-col
        :span="3"
        class="actions"
      >
        <el-dropdown>
          <span class="el-dropdown-link">
            Actions<i class="el-icon-arrow-down el-icon--right" />
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item
              :disabled="!isInProgress"
              @click.native="copyStringToClipboard(execution.vmName)"
            >
              GCloud command
            </el-dropdown-item>
            <el-dropdown-item
              :disabled="!isInProgress"
              @click.native="stopExecution(execution)"
            >
              Stop
            </el-dropdown-item>
            <el-dropdown-item @click.native="deleteExecution(execution)">
              Delete
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import copy from 'copy-to-clipboard';

export default {
  props: {
    execution: Object,
  },
  computed: {
    isInProgress(status) {
      return status === 'INITIALISING' || status === 'RUNNING';
    },
  },
  methods: {
    deleteExecution(execution) {
      this.$confirm('Are you sure you want to delete this execution?')
        .then(() => {
          BenchmarkClient.deleteExecution(execution)
            .then(() => {
              console.log('execution deleted.');
              this.$message({ showClose: true, message: 'The execution was deleted successfully.', type: 'success' });
            })
            .catch((e) => {
              console.log(e);
              this.$message({ showClose: true, message: 'Deleting the execution failed. Check the console logs to find out why.', type: 'error' });
            });
        });
    },
    stopExecution(execution) {
      this.$confirm('Are you sure you want to stop this execution?')
        .then(() => {
          BenchmarkClient.stopExecution(execution)
            .then(() => {
              console.log('execution stopped.');
              this.$message({ showClose: true, message: 'The execution was stopped successfully.', type: 'success' });
            })
            .catch((e) => {
              console.log(e);
              this.$message({ showClose: true, message: 'Stoping the execution failed. Check the console logs to find out why.', type: 'error' });
            });
        });
    },
    copyStringToClipboard(vmName) {
      copy(`gcloud compute ssh ubuntu@${vmName} --zone=us-east1-b`);
    },
  },
};
</script>

<style lang="scss" scoped>
@import "./src/assets/css/variables.scss";

.el-row {
  display: flex;
  text-align: center;
  align-items: center;
}

.el-card {
  margin-bottom: $margin-default;
  overflow: visible;
  font-size: $font-size-card;

  &:last-child { margin-bottom: 0; }
}

.el-col.actions {
  position: absolute;
  right: 0;
  text-align: right;
}

.status {
  width: fit-content;
  border-radius: 10px;
  font-size: 12px;
  padding: 3px 8px;
  line-height: 14px;
  color: #fff;
}

.status-initialising {
  background-color: #f39c12;
}
.status-running {
  background-color: #2980b9;
}
.status-completed {
  background-color: #27ae60;
}
.status-stopped {
  background-color: #2c3e50;
}
.status-failed {
  background-color: #c0392b;
}
</style>
