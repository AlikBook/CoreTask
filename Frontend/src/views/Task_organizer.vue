<template>
    <div class="task-organizer-container">
        <h1 class="page-title">Task Organizer</h1>

        <!-- Action Buttons -->
        <div class="action-bar">
            <button @click="openCreateTaskModal" class="btn-primary">
                New Task +
            </button>
            <button @click="openWorkerModal" class="btn-secondary">
                Manage Workers
            </button>
            <button @click="loadTasks" class="btn-refresh">
                Refresh
            </button>

            <button @click="openStatusModal" class="btn-status-creation">
                Manage Status
            </button>

            <button @click="openCategoryModal" class="btn-category-creation">
                Manage Category
            </button>
        <!-- Create Status Modal -->
        <div v-if="showStatusModal" class="modal-overlay" @click.self="closeStatusModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Create / Delete Status</h2>
                    <button @click="closeStatusModal" class="close-btn">✖</button>
                </div>
                <div class="modal-body">
                    <div class="status-create-section">
                        <h3>Add New Status</h3>
                        <div class="form-group">
                            <label>Status Name</label>
                            <input v-model="newStatusName" type="text" placeholder="Enter status name" />
                        </div>
                        <button @click="createStatus" class="btn-primary">Create Status</button>
                    </div>
                    <hr class="divider" />
                    <div class="form-group">
                        <label>Delete Existing Statuses</label>
                        <div v-if="statuses.length === 0" class="no-data">No statuses found.</div>
                        <div v-else class="status-list">
                            <div v-for="status in statuses" :key="status.statusId" class="status-item">
                                <span class="status-name">{{ status.statusName }}</span>
                                <button @click="deleteStatus(status.statusId)" class="btn-delete-status">Delete</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button @click="closeStatusModal" class="btn-cancel">Close</button>
                </div>
            </div>
        </div>

        <!-- Create Category Modal -->
        <div v-if="showCategoryModal" class="modal-overlay" @click.self="closeCategoryModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Create / Delete Category</h2>
                    <button @click="closeCategoryModal" class="close-btn">✖</button>
                </div>
                <div class="modal-body">
                    <div class="category-create-section">
                        <h3>Add New Category</h3>
                        <div class="form-group">
                            <label>Category Name</label>
                            <input v-model="newCategoryName" type="text" placeholder="Enter category name" />
                        </div>
                        <button @click="createCategory" class="btn-primary">Create Category</button>
                    </div>
                    <hr class="divider" />
                    <div class="form-group">
                        <label>Delete Existing Categories</label>
                        <div v-if="categories.length === 0" class="no-data">No categories found.</div>
                        <div v-else class="category-list">
                            <div v-for="category in categories" :key="category.categoryId" class="category-item">
                                <span class="category-name">{{ category.categoryName }}</span>
                                <button @click="deleteCategory(category.categoryId)" class="btn-delete-category">Delete</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button @click="closeCategoryModal" class="btn-cancel">Close</button>
                </div>
            </div>
        </div>
        </div>

        <div v-if="loading" class="loading">Loading tasks...</div>
        <div v-else-if="error" class="error">{{ error }}</div>

        <div v-else class="tasks-table-container">
            <div v-if="tasks.length === 0" class="no-data">
                No tasks found. Create your first task!
            </div>
            <table v-else class="tasks-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Task Name</th>
                        <th>Description</th>
                        <th>Due Date</th>
                        <th>Worker</th>
                        <th>Status</th>
                        <th>Category</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="task in tasks" :key="task.taskId">
                        <td>{{ task.taskId }}</td>
                        <td class="task-name">{{ task.taskName }}</td>
                        <td class="task-desc">{{ task.description || '-' }}</td>
                        <td>{{ formatDate(task.dueDate) }}</td>
                        <td>
                            <span v-if="task.workerId" class="worker-badge">
                                {{ getWorkerName(task.workerId) }}
                            </span>
                            <span v-else class="unassigned">Unassigned</span>
                        </td>
                        <td>
                            <span class="status-badge" :class="'status-' + task.statusId">
                                {{ getStatusName(task.statusId) }}
                            </span>
                        </td>
                        <td>
                            <span class="category-badge">
                                {{ getCategoryName(task.categoryId) }}
                            </span>
                        </td>
                        <td class="action-buttons">
                            <button @click="openEditTaskModal(task)" class="btn-edit" title="Edit">
                                ✏️
                            </button>
                            <button @click="deleteTask(task.taskId)" class="btn-delete" title="Delete">
                                🗑️
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Task Modal (Create/Edit) -->
        <div v-if="showTaskModal" class="modal-overlay" @click.self="closeTaskModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>{{ isEditMode ? 'Edit Task' : 'Create New Task' }}</h2>
                    <button @click="closeTaskModal" class="close-btn">✖</button>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label>Task Name *</label>
                        <input v-model="taskForm.taskName" type="text" placeholder="Enter task name" required />
                    </div>

                    <div class="form-group">
                        <label>Description</label>
                        <textarea v-model="taskForm.description" rows="3" placeholder="Enter task description"></textarea>
                    </div>

                    <div class="form-group">
                        <label>Due Date</label>
                        <input v-model="taskForm.dueDate" type="date" />
                    </div>

                    <div class="form-group">
                        <label>Worker (Optional)</label>
                        <select v-model="taskForm.workerId">
                            <option :value="null">Unassigned</option>
                            <option v-for="worker in workers" :key="worker.workerId" :value="worker.workerId">
                                {{ worker.workerName }} {{ worker.workerLastName }}
                            </option>
                        </select>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>Status *</label>
                            <select v-model="taskForm.statusId" required>
                                <option :value="null" disabled>Select status</option>
                                <option v-for="status in statuses" :key="status.statusId" :value="status.statusId">
                                    {{ status.statusName }}
                                </option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Category *</label>
                            <select v-model="taskForm.categoryId" required>
                                <option :value="null" disabled>Select category</option>
                                <option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">
                                    {{ category.categoryName }}
                                </option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button @click="closeTaskModal" class="btn-cancel">Cancel</button>
                    <button @click="saveTask" class="btn-save">{{ isEditMode ? 'Update' : 'Create' }}</button>
                </div>
            </div>
        </div>

        <!-- Worker Modal -->
        <div v-if="showWorkerModal" class="modal-overlay" @click.self="closeWorkerModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Manage Workers</h2>
                    <button @click="closeWorkerModal" class="close-btn">✖</button>
                </div>
                <div class="modal-body">
                    <!-- Create Worker Form -->
                    <div class="worker-create-section">
                        <h3>Add New Worker</h3>
                        <div class="form-row">
                            <div class="form-group">
                                <label>First Name</label>
                                <input v-model="workerForm.workerName" type="text" placeholder="First name" />
                            </div>
                            <div class="form-group">
                                <label>Last Name</label>
                                <input v-model="workerForm.workerLastName" type="text" placeholder="Last name" />
                            </div>
                        </div>
                        <button @click="createWorker" class="btn-primary">Add Worker</button>
                    </div>

                    <hr class="divider" />

                    <!-- Workers List -->
                    <div class="workers-list-section">
                        <h3>Existing Workers</h3>
                        <div v-if="workers.length === 0" class="no-data">
                            No workers found.
                        </div>
                        <div v-else class="workers-list">
                            <div v-for="worker in workers" :key="worker.workerId" class="worker-item">
                                <div class="worker-info">
                                    <span class="worker-icon">👤</span>
                                    <span class="worker-name">
                                        {{ worker.workerName }} {{ worker.workerLastName }}
                                    </span>
                                </div>
                                <button @click="deleteWorker(worker.workerId)" class="btn-delete-small">
                                    Delete
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button @click="closeWorkerModal" class="btn-cancel">Close</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
const deleteStatus = async (statusId) => {
    if (!confirm('Are you sure you want to delete this status? Tasks using this status will have it set to null.')) return;
    
    try {
        await api.deleteStatus(statusId);
        loadStatuses();
        loadTasks(); // Refresh tasks to show updated status
        alert('Status deleted successfully');
    } catch (e) {
        alert('Error deleting status: ' + (e.response?.data || e.message));
    }
};

