import { RouteRecordRaw } from 'vue-router';
import Home from './pages/Home.vue';
import Article from './pages/Article.vue';
import Register from './pages/Register.vue';

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
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
  }
];

export default routes; 