<template>
    <div class="home-container">
        <div class="hero-section">
            <h1 class="main-title">Core Task</h1>
            <p class="subtitle">Manage your tasks efficiently</p>
        </div>

        <div class="features-grid">
            <div class="feature-card">
                <h3>Task Management</h3>
                <p>Create, update, and organize tasks easily. Assign workers, set categories, and track status.</p>
            </div>

            <div class="feature-card">
                <h3>Worker Assignment</h3>
                <p>Manage workers and assign them to tasks.</p>
            </div>

            <div class="feature-card">
                <h3>Real-time Dashboard</h3>
                <p>View statistics about your tasks and workers. Track task status and categories in real-time.</p>
            </div>

            <div class="feature-card">
                <h3>Microservices Architecture</h3>
                <p>
                Users can create and manage tasks, workers, statuses, and categories. While traditional database design relies on foreign keys to maintain relationships and handle cascade operations, our gRPC-based architecture enables robust CRUD functionality without concerns about cascade deletes or updates, ensuring flexibility and data integrity across services.
                </p>
            </div>

            <div class="feature-card">
                <h3>API Gateway</h3>
                <p>
                    The API Gateway serves as a unified entry point for all client requests, efficiently routing traffic to the appropriate backend services. It centralizes authentication, request validation, and CORS management, while providing enhanced security and scalability. This architecture simplifies client interactions and enables seamless integration across microservices.
                </p>
            </div>

            <div class="feature-card">
                <h3>Docker & Cloud Native</h3>
                <p>
                    All services are containerized using Docker, enabling consistent and reliable deployments across any environment. Our architecture is cloud-native ready, supporting orchestration with Docker Compose and seamless migration to Kubernetes. This approach ensures scalability, portability, and simplified management for modern cloud platforms.
                </p>
            </div>
        </div>
    </div>
</template>

<script setup>
</script>

<style scoped>
.home-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 40px 20px;
}

.hero-section {
    text-align: center;
    margin-bottom: 60px;
    padding: 60px 20px;
    background: #667eea ;
    border-radius: 20px;
    color: white;
}

.main-title {
    font-size: 3.5rem;
    margin-bottom: 20px;
    font-weight: 700;
}

.subtitle {
    font-size: 1.3rem;
    opacity: 0.95;
    max-width: 600px;
    margin: 0 auto;
}

.features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 30px;
    margin-bottom: 60px;
}

.feature-card {
    background: white;
    padding: 30px;
    border-radius: 15px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 15px rgba(0, 0, 0, 0.15);
}

.feature-icon {
    font-size: 3rem;
    margin-bottom: 15px;
}

.feature-card h3 {
    color: #667eea;
    margin-bottom: 15px;
    font-size: 1.5rem;
}

.feature-card p {
    color: #666;
    line-height: 1.6;
}

.tech-stack {
    text-align: center;
    margin-bottom: 60px;
}

.tech-stack h2 {
    margin-bottom: 30px;
    color: #333;
    font-size: 2rem;
}

.tech-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    justify-content: center;
}

.badge {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 10px 25px;
    border-radius: 25px;
    font-weight: 600;
    font-size: 1rem;
}

.cta-section {
    display: flex;
    gap: 20px;
    justify-content: center;
    margin-top: 50px;
}

.btn-primary, .btn-secondary {
    padding: 15px 40px;
    border-radius: 10px;
    text-decoration: none;
    font-weight: 600;
    font-size: 1.1rem;
    transition: all 0.3s ease;
}

.btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
    transform: translateY(-2px);
}

@media (max-width: 768px) {
    .main-title {
        font-size: 2.5rem;
    }
    
    .cta-section {
        flex-direction: column;
    }
    
    .btn-primary, .btn-secondary {
        text-align: center;
    }
}
</style>