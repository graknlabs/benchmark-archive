<template>
  <div>
    <div
      :class="'table-row ' + (expaned ? 'expanded' : '')"
      @click="toggleMembers"
    >
      <p
        class="table-cell"
        :style="' padding-left:' + padding + 'px;'"
      >
        <i class="el-icon el-icon-arrow-right" />
        {{ groupName | truncate(100) }}
      </p>

      <p class="table-cell">
        {{ minSpan.duration | fixedMs }}/<a :href="'#' + uniqueIdentifier + indexOfMinSpan">{{ indexOfMinSpan | ordinalise }}</a>
      </p>

      <p class="table-cell">
        {{ median | fixedMs }}/{{ reps }}
      </p>

      <p class="table-cell">
        {{ maxSpan.duration | fixedMs }}/<a :href="'#' + uniqueIdentifier + indexOfMaxSpan">{{ indexOfMaxSpan | ordinalise }}</a>
      </p>
    </div>

    <template v-if="expaned">
      <step-line
        v-for="(memberOrder, index) of Object.keys(members)"
        :key="memberOrder"
        :step="index + 1 + '. ' + members[memberOrder][0].name"
        :anchor-id="uniqueIdentifier + getIndexForSpan(memberOrder)"
        :step-spans="members[memberOrder]"
        :is-fastest-member="isSpanFastest(memberOrder)"
        :is-slowest-member="isSpanSlowest(memberOrder)"
        :padding="padding + 20"
      />
    </template>
  </div>
</template>

<script>
import ordinal from 'ordinal';
import StepLine from '../StepLine';

export default {
  name: 'GroupLine',

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
    members: {
      type: Object,
      required: true,
    },

    padding: {
      type: Number,
      required: true,
    },
  },

  data() {
    return {
      loading: false,

      expaned: false,
    };
  },

  computed: {
    uniqueIdentifier() {
      return this.memberSpans[0].id;
    },

    groupName() {
      return `G (${this.memberSpans[0].name}) - ${Object.keys(this.members).length}`;
    },

    reps() {
      return this.members[Object.keys(this.members)[0]].length;
    },

    memberSpans() {
      return Object.values(this.members).flat();
    },

    firstMemberOrder() { // used in finding the correct index of a span among all members
      return Object.keys(this.members).sort((a, b) => (parseInt(a, 0) > parseInt(b, 0) ? 1 : -1))[0];
    },

    durationSortedMemberSpans() {
      const spans = this.memberSpans;
      return spans.sort((a, b) => (a.duration > b.duration ? 1 : -1));
    },

    minSpan() {
      return this.durationSortedMemberSpans[0];
    },

    indexOfMinSpan() {
      return this.getIndexForSpan(this.minSpan.order);
    },

    maxSpan() {
      return this.durationSortedMemberSpans[this.durationSortedMemberSpans.length - 1];
    },

    indexOfMaxSpan() {
      return this.getIndexForSpan(this.maxSpan.order);
    },

    median() {
      const lowMiddleIndex = Math.floor((this.durationSortedMemberSpans.length - 1) / 2);
      const highMiddleIndex = Math.ceil((this.durationSortedMemberSpans.length - 1) / 2);
      return (
        (this.durationSortedMemberSpans[lowMiddleIndex].duration
          + this.durationSortedMemberSpans[highMiddleIndex].duration)
        / 2
      );
    },
  },

  methods: {
    toggleMembers(e) {
      if (e.target.tagName !== 'A') { // the clicked element on the card is not a link
        this.expaned = !this.expaned;
      }
    },

    getIndexForSpan(order) {
      return order - this.firstMemberOrder + 1;
    },

    isSpanFastest(order) {
      return this.indexOfMinSpan === order - this.firstMemberOrder + 1;
    },

    isSpanSlowest(order) {
      return this.indexOfMaxSpan === order - this.firstMemberOrder + 1;
    },
  },
};
</script>
