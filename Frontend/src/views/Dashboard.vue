<template>
    <div class="dashboard-container">
        <h1 class="page-title">📊 Dashboard</h1>

        <div v-if="loading" class="loading">Loading dashboard...</div>
        <div v-else-if="error" class="error">{{ error }}</div>

        <div v-else>
            <!-- Statistics Cards -->
            <div class="stats-grid">
                <div class="stat-card workers">
                    <div class="stat-icon">👥</div>
                    <div class="stat-content">
                        <h3>Total Workers</h3>
                        <p class="stat-number">{{ dashboardData.totalWorkers }}</p>
                        <span class="stat-label">via gRPC</span>
                    </div>
                </div>

                <div class="stat-card tasks">
                    <div class="stat-icon">📋</div>
                    <div class="stat-content">
                        <h3>Total Tasks</h3>
                        <p class="stat-number">{{ dashboardData.totalTasks }}</p>
                        <span class="stat-label">Active tasks</span>
                    </div>
                </div>
            </div>

            <!-- Tasks by Status -->
            <div class="section-card">
                <h2 class="section-title">Tasks by Status</h2>
                <div class="chart-container">
                    <div v-if="Object.keys(dashboardData.tasksByStatus || {}).length === 0" class="no-data">
                        No status data available
                    </div>
                    <div v-else class="status-bars">
                        <div v-for="(count, status) in dashboardData.tasksByStatus" :key="status" class="status-bar">
                            <div class="status-label">{{ status }}</div>
                            <div class="bar-container">
                                <div class="bar" :style="{ width: getPercentage(count, dashboardData.totalTasks) + '%' }"></div>
                                <span class="bar-count">{{ count }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Tasks by Category -->
            <div class="section-card">
                <h2 class="section-title">Tasks by Category</h2>
                <div class="chart-container">
                    <div v-if="Object.keys(dashboardData.tasksByCategory || {}).length === 0" class="no-data">
                        No category data available
                    </div>
                    <div v-else class="category-grid">
                        <div v-for="(count, category) in dashboardData.tasksByCategory" :key="category" class="category-item">
                            <div class="category-circle" :style="{ background: getRandomGradient() }">
                                <span class="category-count">{{ count }}</span>
                            </div>
                            <div class="category-name">{{ category }}</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Recent History -->
            <div class="section-card">
                <h2 class="section-title">Recent Activity</h2>
                <div class="history-container">
                    <div v-if="!dashboardData.recentHistory || dashboardData.recentHistory.length === 0" class="no-data">
                        No recent activity
                    </div>
                    <div v-else class="history-list">
                        <div v-for="item in dashboardData.recentHistory" :key="item.historyId" class="history-item">
                            <div class="history-icon" :class="getActionClass(item.action)">
                                {{ getActionIcon(item.action) }}
                            </div>
                            <div class="history-content">
                                <div class="history-action">
                                    <span class="action-type">{{ item.action }}</span>
                                    <span class="task-name">{{ item.taskName }}</span>
                                </div>
                                <div class="history-details">{{ item.details }}</div>
                                <div class="history-time">{{ formatDate(item.timestamp) }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '../services/api';

const dashboardData = ref({
    totalWorkers: 0,
    totalTasks: 0,
    tasksByStatus: {},
    tasksByCategory: {},
    recentHistory: []
});
const loading = ref(true);
const error = ref(null);

const loadDashboard = async () => {
    try {
        loading.value = true;
        error.value = null;
        const response = await api.getDashboard();
        dashboardData.value = response.data;
    } catch (err) {
        error.value = 'Failed to load dashboard data. Make sure the backend is running.';
        console.error('Dashboard error:', err);
    } finally {
        loading.value = false;
    }
};

const getPercentage = (count, total) => {
    if (total === 0) return 0;
    return Math.round((count / total) * 100);
};

const getRandomGradient = () => {
    const gradients = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        'linear-gradient(135deg, #30cfd0 0%, #330867 100%)'
    ];
    return gradients[Math.floor(Math.random() * gradients.length)];
};

const getActionIcon = (action) => {
    const icons = {
        'CREATE': '✅',
        'UPDATE': '✏️',
        'DELETE': '🗑️',
        'CREATE_WORKER': '👤',
        'DELETE_WORKER': '👋'
    };
    return icons[action] || '📝';
};

const getActionClass = (action) => {
    if (action.includes('CREATE')) return 'action-create';
    if (action.includes('UPDATE')) return 'action-update';
    if (action.includes('DELETE')) return 'action-delete';
    return '';
};

const formatDate = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleString();
};

