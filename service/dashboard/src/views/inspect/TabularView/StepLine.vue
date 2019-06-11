<template>
  <div>
    <div
      :id="anchorId"
      :class="'flexed tableRow ' + (expaned ? 'expanded' : '')"
      @click="toggleChildSteps()"
    >
      <span :style="'text-align: left; padding-left:' + padding + 'px;'">
        <i class="el-icon el-icon-arrow-right" />
        {{ step | truncate(100) }}
      </span>

      <el-tooltip
        class="item"
        effect="dark"
        :content="minTooltipContent()"
        placement="top"
      >
        <span
          :class="isFastestMember === true ? 'fastest' : ''"
        >{{ minSpan.duration | fixedMs }}/{{ minSpan.rep + 1 | ordinalise }}</span>
      </el-tooltip>

      <el-tooltip
        class="item"
        effect="dark"
        :content="medianTooltipContent()"
        placement="top"
      >
        <span>{{ median | fixedMs }}/{{ reps }}</span>
      </el-tooltip>

      <el-tooltip
        class="item"
        effect="dark"
        :content="maxTooltipContent()"
        placement="top"
      >
        <span
          :class="isSlowestMember === true ? 'slowest' : ''"
        >{{ maxSpan.duration | fixedMs }}/{{ maxSpan.rep + 1 | ordinalise }}</span>
      </el-tooltip>
    </div>
    <div v-if="expaned">
      <step-line
        v-for="childStepName in childStepNames"
        :key="childStepName"
        :step="childStepName"
        :step-spans="filterChildStepSpans(childStepName)"
        :padding="padding + 20"
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

    anchorId: {
      type: String,
      required: false,
      default: '',
    },

    isFastestMember: {
      type: Boolean,
      required: false,
    },

    isSlowestMember: {
      type: Boolean,
      required: false,
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

    minTooltipContent() {
      const { ordinalise } = this.$options.filters;
      return `The ${ordinalise(
        this.minSpan.rep + 1,
      )} repetition of this step was the FASTEST.`;
    },

    maxTooltipContent() {
      const { ordinalise } = this.$options.filters;
      return `The ${ordinalise(
        this.maxSpan.rep + 1,
      )} repetition of this step was the SLOWEST.`;
    },

    medianTooltipContent() {
      const { fixedMs } = this.$options.filters;
      return `Among all ${this.reps} repetitions of this step, the median was ${fixedMs(
        this.median,
      )}.`;
    },
  },
};
</script>

<style lang="scss" scoped>
@import "./src/assets/css/variables.scss";

.tableRow {
  background-color: #fafafa;
  border-bottom: 1px solid $color-border-light;
  cursor: pointer;

  &.expanded {
    .el-icon {
      transform: rotate(90deg);
    }
  }

  span {
    text-align: center;
    padding: $padding-more/2 0;

    &:nth-child(1) {
      width: 300px;
      box-sizing: border-box;
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
      padding-right: 20px;
    }
  }

  .fastest {
    color: #27ae60;
  }

  .slowest {
    color: #c0392b;
  }

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: $color-bg-table-hover;
  }
}
</style>
