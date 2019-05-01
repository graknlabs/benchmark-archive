<template>
  <div>
    <el-row
      type="flex"
      justify="end"
    >
      <scale-selector
        title="Scale"
        :items="scales.map(scale => ({ text: scale, value: scale }))"
        :defaultItem="{ text: selectedScale, value: selectedScale }"
        @item-selected="this.selectedScale = scale;"
      />
    </el-row>
    <queries-table
      v-for="scale in scales"
      v-show="scale==selectedScale"
      :key="scale"
      :execution-spans="spans"
      :overview-query="preSelectedQuery"
      :current-scale="scale"
    />
  </div>
</template>
<script>

import QueriesTable from './QueriesTable.vue';
import ScaleSelector from '@/components/Selector.vue';


export default {
  name: 'GraphTab',

  components: { ScaleSelector, QueriesTable },

  props: {
    spans: {
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
    },
  },

  data() {
    return {
      scales: [],
      selectedScale: null,
    };
  },

  created() {
    this.scales = [
      ...new Set(this.spans.map(span => span.tags.graphScale)),
    ].sort((a, b) => a - b);
    this.selectedScale = this.preSelectedScale || this.scales[0];
  },
};

</script>
