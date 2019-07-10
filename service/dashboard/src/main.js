import Vue from 'vue';
import BootstrapVue from 'bootstrap-vue';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faGithub } from '@fortawesome/free-brands-svg-icons';
import { faTachometerAlt, faListAlt } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import App from './App.vue';
import router from './router';
import './util/filters';
import '@/assets/css/global.scss';

library.add(faGithub, faTachometerAlt, faListAlt);

Vue.component('font-awesome-icon', FontAwesomeIcon);
Vue.use(BootstrapVue);

Vue.config.productionTip = false;

new Vue({
  router,
  render: h => h(App),
}).$mount('#app');
