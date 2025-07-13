import {createRouter, createWebHistory} from 'vue-router'
import Home from '../view/home.vue'
import Login from '../view/login.vue'
import GroupManage from '../view/doc/group/index.vue'
import ArticleManage from '../view/doc/article/index.vue'
import ApiManage from '../view/api/manage/index.vue'
import MenuManage from '../view/portal/menu/index.vue'

const routes = [{
    path: '/',
    redirect: '/home'
},{
    path: '/home',
    name: 'Home',
    component: Home,
    redirect: '/home/group',
    children: [
        {
            path: 'group',
            name: 'GroupManage',
            component: GroupManage
        },
        {
            path: 'article',
            name: 'ArticleManage',
            component: ArticleManage
        },
        {
            path: 'api-manage',
            name: 'ApiManage',
            component: ApiManage
        },
        {
            path: 'menu-manage',
            name: 'MenuManage',
            component: MenuManage
        }
    ]
},{
    path: '/login',
    name: 'Login',
    component: Login
}]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router