<template>
  <el-card @click.native="fetchSteps">
    <div class="flexed">
      <el-tooltip
        class="item"
        effect="dark"
        :content="query.value"
        placement="top"
      >
        <span style="width: 300px;">{{ query.value | truncate(100) }}</span>
      </el-tooltip>

      <el-tooltip
        class="item"
        effect="dark"
        content="Min/Rep"
        placement="top"
      >
        <span
          class="text-size-18"
        >{{ minSpan.span.duration | fixedMs }}/{{ minSpan.order + 1 | ordinalise }}</span>
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
        >{{ maxSpan.span.duration | fixedMs }}/{{ maxSpan.order + 1 | ordinalise }}</span>
      </el-tooltip>
    </div>
  </el-card>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import copy from 'copy-to-clipboard';
import ordinal from 'ordinal';

export default {

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
      type: Object,
      required: true,
    },
  },

  data() {
    return {
      steps: null,
    };
  },

  computed: {
    sortedSpans() {
      return this.query.reps.sort((a, b) => (a.span.duration > b.span.duration ? 1 : -1));
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
        (this.sortedSpans[lowMiddleIndex].span.duration
          + this.sortedSpans[highMiddleIndex].span.duration)
        / 2
      );
    },

    reps() {
      return this.query.reps.length;
    },
  },

  methods: {
    fetchSteps() {
      console.log('clicked');
      BenchmarkClient.getSpans(
        `{ childrenSpans( parentId: [${this.spans
          .map(span => `"${span.id}"`)
          .join()}] limit: 1000){ id name duration parentId tags { childNumber }} }`,
      ).then((resp) => {
        // console.log(resp.data.childrenSpans)
        this.steps = this.attachRepetition(resp.data.childrenSpans);
        // this.stepNumbers = [...new Set(this.children.map(child => child.tags.childNumber))];
        // this.stepNumbers.sort((a, b) => a - b);
      });
    },

    attachRepetition(childrenSpans) {
      // Children spans don't have the tags repetition and repetitions, so we attach them here taking the values from parent
      return childrenSpans.map((span) => {
        const parentTag = this.spans.filter(
          parent => parent.id === span.parentId,
        )[0].tags;
        // console.log(parentTag);
        return Object.assign(
          {
            repetition: parentTag.repetition,
            repetitions: parentTag.repetitions,
          },
          span,
        );
      });
    },
  },
};
</script>

<style lang="scss" scoped>
@import "./src/assets/css/variables.scss";

.spans {
  margin-top: $margin-default;
}
</style>
