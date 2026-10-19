const state = { user: null, resources: [] };
const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => [...document.querySelectorAll(selector)];

async function api(path, options = {}) {
    const response = await fetch(path, {
        credentials: "same-origin",
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || "请求失败");
    return data;
}

function toast(message) {
    const element = $("#toast");
    element.textContent = message;
    element.classList.add("show");
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => element.classList.remove("show"), 2200);
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>'"]/g, (char) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
    })[char]);
}

function showApp(user) {
    state.user = user;
    $("#authView").classList.add("hidden");
    $("#appView").classList.remove("hidden");
    $("#username").textContent = user.username;
    $("#roleName").textContent = user.roleName;
    $("#userAvatar").textContent = user.role === "TEACHER" ? "师" : "学";
    $$(".teacher-only").forEach((el) => el.classList.toggle("hidden", user.role !== "TEACHER"));
    $$(".student-only").forEach((el) => el.classList.toggle("hidden", user.role !== "STUDENT"));
    renderTags(user.tags || []);
    loadResources();
}

function showAuth() {
    state.user = null;
    $("#appView").classList.add("hidden");
    $("#authView").classList.remove("hidden");
}

function openPage(page) {
    const titles = { resources: "资料中心", recommendations: "智能推荐", tags: "兴趣标签", manage: "资源管理" };
    $$(".page").forEach((el) => el.classList.toggle("active", el.id === `${page}Page`));
    $$(".nav-item").forEach((el) => el.classList.toggle("active", el.dataset.page === page));
    $("#pageTitle").textContent = titles[page];
    if (page === "tags") loadTags();
}

async function loadResources(keyword = "") {
    try {
        state.resources = await api(`/api/resources${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ""}`);
        renderResources();
    } catch (error) { toast(error.message); }
}

function renderResources() {
    const grid = $("#resourceGrid");
    if (!state.resources.length) {
        grid.innerHTML = '<div class="empty-state">没有找到学习资料</div>';
        return;
    }
    grid.innerHTML = state.resources.map((resource) => `
        <article class="resource-card">
            <div class="resource-meta"><span>${escapeHtml(resource.category)}</span><span>热度 ${resource.heatScore}</span></div>
            <h3>${escapeHtml(resource.title)}</h3>
            <div class="tag-list">${resource.tags.map((tag) => `<span class="tag">${escapeHtml(tag)}</span>`).join("")}</div>
            <div class="card-actions">
                <button class="small-button" onclick="recordBehavior(${resource.id}, 'VIEW')">查看</button>
                <button class="small-button" onclick="recordBehavior(${resource.id}, 'FAVORITE')">收藏</button>
                ${state.user?.role === "TEACHER" ? `<button class="small-button danger-button" onclick="deleteResource(${resource.id})">删除</button>` : ""}
            </div>
        </article>`).join("");
}

async function recordBehavior(resourceId, type) {
    try {
        await api("/api/behaviors", { method: "POST", body: JSON.stringify({ resourceId, type }) });
        toast(type === "FAVORITE" ? "已收藏并计入推荐偏好" : "已记录本次浏览");
    } catch (error) { toast(error.message); }
}

async function deleteResource(id) {
    if (!confirm("确定删除这条学习资料吗？")) return;
    try {
        const result = await api(`/api/resources?id=${id}`, { method: "DELETE" });
        toast(result.message);
        loadResources();
    } catch (error) { toast(error.message); }
}

async function loadTags() {
    try {
        const result = await api("/api/tags");
        renderTags(result.tags);
    } catch (error) { toast(error.message); }
}

function renderTags(tags) {
    $("#tagList").innerHTML = tags.length
        ? tags.map((tag) => `<span class="tag">${escapeHtml(tag)} <button onclick="removeTag('${encodeURIComponent(tag)}')">×</button></span>`).join("")
        : '<span class="muted">还没有兴趣标签</span>';
}

async function removeTag(encodedTag) {
    try {
        const result = await api(`/api/tags?tag=${encodedTag}`, { method: "DELETE" });
        renderTags(result.tags);
    } catch (error) { toast(error.message); }
}

async function loadRecommendations() {
    const strategy = $("#strategySelect").value;
    const n = $("#topNSelect").value;
    const container = $("#recommendationList");
    container.className = "recommend-list empty-state";
    container.textContent = "正在计算推荐结果…";
    try {
        const items = await api(`/api/recommendations?strategy=${strategy}&n=${n}`);
        container.className = "recommend-list";
        container.innerHTML = items.map((item, index) => `
            <article class="recommend-row">
                <span class="rank">${String(index + 1).padStart(2, "0")}</span>
                <div><h4>${escapeHtml(item.title)}</h4><small>${escapeHtml(item.category)} · ${item.tags.map(escapeHtml).join(" / ")}</small></div>
                <div><div class="score-track"><span style="width:${Math.max(2, item.score * 100)}%"></span></div><div class="score-label">匹配度 ${(item.score * 100).toFixed(1)}%</div></div>
            </article>`).join("");
    } catch (error) {
        container.textContent = error.message;
        toast(error.message);
    }
}

$$('[data-auth-tab]').forEach((button) => button.addEventListener("click", () => {
    $$('[data-auth-tab]').forEach((item) => item.classList.toggle("active", item === button));
    $("#loginForm").classList.toggle("hidden", button.dataset.authTab !== "login");
    $("#registerForm").classList.toggle("hidden", button.dataset.authTab !== "register");
}));

$("#loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    try { showApp(await api("/api/session", { method: "POST", body: JSON.stringify(values) })); }
    catch (error) { toast(error.message); }
});

$("#registerForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    try {
        const result = await api("/api/register", { method: "POST", body: JSON.stringify(values) });
        toast(result.message);
        $('[data-auth-tab="login"]').click();
    } catch (error) { toast(error.message); }
});

$("#logoutButton").addEventListener("click", async () => {
    await api("/api/session", { method: "DELETE" });
    showAuth();
});

$$('[data-page]').forEach((button) => button.addEventListener("click", () => openPage(button.dataset.page)));
$("#searchButton").addEventListener("click", () => loadResources($("#searchInput").value.trim()));
$("#showAllButton").addEventListener("click", () => { $("#searchInput").value = ""; loadResources(); });
$("#searchInput").addEventListener("keydown", (event) => { if (event.key === "Enter") $("#searchButton").click(); });
$("#recommendButton").addEventListener("click", loadRecommendations);

$("#tagForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const tag = new FormData(form).get("tag");
    try {
        const result = await api("/api/tags", { method: "POST", body: JSON.stringify({ tag }) });
        renderTags(result.tags);
        form.reset();
    } catch (error) { toast(error.message); }
});

$("#resourceForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const values = Object.fromEntries(new FormData(form));
    values.id = Number(values.id);
    values.heatScore = Number(values.heatScore);
    values.tags = values.tags.split(/[,，]/).map((tag) => tag.trim()).filter(Boolean);
    try {
        const result = await api("/api/resources", { method: "POST", body: JSON.stringify(values) });
        toast(result.message);
        form.reset();
        loadResources();
    } catch (error) { toast(error.message); }
});

(async function restoreSession() {
    try { showApp(await api("/api/session")); }
    catch (_) { showAuth(); }
})();
