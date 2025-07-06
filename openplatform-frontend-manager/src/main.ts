import { createApp } from 'vue'
import './style.css'
import 'ant-design-vue/dist/reset.css';
import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue';
import * as Icons from '@ant-design/icons-vue';
import VxeUITable from 'vxe-table';
import 'vxe-table/lib/style.css';
import VxeUIAll from 'vxe-pc-ui';
import 'vxe-pc-ui/lib/style.css';
import './assets/styles/theme.css'

const app = createApp(App);

app.use(Antd).use(router).use(VxeUIAll).use(VxeUITable).mount('#app');

const icons = Icons;
for (const i in icons) {
    app.component(i, icons[i]);
}