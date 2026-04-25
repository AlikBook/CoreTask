import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api/';
const VIEWER_KEY_STORAGE_KEY = 'coretask_viewer_key';

const buildViewerKey = () => {
    if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
        return `viewer-${crypto.randomUUID()}`;
    }

    return `viewer-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
};

const getViewerKey = () => {
    const existing = localStorage.getItem(VIEWER_KEY_STORAGE_KEY);
    if (existing) {
        return existing;
    }

    const created = buildViewerKey();
    localStorage.setItem(VIEWER_KEY_STORAGE_KEY, created);
    return created;
};

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

api.interceptors.request.use((config) => {
    const nextConfig = { ...config };
    nextConfig.headers = nextConfig.headers || {};
    nextConfig.headers['X-Viewer-Key'] = getViewerKey();
    return nextConfig;
});

export default {
    // Dashboard
    getDashboard() {
        return api.get('/dashboard');
    },
    getDashboardHistory() {
        return api.get('/dashboard/history');
    },

    // Tasks
    getAllTasks() {
        return api.get('/tasks');
    },
    getTaskById(id) {
        return api.get(`/tasks/${id}`);
    },
    createTask(task) {
        return api.post('/tasks', task);
    },
    updateTask(id, task) {
        return api.put(`/tasks/${id}`, task);
    },
    deleteTask(id) {
        return api.delete(`/tasks/${id}`);
    },

    // Workers
    getAllWorkers() {
        return api.get('/workers');
    },
    getWorkerById(id) {
        return api.get(`/workers/${id}`);
    },
    createWorker(worker) {
        return api.post('/workers', worker);
    },
    updateWorker(id, worker) {
        return api.put(`/workers/${id}`, worker);
    },
    deleteWorker(id) {
        return api.delete(`/workers/${id}`);
    },

    // Statuses
    getAllStatuses() {
        return api.get('/statuses');
    },
    getStatusById(id) {
        return api.get(`/statuses/${id}`);
    },
    createStatus(status) {
        return api.post('/statuses', status);
    },
    updateStatus(id, status) {
        return api.put(`/statuses/${id}`, status);
    },
    deleteStatus(id, replacementId = null) {
        const params = replacementId ? { replacementId } : {};
        return api.delete(`/statuses/${id}`, { params });
    },

    // Categories
    getAllCategories() {
        return api.get('/categories');
    },
    getCategoryById(id) {
        return api.get(`/categories/${id}`);
    },
    createCategory(category) {
        return api.post('/categories', category);
    },
    updateCategory(id, category) {
        return api.put(`/categories/${id}`, category);
    },
    deleteCategory(id, replacementId = null) {
        const params = replacementId ? { replacementId } : {};
        return api.delete(`/categories/${id}`, { params });
    },

    resetMyData() {
        return api.post('/viewer/reset');
    }
};
