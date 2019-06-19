import Vue from 'vue';
import Router from 'vue-router';
import Home from './views/Home.vue';

Vue.use(Router);

export default new Router({
  mode: 'history',
  routes: [
    { path: '/', redirect: '/overview' },
    {
      path: '/',
      name: 'home',
      component: Home,
      children: [
        {
          path: 'overview',
          component: () => import(/* webpackChunkName: "overview" */ './views/Overview'),
          meta: { menuIndex: '/overview' },
        },
        {
          path: 'executions',
          component: () => import(/* webpackChunkName: "executions" */ './views/Executions'),
          meta: { menuIndex: '/executions' },
        },
        {
          path: 'inspect/:executionId',
          component: () => import(/* webpackChunkName: "inspect" */ './views/Inspect'),
          meta: { menuIndex: '/executions' },
        },
      ],
    },
    // {
    //   path: "/about",
    //   name: "about",
    //   // route level code-splitting
    //   // this generates a separate chunk (about.[hash].js) for this route
    //   // which is lazy-loaded when the route is visited.
    //   component: () =>
    //     import(/* webpackChunkName: "about" */ "./views/About.vue")
    // }
  ],
});
