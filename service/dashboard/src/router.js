import Vue from 'vue';
import Router from 'vue-router';
import Home from './views/Home.vue';
import BenchmarkClient from '@/util/BenchmarkClient';

Vue.use(Router);

const router = new Router({
  mode: 'history',
  routes: [
    {
      path: '/login',
      component: () => import('./views/Login'),
    },
    { path: '/', redirect: '/overview', meta: { requiresAuth: true } },
    {
      path: '/',
      name: 'home',
      component: Home,
      children: [
        {
          path: 'overview',
          component: () => import(/* webpackChunkName: "overview" */ './views/Overview'),
          meta: { menuIndex: '/overview', requiresAuth: true },
        },
        {
          path: 'executions',
          component: () => import(/* webpackChunkName: "executions" */ './views/Executions'),
          meta: { menuIndex: '/executions', requiresAuth: true },
        },
        {
          path: 'inspect/:executionId',
          component: () => import(/* webpackChunkName: "inspect" */ './views/Inspect'),
          meta: { menuIndex: '/executions', requiresAuth: true },
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

router.beforeEach(async (to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    try {
      await BenchmarkClient.verifyIdentity();
    } catch {
      next({ path: '/login' });
    }
  }
  next();
});


export default router;
