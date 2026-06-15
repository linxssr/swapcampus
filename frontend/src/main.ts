import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import { pinia } from './stores';
import { useAuthStore } from './stores/modules/auth';
import './assets/styles/index.css';

const app = createApp(App);

app.use(pinia);
app.use(router);
app.use(ElementPlus);

const authStore = useAuthStore();
if (authStore.token && !authStore.userInfo) {
  authStore.fetchUserInfo().catch(() => {
    authStore.logout();
  });
}

app.mount('#app');