const deleteCategory = async (categoryId) => {
    if (!confirm('Are you sure you want to delete this category? Tasks using this category will have it set to null.')) return;
    
    try {
        await api.deleteCategory(categoryId);
        loadCategories();
        loadTasks(); 
        alert('Category deleted successfully');
    } catch (e) {
        alert('Error deleting category: ' + (e.response?.data || e.message));
    }
};
const showStatusModal = ref(false);
const showCategoryModal = ref(false);
const newStatusName = ref("");
const newCategoryName = ref("");

const openStatusModal = () => {
    newStatusName.value = "";
    showStatusModal.value = true;
    loadStatuses(); 
};
const closeStatusModal = () => {
    showStatusModal.value = false;
};
const openCategoryModal = () => {
    newCategoryName.value = "";
    showCategoryModal.value = true;
    loadCategories(); // Refresh category list when opening modal
};
const closeCategoryModal = () => {
    showCategoryModal.value = false;
};

const createStatus = async () => {
    if (!newStatusName.value.trim()) {
        alert("Please enter a status name");
        return;
    }
    try {
        await api.createStatus({ statusName: newStatusName.value });
        newStatusName.value = ""; // Clear input after creation
        loadStatuses();
    } catch (e) {
        alert("Error creating status: " + (e.response?.data || e.message));
    }
};

