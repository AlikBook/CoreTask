import { createWebHistory, createRouter } from "vue-router";

import Home from "./views/Home.vue";
import Dashboard from "./views/Dashboard.vue";
import Task_organizer from "./views/Task_organizer.vue";

const routes = [
    {
        path: "/", 
        component: Home
    },
    {
        path: "/dashboard",
        component: Dashboard
    },
    {
        path: "/tasks",
        component: Task_organizer
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;