<template>
  <div class="d-flex">
    <div class="sidebar">
      <div class="sidebar-heading">
        <img src="/assets/grakn-logo.png">
      </div>
      <b-list-group>
        <b-list-group-item
          v-for="menuItem in menuItems"
          :key="menuItem.title"
          :to="menuItem.route"
          :active="activeItem === menuItem.route"
        >
          <font-awesome-icon
            class="icon"
            :icon="menuItem.icon"
          />
          <span>{{ menuItem.title }}</span>
        </b-list-group-item>
      </b-list-group>
    </div>

    <div class="main">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',

  data() {
    return {
      activeItem: null,

      menuItems: [
        {
          title: 'Overview',
          route: '/overview',
          icon: ['fas', 'tachometer-alt'],
        },
        {
          title: 'Executions',
          route: '/executions',
          icon: ['fas', 'list-alt'],
        },
      ],
    };
  },

  watch: {
    $route(newVal) {
      this.activeItem = newVal.meta.menuIndex;
    },
  },

  mounted() {
    this.activeItem = this.$route.meta.menuIndex;
  },
};
</script>

<style scopped lang="scss" src="./style.scss" />
