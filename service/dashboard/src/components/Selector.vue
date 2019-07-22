<template>
  <div>
    <b-dropdown
      :text="getSelectorText()"
      size="sm"
      variant="primary"
      right
    >
      <b-dropdown-item
        v-for="item in items"
        :key="item.value"
        @click.native="updateItem(item)"
      >
        <span>{{ item.text }}</span>
      </b-dropdown-item>
    </b-dropdown>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      required: true,
    },

    items: {
      type: Array,
      required: true,
    },

    defaultItem: {
      type: Object,
      required: false,
      default: null,
    },
  },

  data() {
    return {
      selectedItem: this.defaultItem ? this.defaultItem.text : undefined,
    };
  },

  methods: {
    getSelectorText() {
      let text = this.title;
      if (this.selectedItem) {
        text += `: ${this.selectedItem}`;
      }
      return text;
    },

    updateItem(selectedItem) {
      this.selectedItem = selectedItem.text;
      this.$emit('update:selected-item', selectedItem.value);
    },
  },
};
</script>
