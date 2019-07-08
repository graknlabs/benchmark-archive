<template>
  <section class="steps-table">
    <div class="table-head">
      <p class="table-cell">
        Step
      </p>
      <p class="table-cell">
        Min/Rep|Member
      </p>
      <p class="table-cell">
        Median/Reps
      </p>
      <p class="table-cell">
        Max/Rep
      </p>
    </div>

    <div
      class="table-body"
      :style="'max-height: ' + tbodyMaxHeight + 'px'"
    >
      <template
        v-for="stepOrGroup in stepsAndGroups"
      >
        <group-line
          v-if="stepOrGroup.hasOwnProperty('members')"
          :key="stepOrGroup.name"
          :members="stepOrGroup.members"
          :padding="0"
        />

        <step-line
          v-if="!stepOrGroup.hasOwnProperty('members')"
          :key="stepOrGroup.name"
          :step="stepOrGroup.name"
          :step-spans="filterStepSpans(stepOrGroup.order)"
          :padding="0"
        />
      </template>
    </div>
  </section>
</template>

<script>
import StepLine from '../StepLine';
import GroupLine from '../GroupLine';

export default {
  name: 'StepsTable',

  components: { StepLine, GroupLine },

  filters: {
    fixedMs(num) {
      return `${Number(num / 1000).toFixed(3)}`;
    },
  },

  props: {
    stepsAndGroups: {
      type: Array,
      required: true,
    },

    stepSpans: {
      type: Array,
      required: true,
    },

    maxHeight: {
      type: Number,
      required: true,
    },
  },

  data() {
    return {
      theadHeight: 39,
    };
  },

  computed: {
    tbodyMaxHeight() {
      return this.maxHeight - this.theadHeight;
    },
  },

  methods: {
    filterStepSpans(stepNumber) {
      return this.stepSpans.filter(stepSpan => stepSpan.order === stepNumber);
    },
  },
};
</script>

<style lang="scss" scopped src="./style.scss"></style>

<style lang="scss">
.steps-table {
  .table-row {
    cursor: pointer;
  }

  .table-cell:first-child {
    max-width: 200px;
  }
}
</style>
