<template>
  <!-- <b-container fluid> -->
  <div
    :ref="queryExpanded ? 'expanded' : ''"
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
                Rep {{ span.rep + 1 }}: {{ fixedMs(span.duration) }} ms
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
import EChart from 'vue-echarts';
import BenchmarkClient from '@/util/BenchmarkClient';
import StepsTable from '../StepsTable';
import 'echarts/lib/chart/bar';
import 'echarts/lib/component/tooltip';
import { flattenStepSpans, attachRepsToChildSpans } from '@/util/ExecutionDataFormatters';
import { getQueryCardChartOptions, produceStepsAndGroups } from './util';
import {
  getMean, getStdDeviation, getMedian, getOutliers,
} from '@/util/math';


export default {
  components: { EChart, StepsTable },

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
      loading: { show: false },

      stepSpans: [],

      stepsAndGroups: [],

      queryExpanded: this.expanded,

      // used within the component that fills the right-side panel (e.g. stepsTable) to be set as the max-height of the relevant element
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

    histogramSpans() {
      return this.querySpans
        .filter(duration => !this.outlierSpans.includes(duration))
        .sort((a, b) => (a.duration > b.duration ? 1 : -1));
    },

    queryCardChartOptions() {
      return getQueryCardChartOptions(this.histogramSpans);
    },

    spansSortedByDuration() {
      const spansSortedByDuration = this.querySpans;
      return spansSortedByDuration.sort((a, b) => (a.duration > b.duration ? 1 : -1));
    },

    outlierSpans() {
      const durations = this.spansSortedByDuration.map(span => span.duration);
      const outliers = getOutliers(durations).upper;
      return this.querySpans.filter(span => outliers.includes(span.duration));
    },

    median() {
      return getMedian(this.getDurations()).value;
    },

    mean() {
      return getMean(this.getDurations());
    },

    stdDeviation() {
      return getStdDeviation(this.getDurations());
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

    getDurations() {
      return this.querySpans.map(span => span.duration);
    },

    async toggleStepsTable() {
      this.loading.show = true;

      if (!this.stepsAndGroups.length) {
        await this.fetchStepSpans();
      }

      this.queryExpanded = !this.queryExpanded;

      this.loading.show = false;
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

      produceStepsAndGroups(this.stepSpans, this.stepsAndGroups, this.filterStepSpans);
    },

    filterStepSpans(stepNumber) {
      return this.stepSpans.filter(stepSpan => stepSpan.order === stepNumber);
    },
  },
};
</script>

<style lang="scss" scoped src="./style.scss"></style>