const createCategory = async () => {
    if (!newCategoryName.value.trim()) {
        alert("Please enter a category name");
        return;
    }
    try {
        await api.createCategory({ categoryName: newCategoryName.value });
        newCategoryName.value = ""; // Clear input after creation
        loadCategories();
    } catch (e) {
        alert("Error creating category: " + (e.response?.data || e.message));
    }
};
import { ref, onMounted, computed } from 'vue';
import api from '../services/api';

const tasks = ref([]);
const workers = ref([]);
const statuses = ref([]);
const categories = ref([]);
const loading = ref(true);
const error = ref(null);

const showTaskModal = ref(false);
const showWorkerModal = ref(false);
const isEditMode = ref(false);

const taskForm = ref({
    taskId: null,
    taskName: '',
    description: '',
    dueDate: '',
    workerId: null,
    statusId: null,
    categoryId: null
});

const workerForm = ref({
    workerName: '',
    workerLastName: ''
});

// Load all data
const loadTasks = async () => {
    try {
        loading.value = true;
        error.value = null;
        const response = await api.getAllTasks();
        tasks.value = response.data;
    } catch (err) {
        if (err.response?.status === 404) {
            tasks.value = [];
        } else {
            error.value = 'Failed to load tasks';
            console.error('Error loading tasks:', err);
        }
    } finally {
        loading.value = false;
    }
};

const loadWorkers = async () => {
    try {
        const response = await api.getAllWorkers();
        workers.value = response.data;
    } catch (err) {
        if (err.response?.status === 404) {
            workers.value = [];
        } else {
            console.error('Error loading workers:', err);
        }
    }
};

const loadStatuses = async () => {
    try {
        const response = await api.getAllStatuses();
        statuses.value = response.data;
    } catch (err) {
        console.error('Error loading statuses:', err);
    }
};

const loadCategories = async () => {
    try {
        const response = await api.getAllCategories();
        categories.value = response.data;
    } catch (err) {
        console.error('Error loading categories:', err);
    }
};

// Task Modal Functions
const openCreateTaskModal = () => {
    isEditMode.value = false;
    taskForm.value = {
        taskId: null,
        taskName: '',
        description: '',
        dueDate: '',
        workerId: null,
        statusId: null,
        categoryId: null
    };
    showTaskModal.value = true;
};

const openEditTaskModal = (task) => {
    isEditMode.value = true;
    taskForm.value = {
        taskId: task.taskId,
        taskName: task.taskName,
        description: task.description || '',
        dueDate: task.dueDate || '',
        workerId: task.workerId || null,
        statusId: task.statusId || null,
        categoryId: task.categoryId || null
    };
    showTaskModal.value = true;
};

const closeTaskModal = () => {
    showTaskModal.value = false;
};

const saveTask = async () => {
    try {
        const taskData = {
            taskName: taskForm.value.taskName,
            description: taskForm.value.description,
            dueDate: taskForm.value.dueDate || null,
            workerId: taskForm.value.workerId || null,
            statusId: taskForm.value.statusId || null,
            categoryId: taskForm.value.categoryId || null
        };

        if (isEditMode.value) {
            await api.updateTask(taskForm.value.taskId, taskData);
        } else {
            await api.createTask(taskData);
        }

        closeTaskModal();
        loadTasks();
    } catch (err) {
        alert('Error saving task: ' + (err.response?.data || err.message));
        console.error('Error saving task:', err);
    }
};

