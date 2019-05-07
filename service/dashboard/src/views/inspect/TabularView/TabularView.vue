<template>
  <el-tabs
    :value="activeGraph"
    type="border-card"
    class="wrapper"
  >
    <el-tab-pane
      v-for="graphName in graphNames"
      :key="graphName"
      :label="graphName"
      :name="graphName"
    >
      <graph-tab
        :typed-queries-set="getTypedQueries(graphName)"
        :pre-selected-query="getPreSelectedQuery(graphName)"
        :pre-selected-scale="getPreSelectedScale(graphName)"
        :graphName="graphName"
      />
    </el-tab-pane>
  </el-tabs>
</template>

<script>
import GraphTab from './GraphTab.vue';

export default {
  name: 'TabularView',

  components: { GraphTab },

  props: {
    querySets: {
      type: Array,
      required: true,
    },

    preSelectedGraphName: {
      type: String,
      required: false,
    },

    preSelectedQuery: {
      type: String,
      required: false,
    },

    preSelectedScale: {
      type: Number,
      required: false,
    },
  },

  computed: {
    graphNames() {
      return this.querySets.map(querySet => querySet.type);
    },

    activeGraph() {
      return this.preSelectedGraphName || this.graphNames[0];
    },
  },

  methods: {
    getTypedQueries(graphName) {
      return this.querySets.filter(querySet => querySet.type === graphName)[0].queryTypes;
    },

    getPreSelectedScale(graphName) {
      if (this.preSelectedGraphName === graphName) return this.preSelectedScale;
      return null;
    },

    getPreSelectedQuery(graphName) {
      if (this.preSelectedGraphName === graphName) { return this.preSelectedQuery; }
      return null;
    },
  },
};
</script>

<style scoped>
.wrapper {
  margin-top: 20px;
}
</style>
