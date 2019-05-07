<template>
  <div>
    <div class="cards-list">
      <query-card
        v-for="scaledQuerySet in scaledQueriesSet"
        :key="scaledQuerySet.value"
        :query="scaledQuerySet"
      />
    </div>
  </div>
</template>

<script>
import QueryCard from './QueryCard.vue';

export default {
  name: 'QueriesTable',

  components: { QueryCard },

  props: {
    preSelectedQuery: {
      type: String,
      required: false,
    },

    scaledQueriesSet: {
      type: Array,
      required: true,
    }
  },
}
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
