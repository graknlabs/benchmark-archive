<template>
  <el-dropdown>
    <span class="el-dropdown-link">
      Scale
      <span :v-if="currentItem">: </span>
      <span>{{ currentItem }}</span>
      <i class="el-icon-arrow-down el-icon--right" />
    </span>
    <el-dropdown-menu
      slot="dropdown"
      trigger="hover"
    >
      <el-dropdown-item
        v-for="item in items"
        :key="item"
        @click.native="updateItem(item.value)"
      >
        {{ item.text }}
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
      type: Object,
      required: false
    }
  },

  data() {
    return {
      currentItem: this.defaultItem.text,
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