const deleteTask = async (taskId) => {
    if (!confirm('Are you sure you want to delete this task?')) return;

    try {
        await api.deleteTask(taskId);
        loadTasks();
    } catch (err) {
        alert('Error deleting task: ' + (err.response?.data || err.message));
        console.error('Error deleting task:', err);
    }
};

// Worker Modal Functions
const openWorkerModal = () => {
    workerForm.value = {
        workerName: '',
        workerLastName: ''
    };
    showWorkerModal.value = true;
};

const closeWorkerModal = () => {
    showWorkerModal.value = false;
};

const createWorker = async () => {
    if (!workerForm.value.workerName || !workerForm.value.workerLastName) {
        alert('Please enter both first and last name');
        return;
    }

    try {
        await api.createWorker(workerForm.value);
        workerForm.value = { workerName: '', workerLastName: '' };
        loadWorkers();
    } catch (err) {
        alert('Error creating worker: ' + (err.response?.data || err.message));
        console.error('Error creating worker:', err);
    }
};

const deleteWorker = async (workerId) => {
    if (!confirm('Are you sure? This will unassign the worker from all tasks.')) return;

    try {
        await api.deleteWorker(workerId);
        loadWorkers();
        loadTasks(); 
    } catch (err) {
        alert('Error deleting worker: ' + (err.response?.data || err.message));
        console.error('Error deleting worker:', err);
    }
};

const getWorkerName = (workerId) => {
    const worker = workers.value.find(w => w.workerId === workerId);
    return worker ? `${worker.workerName} ${worker.workerLastName}` : 'Unknown';
};

const getStatusName = (statusId) => {
    const status = statuses.value.find(s => s.statusId === statusId);
    return status ? status.statusName : '-';
};

const getCategoryName = (categoryId) => {
    const category = categories.value.find(c => c.categoryId === categoryId);
    return category ? category.categoryName : '-';
};


//Status 

const create_status = async (status_name)=>{
    try{
        await api.createStatus(status_name)
    }catch(e){
        console.log(e.message)
    }
}

const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString();
};

onMounted(() => {
    loadTasks();
    loadWorkers();
    loadStatuses();
    loadCategories();
});
</script>

<style scoped>
/* Status/Category Create Section Styles */
.status-create-section, .category-create-section {
    margin-bottom: 30px;
}

.status-create-section h3, .category-create-section h3 {
    margin-bottom: 20px;
    color: #333;
}

/* Status/Category Modal List Styles */
.status-list, .category-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-top: 10px;
    max-height: 250px;
    overflow-y: auto;
}

.status-item, .category-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #f9f9f9;
    padding: 12px 15px;
    border-radius: 8px;
    transition: background 0.2s;
}

.status-item:hover, .category-item:hover {
    background: #f0f0f0;
}

.status-name {
    font-weight: 600;
    color: #333;
    font-size: 1rem;
}

.category-name {
    font-weight: 600;
    color: #333;
    font-size: 1rem;
}

.btn-delete-status {
    padding: 8px 16px;
    background: #f5576c;
    color: #fff;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s ease;
}

.btn-delete-status:hover {
    background: #e04354;
    transform: scale(1.05);
}

.btn-delete-category {
    padding: 8px 16px;
    background: #f5576c;
    color: #fff;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s ease;
}

.btn-delete-category:hover {
    background: #e04354;
    transform: scale(1.05);
}
.task-organizer-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 30px 20px;
}

.page-title {
    font-size: 2.5rem;
    margin-bottom: 30px;
    color: #333;
}

.action-bar {
    display: flex;
    gap: 15px;
    margin-bottom: 30px;
    flex-wrap: wrap;
}

.btn-primary, .btn-secondary, .btn-refresh {
    padding: 12px 25px;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 1rem;
}

.btn-primary {
    background: #667eea;
    color: white;
}

.btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
    background: white;
    color: #667eea;
    border: 2px solid #667eea;
}

