<template>
  <el-dropdown>
    <span class="el-dropdown-link">
      Scale
      <span>: {{ currentItem }}</span>
      <i class="el-icon-arrow-down el-icon--right" />
    </span>
    <el-dropdown-menu
      slot="dropdown"
      trigger="hover"
    >
      <el-dropdown-item
        v-for="item in items"
        :key="item"
        @click.native="updateItem(item)"
      >
        {{ item }}
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
export default {
  props: {
    items: {
      type: Array,
      required: true,
    },
    defaultItem: {
      type: [String, Number],
      required: false
    }
  },

  data() {
    return {
      currentItem: this.defaultItem,
    };
  },

  methods: {
    updateItem(selectedItem) {
      this.currentItem = selectedItem;
      this.$emit('item-selected', selectedItem);
    },
  },
};
</script>
