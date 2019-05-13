<template>
  <el-card>
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
        <span style="width: 300px;">{{ query | truncate(100) }}</span>
      </el-tooltip>

      <el-tooltip
        class="item"
        effect="dark"
        content="Min/Rep"
        placement="top"
      >
        <span
          class="text-size-18"
        >{{ minSpan.duration | fixedMs }}/{{ minSpan.rep + 1 | ordinalise }}</span>
      </el-tooltip>

      <el-tooltip
        class="item"
        effect="dark"
        content="Median/Reps"
        placement="top"
      >
        <span class="text-size-18">{{ median | fixedMs }}/{{ reps }}</span>
      </el-tooltip>

      <el-tooltip
        class="item"
        effect="dark"
        content="Max/Rep"
        placement="top"
      >
        <span
          class="text-size-18"
        >{{ maxSpan.duration | fixedMs }}/{{ maxSpan.rep + 1 | ordinalise }}</span>
      </el-tooltip>
    </div>

    <section
      v-if="expanded"
      class="stepsTable"
    >
      <div class="flexed tableHeader">
        <span style="width: 320px;">Query</span>
        <span style="width: 115px;">Min/Rep</span>
        <span style="width: 90px;">Median/Reps</span>
        <span style="width: 115px;">Max/Rep</span>
      </div>
      <step-line
        v-for="stepNumber in stepNumbers"
        :key="stepNumber"
        :step="filterStepSpans(stepNumber)[0].name"
        :step-spans="filterStepSpans(stepNumber)"
        :padding=0
      />
    </section>
  </el-card>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import StepLine from './StepLine.vue';
import EDF from '@/util/ExecutionDataFormatters';
import ordinal from 'ordinal';

const { flattenStepSpans, attachRepsToChildSpans } = EDF;

export default {
  components: { StepLine },

  filters: {
    fixedMs(num) {
      return `${Number(num / 1000).toFixed(3)}`;
    },

    ordinalise(num) {
      return ordinal(num);
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
  },

  data() {
    return {
      expanded: false,
      stepSpans: null,
      stepNumbers: 0,
    };
  },

  computed: {
    sortedSpans() {
      const sortedSpans = this.querySpans;
      return sortedSpans.sort((a, b) => (a.duration > b.duration ? 1 : -1));
    },

    minSpan() {
      return this.sortedSpans[0];
    },

    maxSpan() {
      return this.sortedSpans[this.sortedSpans.length - 1];
    },

    median() {
      const lowMiddleIndex = Math.floor((this.sortedSpans.length - 1) / 2);
      const highMiddleIndex = Math.ceil((this.sortedSpans.length - 1) / 2);
      return (
        (this.sortedSpans[lowMiddleIndex].duration
          + this.sortedSpans[highMiddleIndex].duration)
        / 2
      );
    },

    reps() {
      return this.querySpans.length;
    },
  },

  methods: {
    toggleStepsTable() {
      this.expanded = !this.expanded;
      if (!this.expanded) return;
      this.fetchStepSpans();
    },

    async fetchStepSpans() {
      const querySpanIds = this.querySpans.map(querySpan => `"${querySpan.id}"`).join();

      const stepSpansResp = await BenchmarkClient.getSpans(
        `{ childrenSpans( parentId: [
          ${querySpanIds}
          ] limit: 1000){ id name duration parentId tags { childNumber }} }`,
      );
      let stepSpans = stepSpansResp.data.childrenSpans;
      stepSpans = flattenStepSpans(stepSpans, this.querySpans);
      this.stepSpans = attachRepsToChildSpans(stepSpans, this.querySpans);
      this.stepNumbers = [
        ...new Set(this.stepSpans.map(stepSpan => stepSpan.order)),
      ].sort((a, b) => a - b);
    },

    filterStepSpans(stepNumber) {
      return this.stepSpans.filter(stepSpan => stepSpan.order === stepNumber);
    },
  },
};
</script>

<style lang="scss" scoped>
@import "./src/assets/css/variables.scss";

.queryCardDetails {
  cursor: pointer;
}

.stepsTable {
  margin-top: $margin-most;
  margin-right: -$margin-default;
  margin-bottom: -$margin-default;
  margin-left: -$margin-default;

  .tableHeader {
    border-bottom: 1px solid $color-border-light;
    padding-bottom: $padding-least;

    span {
      color: $color-text-gray;
      font-size: $font-size-table-header;
      font-weight: 600;
    }
  }

  span {
    text-align: center;
  }
}

.spans {
  margin-top: $margin-default;
}
</style>
