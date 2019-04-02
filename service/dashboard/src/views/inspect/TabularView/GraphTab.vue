<template>
  <div>
    <el-row type="flex" justify="end">
      <scale-selector
        :scales="scales"
        :currentScale="currentScale"
        v-on:selected-scale="(scale)=>{this.currentScale=scale}">
      </scale-selector>
    </el-row>
    <queries-table v-for="scale in scales" :key="scale"
      :currentScale="scale"
      :executionSpans="executionSpans"
      :overviewQuery="overviewQuery"
      v-show="scale==currentScale"
    ></queries-table>
  </div>
</template>
<script>

import ScaleSelector from '@/components/ScaleSelector.vue';
import QueriesTable from './QueriesTable.vue';

export default {
  name: 'GraphTab',
  components: { ScaleSelector, QueriesTable },
  props: ['graph', 'executionSpans', 'overviewScale', 'overviewQuery'],
  data() {
    return {
      scales: [],
      currentScale: null,
    };
  },
  created() {
    this.scales = [...new Set(this.executionSpans.map(span => span.tags.graphScale))];
    this.scales.sort((a, b) => a - b);
    this.currentScale = (this.overviewScale) ? this.overviewScale : this.scales[0];
  },
};

</script>
