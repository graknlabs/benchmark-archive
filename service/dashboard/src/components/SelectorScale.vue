<template>
  <el-dropdown>
    <span class="el-dropdown-link">
      Scale
      <span>: {{ currentScale }}</span>
      <i class="el-icon-arrow-down el-icon--right" />
    </span>
    <el-dropdown-menu
      slot="dropdown"
      trigger="hover"
    >
      <el-dropdown-item
        v-for="scale in scales"
        :key="scale"
        @click.native="updateScale(scale)"
      >
        {{ scale }}
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
export default {
  props: {
    scales: {
      type: Array,
      required: true,
    },
  },

  data() {
    return {
      currentScale: this.scales[0],
    };
  },

  methods: {
    updateScale(selectedScale) {
      this.currentScale = selectedScale;
      this.$emit('scale-selected', selectedScale);
    },
  },
};
</script>
