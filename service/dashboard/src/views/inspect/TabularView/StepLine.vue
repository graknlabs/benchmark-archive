<template>
  <div>
    <div
      :class="'flexed tableRow ' + (expaned ? 'expanded' : '')"
      :style="'padding-left:' + padding + 'px;'"
      @click="toggleChildSteps()"
    >
      <span
        class="stepName"
        style="width: 300px;"
      >
        <i class="el-icon el-icon-arrow-right" />
        {{ step | truncate(100) }}
      </span>

      <span
        :style="'width: 110px; margin-left: -' + padding + 'px'"
      >{{ minSpan.duration | fixedMs }}/{{ minSpan.rep + 1 | ordinalise }}</span>

      <span style="width: 90px;">{{ median | fixedMs }}/{{ reps }}</span>

      <span
        style="width: 115px;"
      >{{ maxSpan.duration | fixedMs }}/{{ maxSpan.rep + 1 | ordinalise }}</span>
    </div>
    <div v-if="expaned">
      <step-line
        v-for="childStepName in childStepNames"
        :key="childStepName"
        :step="childStepName"
        :step-spans="filterChildStepSpans(childStepName)"
        :padding="padding+10"
      />
    </div>
  </div>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import EDF from '@/util/ExecutionDataFormatters';
import ordinal from 'ordinal';

const { flattenStepSpans, attachRepsToChildSpans } = EDF;

export default {
  name: 'StepLine',

  filters: {
    fixedMs(num) {
      return `${Number(num / 1000).toFixed(3)}`;
    },

    ordinalise(num) {
      return ordinal(num);
    },
  },

  props: {
    stepSpans: {
      type: Array,
      required: true,
    },

    step: {
      type: String,
      required: true,
    },

    padding: {
      type: Number,
      required: true,
    },
  },

  data() {
    return {
      expaned: false,
      childStepSpans: null,
      childStepNames: null,
    };
  },

  computed: {
    sortedSpans() {
      const sortedSpans = this.stepSpans;
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
      return this.stepSpans.length;
    },
  },

  methods: {
    toggleChildSteps() {
      this.expaned = !this.expaned;
      if (!this.expaned) return;
      this.fetchChildStepSpans();
    },

    async fetchChildStepSpans() {
      const childStepSpansResp = await BenchmarkClient.getSpans(
        `{ childrenSpans( parentId: [${this.stepSpans
          .map(stepSpan => `"${stepSpan.id}"`)
          .join()}]){ id name duration parentId timestamp tags { childNumber }} }`,
      );

      let childStepSpans = childStepSpansResp.data.childrenSpans;
      childStepSpans = flattenStepSpans(childStepSpans, this.stepSpans);

      this.childStepSpans = attachRepsToChildSpans(
        childStepSpans,
        this.stepSpans,
      );

      if (this.childStepSpans.length) {
        const { parentId } = this.childStepSpans[0];

        this.childStepNames = this.childStepSpans
          .filter(childStepSpan => childStepSpan.parentId === parentId)
          .sort((a, b) => a.timestamp > b.timestamp)
          .map(childStepSpan => childStepSpan.name);
      }
    },

    filterChildStepSpans(childStepName) {
      return this.childStepSpans.filter(
        childStepSpan => childStepSpan.name === childStepName,
      );
    },
  },
};
</script>

<style lang="scss" scoped>
@import "./src/assets/css/variables.scss";

.tableRow {
  border-bottom: 1px solid $color-border-light;

  &.expanded {
    .el-icon {
      transform: rotate(90deg);
    }
  }

  .stepName {
    text-align: left;
    padding-left: $padding-default;

    .el-icon {
      cursor: pointer;
    }
  }

  span {
    text-align: center;
    padding: $padding-more/2 0;
  }

  &:nth-child(odd) {
    background-color: $color-bg-table-alternate;
  }

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: $color-bg-table-hover;
  }
}
</style>
