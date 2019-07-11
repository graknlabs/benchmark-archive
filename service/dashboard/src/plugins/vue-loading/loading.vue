<template>
  <transition
    name="loading-fade"
    @after-leave="handleAfterLeave"
  >
    <div
      v-show="visible"
      :style="{ backgroundColor: background || '' }"
      :class="[customClass, { 'is-fullscreen': fullscreen }]"
      class="loading-mask"
    >
      <b-spinner
        label="Spinning"
        class="centered"
      />
    </div>
  </transition>
</template>

<script>
import { BSpinner } from 'bootstrap-vue';

export default {
  components: { BSpinner },
  data() {
    return {
      text: 'cfcvf',
      background: null,
      fullscreen: true,
      visible: false,
      customClass: '',
    };
  },

  methods: {
    handleAfterLeave() {
      this.$emit('after-leave');
    },

    setText(text) {
      this.text = text;
    },
  },
};
</script>

<style lang="scss">
.loading-mask {
  position: absolute;
  z-index: 20;
  background-color: rgba(255, 255, 255, 0.9);
  margin: 0;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  transition: opacity 0.4s;
  &.is-fullscreen {
    position: fixed;
  }
}

.loading-parent--relative {
  position: relative !important;
}
.loading-parent--hidden {
  overflow: hidden !important;
}
</style>
