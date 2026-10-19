# Campus Learning Web 版

这是在原控制台项目基础上增加的轻量 Web 界面。它没有使用 Spring Boot、Vue、React 等框架：

- 后端 HTTP 服务：JDK 自带的 `HttpServer`
- 数据交换：项目内置的最小 JSON 工具
- 前端：原生 HTML、CSS、JavaScript
- 业务逻辑：继续调用原有 `UserService`、`ResourceService`、`BehaviorService` 和推荐策略

## 环境要求

- JDK 21
- Bash（macOS、Linux 或 Git Bash）

## 启动

在项目根目录执行（不需要 Maven，也不需要联网）：

```bash
chmod +x run-web.sh
./run-web.sh
```

Windows 用户可以直接双击 `run-web.bat`，或在命令提示符执行：

```bat
run-web.bat
```

如果 `javac` 不在 PATH 中，也可以指定 JDK 目录：

```bash
CAMPUS_JDK_HOME=/你的/jdk-21目录 ./run-web.sh
```

然后在浏览器打开：

```text
http://localhost:8080
```

演示账号：

- 学生：`stu_zhang` / `123456`
- 教师：`teacher_li` / `123456`

如需更改端口：

```bash
PORT=9090 ./run-web.sh
```

## AI 助教密钥

项目不再在源码中保存 API Key。需要启用 Moonshot AI 时，请先设置环境变量：

```bash
export MOONSHOT_API_KEY="你的密钥"
```

当前 Web 版没有开放 AI 问答接口，避免课堂演示时误用密钥或产生费用；后续可以将它作为独立扩展模块加入。

## 教学说明

Web 层位于 `com.campus.web.WebApp`，只负责：

1. 接收浏览器请求；
2. 调用学生已经完成的 Service 和推荐策略；
3. 把结果转换成 JSON 返回。

因此，原有 Java 分层和算法代码仍然是项目主体，网页没有重复实现业务逻辑。
