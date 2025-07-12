import { RouteRecordRaw } from 'vue-router';
import Home from './pages/Home.vue';
import Article from './pages/Article.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/article',
    name: 'Article',
    component: Article,
  }
];

export default routes; 