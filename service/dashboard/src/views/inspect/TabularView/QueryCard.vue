<template>
  <el-card @click.native="fetchSteps">
    <div class="flexed">
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
  </el-card>
</template>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
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
      steps: null,
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

    fetchSteps() {
      console.log('clicked');
      BenchmarkClient.getSpans(
        `{ childrenSpans( parentId: [${this.querySpans
          .map(querySpan => `"${querySpan.id}"`)
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
