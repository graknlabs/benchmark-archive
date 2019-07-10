<template>
  <div
    v-if="graphs && querySpans"
    class="graph-tabs-wrapper"
  >
    <b-tabs>
      <b-tab
        v-for="graphType in graphTypes"
        :key="graphType"
        :title="graphType"
        :active="graphType === activeGraph"
      >
        <graph-tab
          :query-spans="filterQuerySpans(graphType)"
          :pre-selected-query="getPreSelectedQuery(graphType)"
          :pre-selected-scale="getPreSelectedScale(graphType)"
          :graph-type="graphType"
          :graphs="filterGraphs(graphType)"
        />
      </b-tab>
    </b-tabs>
  </div>
</template>

<script>
import GraphTab from '../GraphTab';

export default {
  name: 'GraphTabs',

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

    preSelectedGraphType: {
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
      return this.preSelectedGraphType || this.graphTypes[0];
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

    getPreSelectedScale(graphType) {
      if (this.preSelectedGraphType === graphType) return this.preSelectedScale;
      return null;
    },

    getPreSelectedQuery(graphType) {
      if (this.preSelectedGraphType === graphType) {
        return this.preSelectedQuery;
      }
      return null;
    },
  },
};
</script>

<style lang="scss" scopped src="./style.scss"></style>
