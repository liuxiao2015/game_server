# 快速开始指南

## 环境要求

- **JDK 17+**
- **Maven 3.8+**
- **Docker 20+**
- **Docker Compose 2.0+**
- **MySQL 8.0+**
- **Redis 7.0+**
- **Node.js 18+** (用于前端开发)

## 部署步骤

### 1. 克隆代码

```bash
git clone https://github.com/liuxiao2015/game_server.git
cd game_server
```

### 2. 环境准备

#### 启动基础服务
```bash
# 启动 MySQL 和 Redis
docker-compose -f docker/docker-compose.dev.yml up -d mysql redis

# 等待服务启动
sleep 30
```

#### 初始化数据库
```bash
# 创建数据库和表结构
mysql -h localhost -P 3306 -u root -p < scripts/init-db.sql
```

### 3. 编译打包

```bash
# 编译整个项目
mvn clean compile

# 打包（跳过测试）
mvn clean package -DskipTests
```

### 4. 启动服务

#### 方式一：使用 Docker Compose (推荐)
```bash
# 构建镜像并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

#### 方式二：本地启动
```bash
# 启动网关服务
cd game_service/service-gateway
mvn spring-boot:run

# 启动逻辑服务
cd ../service-logic
mvn spring-boot:run

# 启动聊天服务
cd ../service-chat
mvn spring-boot:run

# 启动管理后台
cd ../../game_adm/adm-backend
mvn spring-boot:run
```

### 5. 启动前端

```bash
# 进入前端目录
cd game_adm/adm-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 验证部署

### 1. 检查服务状态

```bash
# 检查所有服务
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Logic
curl http://localhost:8082/actuator/health  # Chat
curl http://localhost:8083/actuator/health  # ADM
```

### 2. 访问管理后台

打开浏览器访问：http://localhost:8080

默认登录信息：
- 用户名：admin
- 密码：admin123

### 3. 查看API文档

- Gateway API: http://localhost:8080/swagger-ui.html
- Logic API: http://localhost:8081/swagger-ui.html
- Chat API: http://localhost:8082/swagger-ui.html
- ADM API: http://localhost:8083/swagger-ui.html

### 4. 监控面板

- Spring Boot Admin: http://localhost:8083/admin
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

## 配置说明

### 数据库配置

在 `application.yml` 中修改数据库连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/game_db?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Redis配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password
    database: 0
```

### 日志配置

日志文件默认输出到 `logs/` 目录，可以通过以下配置修改：

```yaml
logging:
  file:
    name: logs/game-server.log
  level:
    com.game: DEBUG
    org.springframework: INFO
```

## 开发模式

### 热部署

使用 Spring Boot DevTools 实现热部署：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### 调试模式

启动时添加调试参数：

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## 常见问题

### 1. 端口冲突

如果遇到端口冲突，可以修改 `application.yml` 中的端口配置：

```yaml
server:
  port: 8080  # 修改为其他可用端口
```

### 2. 内存不足

增加JVM内存配置：

```bash
export JAVA_OPTS="-Xms512m -Xmx2g"
```

### 3. 数据库连接失败

检查数据库服务是否启动，确认连接配置是否正确。

### 4. 前端无法访问后端API

检查前端代理配置 `vite.config.js`：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8083',
      changeOrigin: true
    }
  }
}
```

## 下一步

- 查看 [API文档](../api/API-Reference.md)
- 了解 [部署指南](ProductionDeployment.md)
- 阅读 [运维手册](../operations/Monitoring.md)