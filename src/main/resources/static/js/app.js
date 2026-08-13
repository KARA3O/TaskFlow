const auth = document.getElementById("auth");
const dashboard = document.getElementById("dashboard");
const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");
const loginTab = document.getElementById("loginTab");
const registerTab = document.getElementById("registerTab");
const authMessage = document.getElementById("authMessage");

function getToken() {
    return localStorage.getItem("taskflow_token");
}

function showDashboard() {
    auth.classList.add("hidden");
    dashboard.classList.remove("hidden");
    loadCurrentUser();
    loadTasks();
}

function showAuth() {
    dashboard.classList.add("hidden");
    auth.classList.remove("hidden");
}

loginTab.addEventListener("click", () => {
    loginTab.classList.add("active");
    registerTab.classList.remove("active");
    loginForm.classList.remove("hidden");
    registerForm.classList.add("hidden");
});

registerTab.addEventListener("click", () => {
    registerTab.classList.add("active");
    loginTab.classList.remove("active");
    registerForm.classList.remove("hidden");
    loginForm.classList.add("hidden");
});

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            email: document.getElementById("loginEmail").value,
            password: document.getElementById("loginPassword").value
        })
    });

    const data = await response.json();

    if (!response.ok) {
        authMessage.textContent = data.error || "Login failed";
        return;
    }

    localStorage.setItem("taskflow_token", data.token);
    showDashboard();
});

registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const response = await fetch("/api/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            username: document.getElementById("registerUsername").value,
            email: document.getElementById("registerEmail").value,
            password: document.getElementById("registerPassword").value
        })
    });

    const data = await response.json();

    if (!response.ok) {
        authMessage.textContent = data.error || "Registration failed";
        return;
    }

    authMessage.textContent = "Account created. You can now log in.";
    loginTab.click();
});

async function api(url, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {}),
        "Authorization": `Bearer ${getToken()}`
    };

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("taskflow_token");
        showAuth();
        throw new Error("Session expired");
    }

    return response;
}

async function loadCurrentUser() {
    const response = await api("/api/users/me");
    const user = await response.json();
    document.getElementById("welcome").textContent =
        `Welcome, ${user.username} (${user.role})`;
}

async function loadTasks() {
    const response = await api("/api/tasks");
    const tasks = await response.json();
    const container = document.getElementById("tasks");

    container.innerHTML = tasks.length
        ? tasks.map(task => `
            <article class="task">
                <h3>${escapeHtml(task.title)}</h3>
                <p>${escapeHtml(task.description)}</p>
                <small>Status: ${task.status} | Due: ${task.dueDate}</small>
            </article>
        `).join("")
        : "<p class='muted'>No tasks yet.</p>";
}

document.getElementById("taskForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const response = await api("/api/tasks", {
        method: "POST",
        body: JSON.stringify({
            title: document.getElementById("taskTitle").value,
            description: document.getElementById("taskDescription").value,
            dueDate: document.getElementById("taskDueDate").value
        })
    });

    if (!response.ok) {
        const data = await response.json();
        document.getElementById("taskMessage").textContent =
            data.error || "Could not create task";
        return;
    }

    event.target.reset();
    loadTasks();
});

document.getElementById("logoutButton").addEventListener("click", () => {
    localStorage.removeItem("taskflow_token");
    showAuth();
});

function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value;
    return div.innerHTML;
}

if (getToken()) {
    showDashboard();
}
