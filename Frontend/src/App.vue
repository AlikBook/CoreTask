<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div id="app">
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-brand">
          <span class="brand-text">Task Manager</span>
        </div>
        
        <div class="nav-links">
          <RouterLink to="/" class="nav-link">
            Home
          </RouterLink>
          <RouterLink to="/dashboard" class="nav-link">
            Dashboard
          </RouterLink>
          <RouterLink to="/tasks" class="nav-link">
            Tasks
          </RouterLink>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <RouterView />
    </main>

    
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Montserrat";
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.navbar {
  background: #667eea ;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
}

.brand-icon {
  font-size: 2rem;
}

.brand-text {
  letter-spacing: 0.5px;
}

.nav-links {
  display: flex;
  gap: 10px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.nav-link.router-link-active {
  background: white;
  color: #667eea;
}

.link-icon {
  font-size: 1.2rem;
}

/* Main Content */
.main-content {
  flex: 1;
  width: 100%;
}

/* Footer */
.footer {
  background: #2c3e50;
  color: white;
  text-align: center;
  padding: 25px 20px;
  margin-top: 50px;
}

.footer p {
  font-size: 0.95rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .nav-container {
    flex-direction: column;
    height: auto;
    padding: 15px 20px;
    gap: 15px;
  }

  .nav-brand {
    font-size: 1.2rem;
  }

  .brand-icon {
    font-size: 1.5rem;
  }

  .nav-links {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }

  .nav-link {
    font-size: 0.9rem;
    padding: 8px 16px;
  }
}
</style>