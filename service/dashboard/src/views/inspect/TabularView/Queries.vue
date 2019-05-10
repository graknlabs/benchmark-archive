<template>
  <div>
    <div class="cards-list">
      <query-card
        v-for="query in queries"
        :key="query"
        :query="query"
        :query-spans="getQuerySpans(query)"
      />
    </div>
  </div>
</template>

<script>
import QueryCard from './QueryCard.vue';

export default {
  name: 'Queries',

  components: { QueryCard },

  props: {
    preSelectedQuery: {
      type: String,
      required: false,
      default: null,
    },

    scaledQuerySpans: {
      type: Array,
      required: true,
    },
  },

  computed: {
    queries() {
      return [...new Set(this.scaledQuerySpans.map(querySpan => querySpan.value))];
    },
  },

  methods: {
    getQuerySpans(queryValue) {
      return this.scaledQuerySpans.filter(querySpan => querySpan.value === queryValue).map(span => ({ rep: span.rep, duration: span.duration }));
    },
  },
};
</script>

<style scoped>
.header {
  font-weight: bold;
  margin-top: 10px;
  margin-bottom: 10px;
}
.el-col{
    text-align: center;
}
</style>
