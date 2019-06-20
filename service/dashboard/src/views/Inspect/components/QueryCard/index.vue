<template>
  <div class="queryCard card">
    <el-card v-loading="loading">
      <div
        class="queryCardDetails flexed"
        @click="toggleStepsTable()"
      >
        <el-tooltip
          class="item"
          effect="dark"
          :content="query"
          placement="top"
        >
          <div
            class="queryCardDetail"
            style="padding: 0;"
          >
            <p>
              {{ query | truncate(100) }}
            </p>
          </div>
        </el-tooltip>

        <el-tooltip
          class="item"
          effect="dark"
          content="Outliers"
          placement="top"
        >
          <div class="queryCardDetail">
            <el-row
              v-for="span in outlierSpans"
              :key="span.rep"
            >
              Rep {{ span.rep + 1 }}: {{ span.duration | fixedMs }} ms
            </el-row>
          </div>
        </el-tooltip>

        <e-chart
          class="queryRepChart"
          :autoresize="true"
          :options="queryCardChartOptions"
        />

        <el-tooltip
          class="item"
          effect="dark"
          content="Reps"
          placement="top"
        >
          <div class="queryCardDetail">
            <p>
              {{ reps }}
            </p>
          </div>
        </el-tooltip>

        <el-tooltip
          class="item"
          effect="dark"
          content="Median"
          placement="top"
        >
          <div class="queryCardDetail">
            <p>
              {{ median | fixedMs }}
            </p>
          </div>
        </el-tooltip>

        <el-tooltip
          class="item"
          effect="dark"
          content="Mean"
          placement="top"
        >
          <div class="queryCardDetail">
            <p>
              {{ mean | fixedMs }}
            </p>
          </div>
        </el-tooltip>

        <el-tooltip
          class="item"
          effect="dark"
          content="Standard Deviation"
          placement="top"
        >
          <div class="queryCardDetail">
            <p>
              {{ stdDeviation | fixedMs }}
            </p>
          </div>
        </el-tooltip>
      </div>

      <section
        v-if="queryExpanded"
        class="stepsTable"
      >
        <div class="flexed">
          <p class="tableHeader">
            Step
          </p>
          <p class="tableHeader">
            Min/Rep|Member
          </p>
          <p class="tableHeader">
            Median/Reps
          </p>
          <p class="tableHeader">
            Max/Rep
          </p>
        </div>
        <template v-for="stepOrGroup in stepsAndGroups">
          <group-line
            v-if="stepOrGroup.hasOwnProperty('members')"
            :key="stepOrGroup.name"
            :members="stepOrGroup.members"
            :padding="20"
          />

          <step-line
            v-if="!stepOrGroup.hasOwnProperty('members')"
            :key="stepOrGroup.name"
            :step="stepOrGroup.name"
            :step-spans="filterStepSpans(stepOrGroup.order)"
            :padding="20"
          />
        </template>
      </section>
    </el-card>
  </div>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import StepLine from '../StepLine';
import GroupLine from '../GroupLine';
import EChart from 'vue-echarts';
import 'echarts/lib/chart/bar';
import 'echarts/lib/component/tooltip';
import EDF from '@/util/ExecutionDataFormatters';
import util from './util';
import math from '@/util/math';

const { getMedian, getOutliers } = math;

const { getQueryCardChartOptions } = util;

const { flattenStepSpans, attachRepsToChildSpans } = EDF;

