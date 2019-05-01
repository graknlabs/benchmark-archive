<template>
  <div>
    <el-row type="flex" justify="end" class="queries-action-bar">
      <div class="action-item">
        <scale-selector
          title="Scale"
          :items="scales.map(scale => ({ text: scale, value: scale }))"
          :defaultItem="{ text: selectedScale, value: selectedScale }"
          @item-selected="this.selectedScale = scale;"
        />
      </div>
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
import QueriesTable from "./QueriesTable.vue";
import ScaleSelector from "@/components/Selector.vue";

export default {
  name: "GraphTab",

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
    }
  },

  data() {
    return {
      scales: [],
      selectedScale: null
    };
  },

  created() {
    this.scales = [
      ...new Set(this.spans.map(span => span.tags.graphScale))
    ].sort((a, b) => a - b);
    this.selectedScale = this.preSelectedScale || this.scales[0];
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

