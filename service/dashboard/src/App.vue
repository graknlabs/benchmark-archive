<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
};
</script>

<style lang="scss">
@import 'node_modules/bootstrap/scss/bootstrap';
@import 'node_modules/bootstrap-vue/src/index.scss';
@import "./src/assets/css/fonts.scss";
@import "./src/assets/css/reset.scss";
@import "./src/assets/css/base.scss";
@import "./src/assets/css/overrides.scss";

#app {
  font-family: "Roboto", sans-serif;
  height: 100vh;
}

.el-tooltip__popper,
.el-message-box {
  font-family: "Roboto", sans-serif;
}
</style>
