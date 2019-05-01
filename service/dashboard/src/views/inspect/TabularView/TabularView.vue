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
        :spans="getFilterSpans(graphName)"
        :pre-selected-query="getPreSelectedQuery(graphName)"
        :pre-selected-scale="getPreSelectedScale(graphName)"
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
    graphNames: {
      type: Array,
      required: true
    },

    spans: {
      type: Array,
      required: true
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
      required: false
    }
  },

  computed: {
    activeGraph() {
      return this.preSelectedGraphName || this.graphNames[0];
    }
  },

  methods: {
    getFilterSpans(graphName) {
      return this.spans.filter(span => span.tags.graphType === graphName);
    },

    getPreSelectedScale(graphName) {
      if (this.preSelectedGraphName === graphName) { return this.preSelectedScale; }
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
