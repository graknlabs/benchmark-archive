<template>
  <div>
    <el-row class="currentRow" :class="{'bordered': !expand, 'highlighted': expand}">
      <el-col :span="14" class="query-column" style="text-align: left;">
        <i :class="{ 'el-icon-arrow-right': !expand, 'el-icon-arrow-down': expand }" @click="expandLine"/>
        <div>{{ query }}</div>
      </el-col>
      <el-col :span="3">
        {{ min.duration | fixedMs }} ({{ min.tags.repetition + 1 }})
      </el-col>
      <el-col :span="3">
        {{ med | fixedMs }}
      </el-col>
      <el-col :span="3">
        {{ max.duration | fixedMs }} ({{ max.tags.repetition + 1 }})
      </el-col>
      <el-col :span="1">
        {{ reps }}
      </el-col>
    </el-row>
    <el-row v-if="expand" :class="{'bordered': expand}">
      <step-line
        v-for="stepNumber in stepNumbers"
        :key="stepNumber"
        :spans="filterSpansByStep(stepNumber)"
        padding="15"
      />
    </el-row>
  </div>
</template>
<style scoped>
.currentRow {
  margin: 5px 0px;
  padding: 5px;
  font-weight: 300;
}
.bordered{
  border-bottom: 1px solid #ebeef5;
}
.highlighted{
  font-weight: 400;
}
.currentRow:hover {
  background-color: #fcdaba;
}
.query-column{
  display: flex;
  justify-content: start;
}
.el-col{
  text-align: center;
}
i {
  cursor: pointer;
  margin-right: 5px;
}
</style>

<script>
import BenchmarkClient from '@/util/BenchmarkClient';
import StepLine from './StepLine.vue';

export default {
  name: 'QueryLine',
  components: { StepLine },
  filters: {
    fixedMs(num) {
      return `${Number(num / 1000).toFixed(3)} ms`;
    },
  },
  props: ['query', 'spans', 'isOverviewQuery'],
  data() {
    return {
      expand: false,
      children: [],
      stepNumbers: null,
    };
  },
  created() {
    if (this.isOverviewQuery) this.expandLine();
  },
  computed: {
    min() {
      let min = this.spans[0];
      this.spans.forEach((span) => {
        if (span.duration < min.duration) {
          min = span;
        }
      });
      return min;
    },
    max() {
      let max = this.spans[0];
      this.spans.forEach((span) => {
        if (span.duration > max.duration) {
          max = span;
        }
      });
      return max;
    },
    med() {
      const durations = this.spans.map(span => span.duration);
      durations.sort((a, b) => a - b);
      const middle = (durations.length + 1) / 2;
      const isEven = durations.length % 2 === 0;
      return isEven
        ? (durations[middle - 1.5] + durations[middle - 0.5]) / 2
        : durations[middle - 1];
    },
    reps() {
      return this.spans.length;
    },
  },
  methods: {
    expandLine() {
      this.expand = !this.expand;
      if (!this.expand) return;
      this.fetchChildrenSpans();
    },
    fetchChildrenSpans() {
      BenchmarkClient.getSpans(
        `{ childrenSpans( parentId: [${
          this.spans.map(span => `"${span.id}"`).join()
        }] limit: 1000){ id name duration parentId tags { childNumber }} }`,
      ).then((resp) => {
        this.children = this.attachRepetition(resp.data.childrenSpans);
        this.stepNumbers = [...new Set(this.children.map(child => child.tags.childNumber))];
        this.stepNumbers.sort((a, b) => a - b);
      });
    },
    filterSpansByStep(stepNumber) {
      return this.children.filter(child => child.tags.childNumber === stepNumber);
    },
    attachRepetition(childrenSpans) {
      // Children spans don't have the tags repetition and repetitions, so we attach them here taking the values from parent
      return childrenSpans.map((span) => {
        const parentTag = this.spans.filter(parent => parent.id === span.parentId)[0].tags;
        return Object.assign({ repetition: parentTag.repetition, repetitions: parentTag.repetitions }, span);
      });
    },
  },
};
</script>
