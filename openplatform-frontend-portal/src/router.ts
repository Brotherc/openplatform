import { RouteRecordRaw } from 'vue-router';
import Home from './pages/Home.vue';
import Article from './pages/Article.vue';
import Login from './pages/Login.vue';
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
    path: '/login',
    name: 'Login',
    component: Login,
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
  }
];

export default routes; 