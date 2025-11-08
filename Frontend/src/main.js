import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

import { library } from '@fortawesome/fontawesome-svg-core';
import { faCheck, faPenToSquare, faTrash, faUser, faUserSlash, faFile } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

library.add(faCheck, faPenToSquare, faTrash, faUser, faUserSlash, faFile);
const app = createApp(App)

app.use(router)
app.component('FontAwesomeIcon', FontAwesomeIcon);
app.mount('#app')
