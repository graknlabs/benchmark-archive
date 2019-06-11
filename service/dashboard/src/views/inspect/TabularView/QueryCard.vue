<template>
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
        <span style="width: 300px;">{{ query | truncate(100) }}</span>
      </el-tooltip>

      <el-tooltip
        class="item"
        effect="dark"
        content="Min/Rep"
        placement="top"
      >
        <span class="text-size-18">{{ minSpan.duration | fixedMs }}/{{ minSpan.rep + 1 | ordinalise }}</span>
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
        <span class="text-size-18">{{ maxSpan.duration | fixedMs }}/{{ maxSpan.rep + 1 | ordinalise }}</span>
      </el-tooltip>
    </div>

    <section
      v-if="queryExpanded"
      class="stepsTable"
    >
      <div
        v-show="!loading"
        class="flexed tableHeader"
      >
        <span style="width: 320px;">Query</span>
        <span style="width: 115px;">Min/Rep</span>
        <span style="width: 90px;">Median/Reps</span>
        <span style="width: 115px;">Max/Rep</span>
      </div>
      <template v-for="(stepOrGroup, index) in stepsAndGroups">
        <group-line
          v-if="stepOrGroup.hasOwnProperty('members')"
          :key="index"
          :members="stepOrGroup.members"
        />

        <step-line
          v-if="!stepOrGroup.hasOwnProperty('members')"
          :key="stepOrGroup.name"
          :step="stepOrGroup.name"
          :step-spans="filterStepSpans(stepOrGroup.order)"
          :padding="0"
        />
      </template>
    </section>
  </el-card>
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
          group.members[grouppedStep.order] = this.filterStepSpans(grouppedStep.order);
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
