<template>
  <!-- <b-container fluid> -->
  <div
    ref="expanded"
    :class="'query-card ' + (queryExpanded ? 'expanded' : '')"
  >
    <div
      v-loading="loading"
      class="query-summary"
      @click="toggleStepsTable()"
    >
      <div
        :id="query"
        :title="query"
        class="query-graql"
      >
        <p>
          {{ query | truncate(180) }}
        </p>
      </div>

      <b-tooltip
        v-if="query.length > 180"
        custom="query-tooltip"
        :target="query"
        :title="query"
      />

      <div class="query-chart-details-wrapper">
        <div class="query-chart">
          <div class="histogram-chart-wrapper">
            <e-chart
              class="histogram-chart"
              :autoresize="true"
              :options="queryCardChartOptions"
            />
            <div class="outliers">
              <header>
                <p>Outliers</p>
              </header>
              <p
                v-for="span in outlierSpans"
                :key="span.rep"
              >
                Rep {{ span.rep + 1 }}: {{ span.duration | fixedMs }} ms
              </p>
            </div>
          </div>
        </div>
        <div class="query-details">
          <b-row
            v-for="(row, rowIindex) in queryDetails"
            :key="rowIindex"
            no-gutters
          >
            <b-col
              v-for="detail in row"
              :key="detail.label"
              :cols="12/row.length"
            >
              <p>
                <span class="label">{{ detail.label }}</span>
                <span class="value">{{ detail.value }}</span>
              </p>
            </b-col>
          </b-row>
        </div>
      </div>
    </div>

    <div
      v-if="queryExpanded && stepSpans"
      class="query-expanded-section"
    >
      <steps-table
        v-if="queryExpanded"
        :steps-and-groups="stepsAndGroups"
        :step-spans="stepSpans"
        :max-height="expandedSummaryHeight"
      />
    </div>
  </div>
  <!-- </b-container> -->
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import StepsTable from '../StepsTable';
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
  components: { EChart, StepsTable },

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

      stepSpans: [],

      stepsAndGroups: [],

      queryExpanded: this.expanded,

      expandedSummaryHeight: 0,
    };
  },

  computed: {
    queryDetails() {
      return [
        [
          // row 1
          {
            label: 'REP',
            value: this.reps,
          },
          {
            label: 'AVG',
            value: `${this.fixedMs(this.mean)} ms`,
          },
        ],
        [
          // row 2
          {
            label: 'SD',
            value: `${this.fixedMs(this.stdDeviation)} ms`,
          },
          {
            label: 'MED',
            value: `${this.fixedMs(this.median)} ms`,
          },
        ],
      ];
    },

    queryCardChartOptions() {
      return getQueryCardChartOptions(this.histogramSpans);
    },

    spanOfFirstRep() {
      return this.querySpans.filter(span => span.rep === 0)[0];
    },

    spansSortedByDuration() {
      const spansSortedByDuration = this.querySpans;
      return spansSortedByDuration.sort((a, b) => (a.duration > b.duration ? 1 : -1));
    },

    outlierSpans() {
      const outliers = getOutliers(this.querySpans.map(span => span.duration))
        .upper;
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
      return (
        this.querySpans.map(span => span.duration).reduce((a, b) => a + b, 0)
        / this.querySpans.length
      );
    },

    stdDeviation() {
      const sum = this.querySpans
        .map(span => (span.duration - this.mean) ** 2)
        .reduce((a, b) => a + b, 0);
      return Math.sqrt(sum / this.querySpans.length);
    },

    reps() {
      return this.querySpans.length;
    },
  },

  watch: {
    queryExpanded() {
      this.$nextTick(() => {
        if (this.queryExpanded && this.$refs.expanded) {
          this.expandedSummaryHeight = this.$refs.expanded.offsetHeight;
        }
      });
    },
  },

  created() {
    if (this.expanded) {
      this.fetchStepSpans();
    }
  },

  methods: {
    fixedMs(num) {
      return `${Number(num / 1000).toFixed(3)}`;
    },

    async toggleStepsTable() {
      this.loading = true;

      if (!this.stepsAndGroups.length) {
        await this.fetchStepSpans();
      }

      this.queryExpanded = !this.queryExpanded;

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

<style lang="scss" scoped src="./style.scss"></style>
