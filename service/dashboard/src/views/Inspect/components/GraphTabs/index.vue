<template>
  <div class="graph-tabs-wrapper">
    <b-tabs>
      <b-tab
        v-for="graphType in graphTypes"
        :key="graphType"
        :title="graphType"
        :active="graphType === activeGraphType"
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
      required: true,
    },

    querySpans: {
      type: Array,
      required: true,
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
      return [...new Set(this.graphs.map(graph => graph.type))];
    },

    activeGraphType() {
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
      return (this.preSelectedGraphType === graphType ? this.preSelectedScale : null);
    },

    getPreSelectedQuery(graphType) {
      return ((this.preSelectedGraphType === graphType) ? this.preSelectedQuery : null);
    },
  },
};
</script>

<style lang="scss" scopped src="./style.scss"></style>
