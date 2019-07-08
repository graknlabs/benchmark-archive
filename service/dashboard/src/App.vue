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
@import "./src/assets/css/global";

#app {
  font-family: "Roboto", sans-serif;
  height: 100vh;
}

.el-tooltip__popper,
.el-message-box {
  font-family: "Roboto", sans-serif;
}
</style>