.btn-secondary:hover {
    background: #667eea;
    color: white;
}

.btn-refresh {
    background: #43e97b;
    color: white;
}

.btn-refresh:hover {
    background: #38d66a;
}

.btn-status-creation, .btn-category-creation {
    padding: 12px 25px;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 1rem;
}

.btn-status-creation {
    background: linear-gradient(135deg, #43e97b 0%, #38d66a 100%);
    color: white;
}

.btn-status-creation:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(67, 233, 123, 0.4);
}

.btn-category-creation {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
}

.btn-category-creation:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(240, 147, 251, 0.4);
}

.loading, .error, .no-data {
    text-align: center;
    padding: 60px 20px;
    font-size: 1.2rem;
}

.error {
    color: #f5576c;
}

.tasks-table-container {
    background: white;
    border-radius: 15px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.tasks-table {
    width: 100%;
    border-collapse: collapse;
}

.tasks-table thead {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.tasks-table th {
    padding: 15px;
    text-align: left;
    font-weight: 600;
}

.tasks-table td {
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;
}

.tasks-table tbody tr:hover {
    background: #f9f9f9;
}

.task-name {
    font-weight: 600;
    color: #333;
}

.task-desc {
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.worker-badge, .status-badge, .category-badge {
    display: inline-block;
    padding: 5px 12px;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 600;
}

.worker-badge {
    background: #4facfe;
    color: white;
}

.unassigned {
    color: #999;
    font-style: italic;
}

.status-badge {
    background: #43e97b;
    color: white;
}

.category-badge {
    background: #f093fb;
    color: white;
}

.action-buttons {
    display: flex;
    gap: 8px;
}

.btn-edit, .btn-delete {
    padding: 8px 12px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 1rem;
    transition: all 0.2s ease;
}

.btn-edit {
    background: #4facfe;
}

.btn-edit:hover {
    background: #2d9cdb;
    transform: scale(1.1);
}

.btn-delete {
    background: #f5576c;
}

.btn-delete:hover {
    background: #e04354;
    transform: scale(1.1);
}

/* Modal Styles */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-content {
    background: white;
    border-radius: 15px;
    width: 90%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 25px 30px;
    border-bottom: 2px solid #f0f0f0;
}

.modal-header h2 {
    margin: 0;
    color: #333;
}

.close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #999;
}

.close-btn:hover {
    color: #333;
}

.modal-body {
    padding: 30px;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: #555;
}

.form-group input,
.form-group textarea,
.form-group select {
    width: 100%;
    padding: 12px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 1rem;
    transition: border-color 0.3s;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
    outline: none;
    border-color: #667eea;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
}

.modal-footer {
    padding: 20px 30px;
    border-top: 2px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
    gap: 15px;
}

.btn-cancel, .btn-save {
    padding: 12px 30px;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
}

.btn-cancel {
    background: #e0e0e0;
    color: #666;
}

.btn-cancel:hover {
    background: #d0d0d0;
}

.btn-save {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.btn-save:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

/* Worker Modal Specific */
.worker-create-section {
    margin-bottom: 30px;
}

.worker-create-section h3 {
    margin-bottom: 20px;
    color: #333;
}

.divider {
    border: none;
    border-top: 2px solid #f0f0f0;
    margin: 30px 0;
}

.workers-list-section h3 {
    margin-bottom: 20px;
    color: #333;
}

.workers-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-height: 300px;
    overflow-y: auto;
}

.worker-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    background: #f9f9f9;
    border-radius: 8px;
    transition: background 0.2s;
}

.worker-item:hover {
    background: #f0f0f0;
}

.worker-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.worker-icon {
    font-size: 1.5rem;
}

.worker-name {
    font-weight: 600;
    color: #333;
}

.btn-delete-small {
    padding: 8px 16px;
    background: #f5576c;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s ease;
}

.btn-delete-small:hover {
    background: #e04354;
    transform: scale(1.05);
}

@media (max-width: 768px) {
    .tasks-table {
        font-size: 0.9rem;
    }
    
    .tasks-table th,
    .tasks-table td {
        padding: 10px 5px;
    }
    
    .form-row {
        grid-template-columns: 1fr;
    }
    
    .modal-content {
        width: 95%;
    }
}
</style>