export default {
  components: { EChart, StepLine, GroupLine },

  filters: {
    fixedMs(num) {
      return `${Number(num / 1000).toFixed(3)}`;
    },
  },

  props: {
    query: {
      type: String,
      required: true,
    },

    querySpans: {
      type: Array,
      required: true,
    },

    expanded: {
      type: Boolean,
      required: true,
    },
  },

  data() {
    return {
      loading: false,

      stepSpans: null,

      stepsAndGroups: [],

      queryExpanded: this.expanded,

      queryCardChartOptions: {},
    };
  },

  computed: {
    spanOfFirstRep() {
      return this.querySpans.filter(span => span.rep === 0)[0];
    },

    spansSortedByDuration() {
      const spansSortedByDuration = this.querySpans;
      return spansSortedByDuration.sort((a, b) => (a.duration > b.duration ? 1 : -1));
    },

    outlierSpans() {
      const outliers = getOutliers(this.querySpans.map(span => span.duration)).upper;
      return this.querySpans.filter(span => outliers.includes(span.duration));
    },

    histogramSpans() {
      return this.querySpans
        .filter(duration => !this.outlierSpans.includes(duration))
        .sort((a, b) => (a.duration > b.duration ? 1 : -1));
    },

    median() {
      const durations = this.spansSortedByDuration.map(span => span.duration);
      return getMedian(durations).value;
    },

    mean() {
      return this.querySpans.map(span => span.duration).reduce((a, b) => a + b, 0) / this.querySpans.length;
    },

    stdDeviation() {
      const sum = this.querySpans.map(span => (span.duration - this.mean) ** 2).reduce((a, b) => a + b, 0);
      return Math.sqrt(sum / this.querySpans.length);
    },

    reps() {
      return this.querySpans.length;
    },
  },

  created() {
    if (this.expanded) {
      this.fetchStepSpans();
    }

    this.queryCardChartOptions = getQueryCardChartOptions(this.histogramSpans);
  },

  methods: {
    async toggleStepsTable() {
      this.loading = true;

      this.queryExpanded = !this.queryExpanded;
      if (!this.queryExpanded) {
        this.loading = false;
        return;
      }

      if (!this.stepsAndGroups.length) {
        await this.fetchStepSpans();
      }

      this.loading = false;
    },

    async fetchStepSpans() {
      const querySpanIds = this.querySpans
        .map(querySpan => `"${querySpan.id}"`)
        .join();

      const stepSpansResp = await BenchmarkClient.getSpans(
        `{ childrenSpans( parentId: [
          ${querySpanIds}
          ] limit: 10000){ id name duration parentId timestamp tags { childNumber }} }`,
      );
      let stepSpans = stepSpansResp.data.childrenSpans;
      stepSpans = flattenStepSpans(stepSpans);
      this.stepSpans = attachRepsToChildSpans(stepSpans, this.querySpans);

      this.produceStepsAndGroups();
    },

    /**
     * iterates over fetched stepSpans to find out which spans should belong to a "group"
     * the group object has one key i.e. 'members'
     * 'members' is an object where:
     *    - keys are the distinct 'order' of its members
     *    - values are array of span objects
     */
    produceStepsAndGroups() {
      const steps = this.stepSpans.filter(span => span.rep === 0);
      steps.sort((a, b) => a.order - b.order);

      let currentStep = steps[0];
      let currentSteps = [];
      let i = 0;
      // debugger;

      do {
        if (steps[i].name === currentStep.name) {
          currentSteps.push(steps[i]);
          i += 1;
        } else {
          const stepOrGroup = this.buildStepOrGroup(currentSteps);
          this.stepsAndGroups.push(stepOrGroup);
          currentStep = steps[i];
          currentSteps = [];
        }
      } while (i < steps.length);

      // last step is not a group. insert it.
      this.stepsAndGroups.push(steps[steps.length - 1]);
    },

    buildStepOrGroup(grouppedSteps) {
      if (grouppedSteps.length > 1) {
        const group = { members: {} };
        grouppedSteps.forEach((grouppedStep) => {
          group.members[grouppedStep.order] = this.filterStepSpans(
            grouppedStep.order,
          );
        });
        return group;
      }
      return grouppedSteps[0];
    },

    filterStepSpans(stepNumber) {
      return this.stepSpans.filter(stepSpan => stepSpan.order === stepNumber);
    },
  },
};
</script>

<style lang="scss">
.queryCard {
  .el-card__body {
    padding: 0;
  }
}
</style>

<style lang="scss" scoped>
@import "./src/assets/css/variables.scss";

.queryRepChart {
  height: 130px;
  width: 300px;
}

.queryCardDetails {
  cursor: pointer;

  padding: $padding-default;
}

.queryCardDetail {
  font-size: 16px;

  &:nth-child(1) {
    width: 300px;
    font-size: 14px;
    text-align: left;
  }

  .el-row {
    padding-top: 10px;

    &:nth-child(1) {
      padding-top: 0;
    }
  }
}

.tableHeader {
  text-align: center;

  &:nth-child(1) {
    width: 300px;
    text-align: left;
    box-sizing: border-box;
    padding-left: $padding-default;
  }

  &:nth-child(2) {
    width: 100px;
  }

  &:nth-child(3) {
    width: 100px;
  }

  &:nth-child(4) {
    width: 100px;
    box-sizing: border-box;
    padding-right: $padding-default;
  }
}

.tableHeader {
  padding: $padding-less 0;

  color: $color-text-gray;
  font-size: $font-size-table-header;
  font-weight: 600;
}

.stepsTable {
  background-color: #fafafa;

  span {
    text-align: center;
  }
}

.spans {
  margin-top: $margin-default;
}
</style>
