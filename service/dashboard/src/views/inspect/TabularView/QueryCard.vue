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
          <div class="queryCardDetail">
            <p
              class="tableHeader"
              style="text-align: left;"
            >
              Query/Step
            </p>
            <p style="text-align: left;">
              {{ query | truncate(100) }}
            </p>
          </div>
        </el-tooltip>

        <el-tooltip
          class="item"
          effect="dark"
          :content="minTooltipContent()"
          placement="top"
        >
          <div class="queryCardDetail">
            <p class="tableHeader">
              Min/Rep
            </p>
            <p class="text-size-18">
              {{ minSpan.duration | fixedMs }}/{{ minSpan.rep + 1 | ordinalise }}
            </p>
          </div>
        </el-tooltip>

        <el-tooltip
          class="item"
          effect="dark"
          :content="medianTooltipContent()"
          placement="top"
        >
          <div class="queryCardDetail">
            <p class="tableHeader">
              Median/Reps
            </p>
            <p class="text-size-18">
              {{ median | fixedMs }}/{{ reps }}
            </p>
          </div>
        </el-tooltip>

        <el-tooltip
          class="item"
          effect="dark"
          :content="maxTooltipContent()"
          placement="top"
        >
          <div class="queryCardDetail">
            <p class="tableHeader">
              Max/Rep
            </p>
            <p class="text-size-18">
              {{ maxSpan.duration | fixedMs }}/{{ maxSpan.rep + 1 | ordinalise }}
            </p>
          </div>
        </el-tooltip>
      </div>

      <section
        v-if="queryExpanded"
        class="stepsTable"
      >
        <template v-for="(stepOrGroup, index) in stepsAndGroups">
          <group-line
            v-if="stepOrGroup.hasOwnProperty('members')"
            :key="index"
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
import StepLine from './StepLine.vue';
import GroupLine from './GroupLine.vue';
import EDF from '@/util/ExecutionDataFormatters';
import ordinal from 'ordinal';

const { flattenStepSpans, attachRepsToChildSpans } = EDF;

export default {
  components: { StepLine, GroupLine },

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

  created() {
    if (this.expanded) {
      this.fetchStepSpans();
    }
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
      stepSpans = flattenStepSpans(stepSpans, this.querySpans);
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

    minTooltipContent() {
      const { ordinalise } = this.$options.filters;
      return `The ${ordinalise(
        this.minSpan.rep + 1,
      )} repetition of this query was the FASTEST.`;
    },

    maxTooltipContent() {
      const { ordinalise } = this.$options.filters;
      return `The ${ordinalise(
        this.maxSpan.rep + 1,
      )} repetition of this query was the SLOWEST.`;
    },

    medianTooltipContent() {
      const { fixedMs } = this.$options.filters;
      return `Among all ${this.reps} repetitions of this query, the median was ${fixedMs(
        this.median,
      )}.`;
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

.queryCardDetails {
  cursor: pointer;

  padding: $padding-default;

  .queryCardDetail {
    text-align: center;

    &:nth-child(1) {
      width: 300px;
    }

    &:nth-child(2) {
      width: 100px;
    }

    &:nth-child(3) {
      width: 100px;
    }

    &:nth-child(4) {
      width: 100px;
    }
  }
}

.tableHeader {
  padding-bottom: $padding-less;

  color: $color-text-gray;
  font-size: $font-size-table-header;
  font-weight: 600;
}

.stepsTable {
  background-color: #fafafa;
  // padding :0 $padding-default;

  span {
    text-align: center;
  }
}

.spans {
  margin-top: $margin-default;
}
</style>