onMounted(() => {
    loadDashboard();
});
</script>

<style scoped>
.dashboard-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 30px 20px;
}

.page-title {
    font-size: 2.5rem;
    margin-bottom: 30px;
    color: #333;
}

.loading, .error {
    text-align: center;
    padding: 60px 20px;
    font-size: 1.2rem;
}

.error {
    color: #f5576c;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 25px;
    margin-bottom: 40px;
}

.stat-card {
    background: white;
    border-radius: 15px;
    padding: 30px;
    display: flex;
    align-items: center;
    gap: 20px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
}

.stat-card:hover {
    transform: translateY(-5px);
}

.stat-card.workers {
    border-left: 5px solid #667eea;
}

.stat-card.tasks {
    border-left: 5px solid #43e97b;
}

.stat-icon {
    font-size: 3rem;
}

.stat-content h3 {
    margin: 0 0 10px 0;
    color: #666;
    font-size: 1rem;
    font-weight: 600;
}

.stat-number {
    font-size: 3rem;
    font-weight: 700;
    margin: 0;
    color: #333;
}

.stat-label {
    font-size: 0.85rem;
    color: #999;
}

.section-card {
    background: white;
    border-radius: 15px;
    padding: 30px;
    margin-bottom: 30px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.section-title {
    margin: 0 0 25px 0;
    color: #333;
    font-size: 1.5rem;
}

.no-data {
    text-align: center;
    padding: 40px;
    color: #999;
}

.status-bars {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.status-bar {
    display: flex;
    align-items: center;
    gap: 15px;
}

.status-label {
    min-width: 120px;
    font-weight: 600;
    color: #555;
}

.bar-container {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 10px;
}

.bar {
    height: 30px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 15px;
    transition: width 0.5s ease;
    min-width: 30px;
}

.bar-count {
    font-weight: 600;
    color: #667eea;
}

.category-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 25px;
}

.category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
}

.category-circle {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 2rem;
    font-weight: 700;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.category-name {
    font-weight: 600;
    color: #555;
    text-align: center;
}

.history-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
    max-height: 500px;
    overflow-y: auto;
}

.history-item {
    display: flex;
    gap: 15px;
    padding: 15px;
    background: #f9f9f9;
    border-radius: 10px;
    transition: background 0.2s;
}

.history-item:hover {
    background: #f0f0f0;
}

.history-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.2rem;
    flex-shrink: 0;
}

.action-create {
    background: #43e97b;
    color: white;
}

.action-update {
    background: #4facfe;
    color: white;
}

.action-delete {
    background: #f5576c;
    color: white;
}

.history-content {
    flex: 1;
}

.history-action {
    margin-bottom: 5px;
}

.action-type {
    background: #667eea;
    color: white;
    padding: 3px 10px;
    border-radius: 5px;
    font-size: 0.75rem;
    font-weight: 600;
    margin-right: 8px;
}

.task-name {
    font-weight: 600;
    color: #333;
}

.history-details {
    color: #666;
    font-size: 0.9rem;
    margin-bottom: 5px;
}

.history-time {
    color: #999;
    font-size: 0.85rem;
}

@media (max-width: 768px) {
    .status-bar {
        flex-direction: column;
        align-items: flex-start;
    }
    
    .status-label {
        min-width: auto;
    }
    
    .bar-container {
        width: 100%;
    }
}
</style>