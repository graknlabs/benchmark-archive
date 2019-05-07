<template>
  <div>
    <el-row type="flex" justify="end" class="queries-action-bar">
      <div class="action-item">
        <scale-selector
          title="Scale"
          :items="scales.map(scale => ({ text: scale, value: scale }))"
          :default-item="{ text: selectedScale, value: selectedScale }"
          @item-selected="(scale) => { this.selectedScale = scale; }"
        />
      </div>
    </el-row>

    <queries-table
      :graph-name="graphName"
      :pre-selected-query="preSelectedQuery"
      :selected-scale="selectedScale"
      :scaled-queries-set="scaledQueriesSet"
    />
  </div>
</template>
<script>
import QueriesTable from "./Queries.vue";
import ScaleSelector from "@/components/Selector.vue";

export default {
  name: "GraphTab",

  components: { ScaleSelector, QueriesTable },

  props: {
    graphName: String,

    typedQueriesSet: {
      type: Array,
      required: true
    },

    preSelectedQuery: {
      type: String,
      required: false
    },

    preSelectedScale: {
      type: Number,
      required: false
    }
  },

  data() {
    return {
      scales: [],
      selectedScale: null
    };
  },

  created() {
    // TODO: when we decide to allow navigation between queryTypes of the same graphType,
    // the implementation here needs to change. At the moment, the scales are the unique set of
    // those available on wach queryType
    const scales = [];
    this.typedQueriesSet.forEach(typedQuerySet => {
      typedQuerySet.scales.forEach(scale => scales.push(scale.value));
    });
    this.scales = [...new Set(scales.sort())];

    this.selectedScale = this.preSelectedScale || this.scales[0];
  },

  computed: {
    scaledQueriesSet() {
      // TODO: when we decide to allow navigation between queryTypes of the same graphType,
      // the implementation here needs to change. At the moment, we're combining the queries
      // contained within all queryTypes
      const scaledQueriesSet = [];
      this.typedQueriesSet.forEach(typedQuerySet => {
        typedQuerySet.scales
          .filter(scale => scale.value === this.selectedScale)[0]
          .queries.forEach(query => scaledQueriesSet.push(query));
      });
      scaledQueriesSet.sort((a, b) => a.value > b.value ? 1 : -1)
      return scaledQueriesSet;
    }
  }
};
</script>

<style scoped lang="scss">
@import "./src/assets/css/variables.scss";

.queries-action-bar {
  height: 39px;

  border-bottom: 1px solid $color-light-border;

  align-items: center;
  display: flex;
  justify-content: flex-end;
  margin-top: -$margin-default;
  margin-right: -$margin-default;
  margin-bottom: $margin-default;
  margin-left: -$margin-default;

  .action-item {
    padding-right: $padding-default;
  }
}
</style>
