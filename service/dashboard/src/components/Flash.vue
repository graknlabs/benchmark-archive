<template>
  <div class="universal-alert-wrapper">
    <b-alert
      :show="dismissCountDown"
      :variant="getVarient(type)"
      dismissible
      @dismissed="onDismiss"
      @dismiss-count-down="countDownChanged"
    >
      {{ message }}
    </b-alert>
  </div>
</template>

<script>
export default {
  name: 'Flash',

  props: {
    message: {
      type: String,
      required: true,
    },

    type: {
      type: String,
      required: true,
    },

    countDown: {
      type: Number,
      required: true,
    },
  },

  data() {
    return {
      dismissCountDown: this.countDown,
    };
  },

  methods: {
    countDownChanged(countDown) {
      this.dismissCountDown = countDown;
      if (countDown === 0) {
        this.onDismiss();
      }
    },

    getVarient(type) {
      if (type === 'error') {
        return 'danger';
      } if (['success', 'info', 'warning'].includes(type)) {
        return type;
      }
      throw Error('Invalid flash type');
    },

    onDismiss() {
      this.dismissCountDown = 0;
      this.$emit('dismiss');
    },
  },
};
</script>

<style scopped lang="scss">
$universal-alert-width: 700px;

.universal-alert-wrapper {
  width: $universal-alert-width;
  position: fixed;
  top: 0;
  left: 50%;
  margin-left: calc(-#{$universal-alert-width}/2);
  z-index: 1032;
  @extend .p-4;
}
</style>
