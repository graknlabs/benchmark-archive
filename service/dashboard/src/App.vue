<template>
  <div id="app">
    <flash
      v-if="flashOptions"
      :message="flashOptions.message"
      :count-down="flashOptions.countDown"
      :type="flashOptions.type"
      @dismiss="() => flashOptions = null"
    />
    <router-view />
  </div>
</template>

<script>
import Flash from './components/Flash';

export default {
  name: 'App',

  components: { Flash },

  data() {
    return {
      flashOptions: null,
    };
  },

  created() {
    this.$root.$on('show:flash', (options) => { this.flashOptions = { countDown: 7, ...options }; });
  },
};
</script>

<style lang="scss">
#app {
  font-family: "Roboto", sans-serif;
  height: 100vh;
}
</style>
