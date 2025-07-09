import { RouteRecordRaw } from 'vue-router';
import Home from './pages/Home.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  }
];

export default routes; 