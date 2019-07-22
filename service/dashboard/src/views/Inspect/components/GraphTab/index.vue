<template>
  <div>
    <div class="queries-action-bar">
      <p>{{ graphDescription }}</p>
      <div class="action-item">
        <scale-selector
          title="Scale"
          :items="scales.map(scale => ({ text: scale, value: scale }))"
          :default-item="{ text: selectedScale, value: selectedScale }"
          @update:selected-item="(scale) => { selectedScale = scale; }"
        />
      </div>
    </div>

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
import QueryCards from '../QueryCards';
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

    graphDescription() {
      return this.graphs[0].description;
    },
  },

  created() {
    this.scales = this.getScales();
    this.selectedScale = this.preSelectedScale || this.scales[0];
  },

  methods: {
    getScales() {
      return [...new Set(this.graphs.map(graph => graph.scale))].sort();
    },
  },
};
</script>

<style lang="scss" scopped src="./style.scss"></style>
