# Instagram Spring Boot Backend

基于 Spring Boot 3、MyBatis-Plus、MySQL 的 Instagram 风格后端接口项目。

## 技术栈

- Java 21
- Spring Boot 3.1.11
- MyBatis-Plus 3.5.7
- MySQL 8
- Knife4j / OpenAPI 3
- JJWT
- Lombok

## 主要功能

- 认证：登录、刷新 token、退出登录
- 用户：当前用户信息、用户资料、统计信息、编辑资料、推荐用户
- 帖子：发布、编辑、删除、Feed、详情、点赞、取消点赞、收藏、取消收藏
- 关注：关注、取消关注
- 快拍：关注用户的 Story Feed
- 探索：Explore Feed、搜索用户、搜索帖子
- 上传：图片上传

## 项目结构

项目主包为 `com.example.instagram`，按业务域拆分到 `module/<domain>/`：

- `auth`：认证与 refresh token
- `user`：用户资料、统计、个人主页内容
- `post`：帖子与互动行为
- `follow`：关注关系
- `story`：快拍列表
- `explore`：探索与搜索
- `upload`：文件上传
- `notification`：通知数据访问

通用能力放在：
- `common`：统一返回体、异常、工具类
- `config`：Web / MyBatis-Plus / Knife4j 配置
- `interceptor`：登录拦截

## 本地运行

### 1. 配置数据库与上传目录

编辑 `src/main/resources/application.yml`，确认以下配置可用：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/instagram_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: <your-username>
    password: <your-password>

server:
  port: 8081

upload:
  path: /data/instagram/uploads/
  url-prefix: /uploads
```

说明：
- 默认数据库名为 `instagram_db`
- 默认端口为 `8081`
- 上传文件会落盘到 `upload.path`，并通过 `/uploads/**` 对外访问

### 2. 启动项目

```bash
mvn spring-boot:run
```

### 3. 打包

```bash
mvn clean package
```

## 测试命令

运行全部测试：

```bash
mvn test
```

运行单个测试类：

```bash
mvn -Dtest=ClassName test
```

运行单个测试方法：

```bash
mvn -Dtest=ClassName#methodName test
```

## 接口文档

项目启动后可访问：

- Knife4j: `http://localhost:8081/doc.html`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`

## 认证说明

除以下接口外，`/api/**` 默认都需要携带 Bearer Token：

- `/api/auth/login`
- `/api/auth/refresh`
- `/uploads/**`
- `/doc.html`
- `/webjars/**`
- `/v3/api-docs/**`

请求头格式：

```http
Authorization: Bearer <token>
```

## 统一返回格式

接口统一返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1710000000000
}
```

分页接口返回 `PageResult<T>` 作为 `data`。

## 已实现接口概览

### 认证
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### 用户
- `GET /api/user/me`
- `GET /api/user/info`
- `GET /api/user/stats`
- `POST /api/user/profile/update`
- `GET /api/user/posts`
- `GET /api/user/reels`
- `GET /api/user/discover`
- `POST /api/user/follow`
- `POST /api/user/unfollow`
- `GET /api/user/followers`
- `GET /api/user/following`

### 帖子
- `POST /api/post/create`
- `POST /api/post/update`
- `POST /api/post/delete`
- `GET /api/post/feed`
- `GET /api/post/detail`
- `POST /api/post/like`
- `POST /api/post/unlike`
- `POST /api/post/save`
- `POST /api/post/unsave`

### 快拍 / 探索 / 上传
- `GET /api/story/feed`
- `GET /api/explore/feed`
- `GET /api/search/user`
- `GET /api/search/post`
- `POST /api/upload/image`

## 当前代码特征

- 使用 MyBatis-Plus，且 `map-underscore-to-camel-case: false`，字段命名要和数据库列保持一致
- `AuthInterceptor` 会把当前登录用户写入 `UserContext`，service 层通过它读取当前用户 ID
- 关注数、粉丝数、帖子点赞数等冗余统计由业务写路径维护，不是在查询时动态聚合
- 上传接口当前仅支持 `image/jpeg`、`image/png`、`image/gif`、`image/webp`
