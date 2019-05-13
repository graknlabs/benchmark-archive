<template>
  <!-- TODO: check on why and how this condition is required -->
  <div>
    <el-row
      type="flex"
      justify="end"
      class="queries-action-bar"
    >
      <div class="action-item">
        <scale-selector
          title="Scale"
          :items="scales.map(scale => ({ text: scale, value: scale }))"
          :default-item="{ text: selectedScale, value: selectedScale }"
          @item-selected="(scale) => { selectedScale = scale; }"
        />
      </div>
    </el-row>

    <query-cards
      v-for="scale in scales"
      v-show="scale===selectedScale"
      :key="scale"
      :pre-selected-query="preSelectedQuery"
      :scaled-query-spans="scaledQueries"
    />
  </div>
</template>
<script>
import QueryCards from './Queries.vue';
import ScaleSelector from '@/components/Selector.vue';

export default {
  name: 'GraphTab',

  components: { ScaleSelector, QueryCards },

  props: {
    graphs: {
      type: Array,
      required: true,
    },

    graphType: {
      type: String,
      required: true,
    },

    querySpans: {
      type: Array,
      required: true,
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

  data() {
    return {
      scales: [],
      selectedScale: null,
    };
  },

  computed: {
    scaledQueries() {
      const graphIds = this.graphs.filter(graph => graph.scale === this.selectedScale).map(graph => graph.id);
      const scaledQueries = this.querySpans.filter(querySpan => graphIds.includes(querySpan.parentId));
      scaledQueries.sort((a, b) => (a.value > b.value ? 1 : -1));
      return scaledQueries;
    },
  },

  created() {
    this.scales = [...new Set(this.graphs.map(graph => graph.scale))].sort((a, b) => a - b);
    this.selectedScale = this.preSelectedScale || this.scales[0];
  },
};
</script>

<style scoped lang="scss">
@import "./src/assets/css/variables.scss";

.queries-action-bar {
  height: 39px;

  border-bottom: 1px solid $color-border-light;

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
