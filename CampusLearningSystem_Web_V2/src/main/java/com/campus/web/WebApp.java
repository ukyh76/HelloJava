package com.campus.web;

import com.campus.Repository.InMemoryBehaviorResposity;
import com.campus.behavior.BehaviorType;
import com.campus.dao.impl.InMemoryUserDao;
import com.campus.face.MockFaceRecognitionService;
import com.campus.model.LearningResource;
import com.campus.model.Role;
import com.campus.model.Student;
import com.campus.model.Teacher;
import com.campus.model.User;
import com.campus.recommend.CollaborativeFilteringStrategy;
import com.campus.recommend.HotnessRecommendStrategy;
import com.campus.recommend.HybirdRecommendStrategy;
import com.campus.recommend.RecommendStrategy;
import com.campus.recommend.TagSimilarityStrategy;
import com.campus.service.BehaviorService;
import com.campus.service.ResourceService;
import com.campus.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WebApp {
    private final UserService userService;
    private final ResourceService resourceService;
    private final InMemoryBehaviorResposity behaviorRepository;
    private final BehaviorService behaviorService;
    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    public WebApp() {
        userService = new UserService(new InMemoryUserDao(), new MockFaceRecognitionService());
        resourceService = new ResourceService();
        behaviorRepository = new InMemoryBehaviorResposity();
        behaviorService = new BehaviorService(behaviorRepository);
        initDemoData();
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        WebApp app = new WebApp();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/session", app::handleSession);
        server.createContext("/api/register", app::handleRegister);
        server.createContext("/api/resources", app::handleResources);
        server.createContext("/api/tags", app::handleTags);
        server.createContext("/api/recommendations", app::handleRecommendations);
        server.createContext("/api/behaviors", app::handleBehaviors);
        server.createContext("/", app::handleStatic);
        server.setExecutor(null);
        server.start();
        System.out.println("Campus Learning System 已启动：http://localhost:" + port);
        System.out.println("演示教师：teacher_li / 123456");
        System.out.println("演示学生：stu_zhang / 123456");
    }

    private void initDemoData() {
        Teacher teacher = new Teacher("teacher_li", "123456");
        Student student = new Student("stu_zhang", "123456");
        userService.register(teacher);
        userService.register(student);
        userService.addUserTag(student.getUsername(), "Java");
        userService.addUserTag(student.getUsername(), "算法");

        addDemoResource(teacher, 1, "Java面向对象核心编程笔记", "课程笔记", 85,
                Set.of("Java", "面向对象"));
        addDemoResource(teacher, 2, "数据结构与经典算法精讲", "视频课程", 95,
                Set.of("数据结构", "算法"));
        addDemoResource(teacher, 3, "Java集合框架实践", "实验指导", 72,
                Set.of("Java", "集合", "实践"));
        addDemoResource(teacher, 4, "数据库基础与SQL", "电子教材", 80,
                Set.of("数据库", "SQL"));

        behaviorService.record(1001, 1, BehaviorType.VIEW);
        behaviorService.record(1001, 3, BehaviorType.FAVORITE);
    }

    private void addDemoResource(
            Teacher teacher, int id, String title, String category, int heat, Set<String> tags) {
        LearningResource resource = new LearningResource(id, title, category, heat);
        tags.forEach(resource::addTag);
        resourceService.addResource(teacher, resource);
    }

    private void handleSession(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET" -> {
                    User user = currentUser(exchange);
                    if (user == null) {
                        sendJson(exchange, 401, Map.of("message", "尚未登录"));
                    } else {
                        sendJson(exchange, 200, userView(user));
                    }
                }
                case "POST" -> {
                    Map<String, Object> body = readJson(exchange);
                    User user = userService.login(text(body, "username"), text(body, "password"));
                    if (user == null) {
                        sendJson(exchange, 401, Map.of("message", "用户名或密码错误"));
                        return;
                    }
                    String sessionId = UUID.randomUUID().toString();
                    sessions.put(sessionId, user);
                    exchange.getResponseHeaders().add(
                            "Set-Cookie", "SID=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax");
                    sendJson(exchange, 200, userView(user));
                }
                case "DELETE" -> {
                    String sessionId = sessionId(exchange);
                    if (sessionId != null) {
                        sessions.remove(sessionId);
                    }
                    exchange.getResponseHeaders().add(
                            "Set-Cookie", "SID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
                    sendJson(exchange, 200, Map.of("message", "已退出登录"));
                }
                default -> methodNotAllowed(exchange);
            }
        } catch (Exception error) {
            serverError(exchange, error);
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            methodNotAllowed(exchange);
            return;
        }
        try {
            Map<String, Object> body = readJson(exchange);
            String username = text(body, "username");
            String password = text(body, "password");
            User user = "TEACHER".equalsIgnoreCase(text(body, "role"))
                    ? new Teacher(username, password)
                    : new Student(username, password);
            boolean created = userService.register(user);
            sendJson(exchange, created ? 201 : 409,
                    Map.of("message", created ? "注册成功，请登录" : "用户名已存在或信息不完整"));
        } catch (Exception error) {
            serverError(exchange, error);
        }
    }

    private void handleResources(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> query = query(exchange);
                String keyword = query.getOrDefault("keyword", "").trim();
                List<LearningResource> resources = keyword.isEmpty()
                        ? resourceService.getAllResource()
                        : resourceService.searchByKeyword(keyword);
                sendJson(exchange, 200, resources.stream().map(this::resourceView).toList());
                return;
            }

            User user = requireUser(exchange);
            if (user == null) {
                return;
            }
            if (user.getRole() != Role.TEACHER) {
                sendJson(exchange, 403, Map.of("message", "只有教师可以管理学习资源"));
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, Object> body = readJson(exchange);
                LearningResource resource = new LearningResource(
                        intValue(body, "id"), text(body, "title"), text(body, "category"),
                        intValue(body, "heatScore"));
                for (String tag : stringList(body.get("tags"))) {
                    if (!tag.isBlank()) {
                        resource.addTag(tag.trim());
                    }
                }
                boolean created = resourceService.addResource(user, resource);
                sendJson(exchange, created ? 201 : 400,
                        Map.of("message", created ? "资源添加成功" : "资源添加失败"));
                return;
            }

            if ("DELETE".equals(exchange.getRequestMethod())) {
                int id = Integer.parseInt(query(exchange).getOrDefault("id", "0"));
                boolean deleted = resourceService.deleteResource(user, id);
                sendJson(exchange, deleted ? 200 : 404,
                        Map.of("message", deleted ? "资源删除成功" : "没有找到该资源"));
                return;
            }
            methodNotAllowed(exchange);
        } catch (IllegalArgumentException error) {
            sendJson(exchange, 400, Map.of("message", "请求参数格式不正确"));
        } catch (Exception error) {
            serverError(exchange, error);
        }
    }

    private void handleTags(HttpExchange exchange) throws IOException {
        User user = requireUser(exchange);
        if (user == null) {
            return;
        }
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 200, Map.of("tags", user.getTags()));
            } else if ("POST".equals(exchange.getRequestMethod())) {
                String tag = text(readJson(exchange), "tag").trim();
                if (tag.isEmpty()) {
                    sendJson(exchange, 400, Map.of("message", "标签不能为空"));
                    return;
                }
                userService.addUserTag(user.getUsername(), tag);
                sendJson(exchange, 200, Map.of("tags", user.getTags()));
            } else if ("DELETE".equals(exchange.getRequestMethod())) {
                String tag = query(exchange).getOrDefault("tag", "");
                user.removeTag(tag);
                sendJson(exchange, 200, Map.of("tags", user.getTags()));
            } else {
                methodNotAllowed(exchange);
            }
        } catch (Exception error) {
            serverError(exchange, error);
        }
    }

    private void handleRecommendations(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            methodNotAllowed(exchange);
            return;
        }
        User user = requireUser(exchange);
        if (user == null) {
            return;
        }
        try {
            Map<String, String> query = query(exchange);
            String type = query.getOrDefault("strategy", "hybrid");
            int limit = Math.max(1, Math.min(20,
                    Integer.parseInt(query.getOrDefault("n", "5"))));
            List<LearningResource> all = resourceService.getAllResource();
            RecommendStrategy tag = new TagSimilarityStrategy();
            RecommendStrategy hot = new HotnessRecommendStrategy();
            RecommendStrategy collaborative = new CollaborativeFilteringStrategy(behaviorRepository);
            RecommendStrategy strategy = switch (type) {
                case "tag" -> tag;
                case "hot" -> hot;
                case "collaborative" -> collaborative;
                default -> new HybirdRecommendStrategy(tag, collaborative, hot);
            };

            Map<Integer, Double> scores = strategy.scoreAll(user, user.getTags(), all);
            List<Map<String, Object>> result = all.stream()
                    .sorted(Comparator.comparingDouble(
                            (LearningResource item) -> scores.getOrDefault(item.getId(), 0.0)).reversed())
                    .limit(limit)
                    .map(resource -> {
                        Map<String, Object> item = new LinkedHashMap<>(resourceView(resource));
                        item.put("score", scores.getOrDefault(resource.getId(), 0.0));
                        return item;
                    })
                    .toList();
            sendJson(exchange, 200, result);
        } catch (IllegalArgumentException error) {
            sendJson(exchange, 400, Map.of("message", "推荐参数格式不正确"));
        } catch (Exception error) {
            serverError(exchange, error);
        }
    }

    private void handleBehaviors(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            methodNotAllowed(exchange);
            return;
        }
        User user = requireUser(exchange);
        if (user == null) {
            return;
        }
        try {
            Map<String, Object> body = readJson(exchange);
            BehaviorType type = BehaviorType.valueOf(text(body, "type").toUpperCase());
            behaviorService.record(user.getUserID(), intValue(body, "resourceId"), type);
            sendJson(exchange, 201, Map.of("message", "学习行为已记录"));
        } catch (Exception error) {
            sendJson(exchange, 400, Map.of("message", "行为类型或资源编号不正确"));
        }
    }

    private void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) {
            path = "/index.html";
        }
        if (path.contains("..")) {
            sendJson(exchange, 400, Map.of("message", "非法路径"));
            return;
        }
        String resourcePath = "/web" + path;
        try (InputStream input = WebApp.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                sendJson(exchange, 404, Map.of("message", "页面不存在"));
                return;
            }
            byte[] content = input.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType(path));
            exchange.sendResponseHeaders(200, content.length);
            exchange.getResponseBody().write(content);
            exchange.close();
        }
    }

    private User requireUser(HttpExchange exchange) throws IOException {
        User user = currentUser(exchange);
        if (user == null) {
            sendJson(exchange, 401, Map.of("message", "请先登录"));
        }
        return user;
    }

    private User currentUser(HttpExchange exchange) {
        String sessionId = sessionId(exchange);
        return sessionId == null ? null : sessions.get(sessionId);
    }

    private String sessionId(HttpExchange exchange) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) {
            return null;
        }
        for (String part : cookie.split(";")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length == 2 && "SID".equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }

    private Map<String, Object> userView(User user) {
        return Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "roleName", user.getRole().getDescription(),
                "tags", user.getTags());
    }

    private Map<String, Object> resourceView(LearningResource resource) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", resource.getId());
        view.put("title", resource.getTitle());
        view.put("category", resource.getCategory());
        view.put("tags", resource.getTags());
        view.put("heatScore", resource.getHeatScore());
        return view;
    }

    private Map<String, Object> readJson(HttpExchange exchange) throws IOException {
        return Json.parseObject(new String(
                exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    private String text(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? "" : value.toString();
    }

    private int intValue(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> values)) {
            return new ArrayList<>();
        }
        return values.stream().map(String::valueOf).toList();
    }

    private Map<String, String> query(HttpExchange exchange) {
        Map<String, String> result = new LinkedHashMap<>();
        String rawQuery = exchange.getRequestURI().getRawQuery();
        if (rawQuery == null || rawQuery.isBlank()) {
            return result;
        }
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] content = Json.stringify(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, content.length);
        exchange.getResponseBody().write(content);
        exchange.close();
    }

    private void methodNotAllowed(HttpExchange exchange) throws IOException {
        sendJson(exchange, 405, Map.of("message", "不支持该请求方法"));
    }

    private void serverError(HttpExchange exchange, Exception error) throws IOException {
        error.printStackTrace();
        sendJson(exchange, 500, Map.of("message", "服务器处理请求时发生错误"));
    }

    private String contentType(String path) {
        if (path.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        return "text/html; charset=utf-8";
    }
}
