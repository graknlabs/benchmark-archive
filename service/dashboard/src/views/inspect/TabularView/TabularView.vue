<template>
  <div v-if="graphs && querySpans">
    <el-tabs
      :value="activeGraph"
      type="border-card"
      class="wrapper"
    >
      <el-tab-pane
        v-for="graphType in graphTypes"
        :key="graphType"
        :label="graphType"
        :name="graphType"
      >
        <graph-tab
          :query-spans="filterQuerySpans(graphType)"
          :pre-selected-query="getPreSelectedQuery(graphType)"
          :pre-selected-scale="getPreSelectedScale(graphType)"
          :graph-type="graphType"
          :graphs="filterGraphs(graphType)"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import GraphTab from './GraphTab.vue';

export default {
  name: 'TabularView',

  components: { GraphTab },

  props: {
    graphs: {
      type: Array,
      required: false,
      default: null,
    },

    querySpans: {
      type: Array,
      required: false,
      default: null,
    },

    preSelectedGraphName: {
      type: String,
      required: false,
      default: null,
    },

    preSelectedQuery: {
      type: String,
      required: false,
      default: null,
    },

    preSelectedScale: {
      type: Number,
      required: false,
      default: null,
    },
  },

  computed: {
    graphTypes() {
      const uniqueGraphTypes = [
        ...new Set(this.graphs.map(graph => graph.type)),
      ];
      return uniqueGraphTypes;
    },

    activeGraph() {
      return this.preSelectedGraphName || this.graphTypes[0];
    },
  },

  methods: {
    filterQuerySpans(graphType) {
      const querySpans = [];
      const graphsOfInterest = this.filterGraphs(graphType);
      graphsOfInterest.forEach((graph) => {
        this.querySpans
          .filter(query => query.parentId === graph.id)
          .forEach((query) => {
            querySpans.push(query);
          });
      });

      return querySpans;
    },

    filterGraphs(graphType) {
      return this.graphs.filter(graph => graph.type === graphType);
    },

    getPreSelectedScale(graphName) {
      if (this.preSelectedGraphName === graphName) return this.preSelectedScale;
      return null;
    },

    getPreSelectedQuery(graphName) {
      if (this.preSelectedGraphName === graphName) {
        return this.preSelectedQuery;
      }
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
