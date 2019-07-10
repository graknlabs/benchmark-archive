<template>
  <div>
    <query-card
      v-for="query in queries"
      :key="query"
      :query="query"
      :query-spans="getQuerySpans(query)"
      :expanded="query === preSelectedQuery"
    />
  </div>
</template>

<script>
import QueryCard from '../QueryCard';

export default {
  name: 'QueryCards',

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
      return this.scaledQuerySpans.filter(querySpan => querySpan.value === queryValue);
    },
  },
};
</script>
