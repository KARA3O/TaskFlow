(() => {
    const auth = document.getElementById("auth");
    const dashboard = document.getElementById("dashboard");

    const loginForm = document.getElementById("loginForm");
    const registerForm = document.getElementById("registerForm");
    const loginTab = document.getElementById("loginTab");
    const registerTab = document.getElementById("registerTab");
    const authMessage = document.getElementById("authMessage");

    const taskForm = document.getElementById("taskForm");
    const taskFormTitle = document.getElementById("taskFormTitle");
    const taskIdField = document.getElementById("taskId");
    const taskTitleField = document.getElementById("taskTitle");
    const taskDescriptionField = document.getElementById("taskDescription");
    const taskDueDateField = document.getElementById("taskDueDate");
    const taskSubmitButton = document.getElementById("taskSubmitButton");
    const taskCancelButton = document.getElementById("taskCancelButton");
    const taskMessage = document.getElementById("taskMessage");

    const tasksContainer = document.getElementById("tasks");
    const emptyState = document.getElementById("emptyState");
    const filterButtons = Array.from(document.querySelectorAll(".filter"));

    // AI Component Elements
    const getAiPlanBtn = document.getElementById("getAiPlanBtn");
    const aiSummary = document.getElementById("aiSummary");
    const aiRecommendations = document.getElementById("aiRecommendations");

    const STATUS_LABELS = {
        PENDING: "Pending",
        IN_PROGRESS: "In progress",
        COMPLETED: "Completed"
    };

    let allTasks = [];
    let activeFilter = "ALL";

    function getToken() {
        return localStorage.getItem("taskflow_token");
    }

    function setMessage(element, text, kind) {
        element.textContent = text || "";
        element.classList.remove("error", "success");
        if (kind) {
            element.classList.add(kind);
        }
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

    // ---------- Auth tabs ----------

    loginTab.addEventListener("click", () => {
        loginTab.classList.add("active");
        registerTab.classList.remove("active");
        loginForm.classList.remove("hidden");
        registerForm.classList.add("hidden");
        setMessage(authMessage, "");
    });

    registerTab.addEventListener("click", () => {
        registerTab.classList.add("active");
        loginTab.classList.remove("active");
        registerForm.classList.remove("hidden");
        loginForm.classList.add("hidden");
        setMessage(authMessage, "");
    });

    // ---------- Auth requests ----------

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const button = loginForm.querySelector("button[type=submit]");
        button.disabled = true;
        setMessage(authMessage, "");

        try {
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
                setMessage(authMessage, data.error || "Login failed", "error");
                return;
            }

            localStorage.setItem("taskflow_token", data.token);
            loginForm.reset();
            showDashboard();
        } catch (error) {
            setMessage(authMessage, "Could not reach the server. Please try again.", "error");
        } finally {
            button.disabled = false;
        }
    });

    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const button = registerForm.querySelector("button[type=submit]");
        button.disabled = true;
        setMessage(authMessage, "");

        try {
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
                const detail = data.fields
                    ? Object.values(data.fields).join(" ")
                    : data.error;
                setMessage(authMessage, detail || "Registration failed", "error");
                return;
            }

            setMessage(authMessage, "Account created. You can now log in.", "success");
            registerForm.reset();
            loginTab.click();
        } catch (error) {
            setMessage(authMessage, "Could not reach the server. Please try again.", "error");
        } finally {
            button.disabled = false;
        }
    });

    document.getElementById("logoutButton").addEventListener("click", () => {
        localStorage.removeItem("taskflow_token");
        showAuth();
    });

    // ---------- Authenticated requests ----------

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
        try {
            const response = await api("/api/users/me");
            const user = await response.json();
            document.getElementById("welcome").textContent =
                `Signed in as ${user.username} \u00b7 ${user.role}`;
        } catch (error) {
            // handled by api()'s session-expiry redirect
        }
    }

    // ---------- Task list ----------

    async function loadTasks() {
        try {
            const response = await api("/api/tasks");
            allTasks = await response.json();
            renderStats();
            renderTasks();
        } catch (error) {
            // handled by api()'s session-expiry redirect
        }
    }

    function renderStats() {
        document.getElementById("statTotal").textContent = allTasks.length;
        document.getElementById("statPending").textContent =
            allTasks.filter(t => t.status === "PENDING").length;
        document.getElementById("statProgress").textContent =
            allTasks.filter(t => t.status === "IN_PROGRESS").length;
        document.getElementById("statDone").textContent =
            allTasks.filter(t => t.status === "COMPLETED").length;
    }

    function renderTasks() {
        const visible = activeFilter === "ALL"
            ? allTasks
            : allTasks.filter(task => task.status === activeFilter);

        if (visible.length === 0) {
            tasksContainer.innerHTML = "";
            emptyState.classList.remove("hidden");
            return;
        }

        emptyState.classList.add("hidden");
        tasksContainer.innerHTML = visible.map(renderTaskCard).join("");

        visible.forEach(task => {
            const startBtn = document.querySelector(`[data-start="${task.id}"]`);
            const completeBtn = document.querySelector(`[data-complete="${task.id}"]`);
            const editBtn = document.querySelector(`[data-edit="${task.id}"]`);
            const deleteBtn = document.querySelector(`[data-delete="${task.id}"]`);

            if (startBtn) startBtn.addEventListener("click", () => transitionTask(task.id, "start"));
            if (completeBtn) completeBtn.addEventListener("click", () => transitionTask(task.id, "complete"));
            if (editBtn) editBtn.addEventListener("click", () => enterEditMode(task));
            if (deleteBtn) deleteBtn.addEventListener("click", () => deleteTask(task.id));
        });
    }

    function renderTaskCard(task) {
        const statusClass = `badge-${task.status.toLowerCase()}`;
        const statusLabel = STATUS_LABELS[task.status] || task.status;

        const actions = [];
        if (task.status === "PENDING") {
            actions.push(`<button class="btn btn-sm btn-ghost" data-start="${task.id}">Start</button>`);
        }
        if (task.status !== "COMPLETED") {
            actions.push(`<button class="btn btn-sm btn-success" data-complete="${task.id}">Complete</button>`);
        }
        actions.push(`<button class="btn btn-sm btn-ghost" data-edit="${task.id}">Edit</button>`);
        actions.push(`<button class="btn btn-sm btn-danger-outline" data-delete="${task.id}">Delete</button>`);

        return `
            <article class="task">
                <div class="task-top">
                    <h3>${escapeHtml(task.title)}</h3>
                    <span class="badge ${statusClass}">${statusLabel}</span>
                </div>
                <p>${escapeHtml(task.description)}</p>
                <div class="task-meta">
                    <span>Due ${escapeHtml(task.dueDate)}</span>
                </div>
                <div class="task-actions">${actions.join("")}</div>
            </article>
        `;
    }

    filterButtons.forEach(button => {
        button.addEventListener("click", () => {
            filterButtons.forEach(b => b.classList.remove("active"));
            button.classList.add("active");
            activeFilter = button.dataset.filter;
            renderTasks();
        });
    });

    // ---------- Create / edit ----------

    function enterEditMode(task) {
        taskFormTitle.textContent = "Edit task";
        taskIdField.value = task.id;
        taskTitleField.value = task.title;
        taskDescriptionField.value = task.description;
        taskDueDateField.value = task.dueDate;
        taskSubmitButton.textContent = "Save changes";
        taskCancelButton.classList.remove("hidden");
        taskTitleField.focus();
    }

    function exitEditMode() {
        taskFormTitle.textContent = "New task";
        taskForm.reset();
        taskIdField.value = "";
        taskSubmitButton.textContent = "Add task";
        taskCancelButton.classList.add("hidden");
    }

    taskCancelButton.addEventListener("click", exitEditMode);

    taskForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        taskSubmitButton.disabled = true;
        setMessage(taskMessage, "");

        const id = taskIdField.value;
        const payload = {
            title: taskTitleField.value,
            description: taskDescriptionField.value,
            dueDate: taskDueDateField.value
        };

        try {
            const response = await api(
                id ? `/api/tasks/${id}` : "/api/tasks",
                {
                    method: id ? "PUT" : "POST",
                    body: JSON.stringify(payload)
                }
            );

            const data = await response.json();

            if (!response.ok) {
                const detail = data.fields
                    ? Object.values(data.fields).join(" ")
                    : data.error;
                setMessage(taskMessage, detail || "Could not save task", "error");
                return;
            }

            exitEditMode();
            await loadTasks();
        } catch (error) {
            setMessage(taskMessage, "Could not reach the server. Please try again.", "error");
        } finally {
            taskSubmitButton.disabled = false;
        }
    });

    async function transitionTask(id, action) {
        try {
            const response = await api(`/api/tasks/${id}/${action}`, { method: "PATCH" });
            if (!response.ok) {
                const data = await response.json();
                setMessage(taskMessage, data.error || "Could not update task", "error");
                return;
            }
            await loadTasks();
        } catch (error) {
            setMessage(taskMessage, "Could not reach the server. Please try again.", "error");
        }
    }

    async function deleteTask(id) {
        if (!window.confirm("Delete this task? This cannot be undone.")) {
            return;
        }

        try {
            const response = await api(`/api/tasks/${id}`, { method: "DELETE" });
            if (!response.ok && response.status !== 204) {
                const data = await response.json();
                setMessage(taskMessage, data.error || "Could not delete task", "error");
                return;
            }
            if (taskIdField.value === String(id)) {
                exitEditMode();
            }
            await loadTasks();
        } catch (error) {
            setMessage(taskMessage, "Could not reach the server. Please try again.", "error");
        }
    }

    // ---------- AI Plan Assistant ----------

    if (getAiPlanBtn) {
        getAiPlanBtn.addEventListener("click", async () => {
            getAiPlanBtn.disabled = true;
            getAiPlanBtn.textContent = "Analyzing...";
            aiSummary.textContent = "Analyzing your tasks with Gemini AI...";

            try {
                const response = await api("/api/ai/recommendations");
                const data = await response.json();

                if (!response.ok) {
                    aiSummary.textContent = data.error || "Could not generate AI plan.";
                    return;
                }

                aiSummary.textContent = data.summary || "Here is your plan for today:";
                aiRecommendations.innerHTML = "";

                if (data.recommendations && data.recommendations.length > 0) {
                    data.recommendations.forEach(rec => {
                        const item = document.createElement("div");
                        item.className = "ai-recommendation-item";
                        item.innerHTML = `<strong>${escapeHtml(rec.title)}:</strong> ${escapeHtml(rec.reason)}`;
                        aiRecommendations.appendChild(item);
                    });
                    aiRecommendations.classList.remove("hidden");
                } else {
                    aiRecommendations.innerHTML = "<p class='muted'>No urgent priorities right now.</p>";
                    aiRecommendations.classList.remove("hidden");
                }
            } catch (error) {
                aiSummary.textContent = "Unable to load AI recommendations at this time.";
            } finally {
                getAiPlanBtn.disabled = false;
                getAiPlanBtn.textContent = "Analyze My Day";
            }
        });
    }

    function escapeHtml(value) {
        const div = document.createElement("div");
        div.textContent = value;
        return div.innerHTML;
    }

    // ---------- Boot ----------

    if (getToken()) {
        showDashboard();
    }
})();