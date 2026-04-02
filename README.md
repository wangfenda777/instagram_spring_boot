# Spring Boot 用户管理系统 - 项目创建报告

## 📋 项目信息

- **项目名称**: user-manage
- **Spring Boot版本**: 3.1.11（LTS稳定版）
- **Java版本**: 21
- **数据库**: MySQL 8.0
- **持久层框架**: MyBatis-Plus 3.5.7
- **API文档**: Knife4j 4.4.0

---

## 🎯 已完成的功能

### 1. 项目结构创建 ✅
```
user_manage_spring_boot/
├── pom.xml                                    # Maven配置文件
├── src/
│   ├── main/
│   │   ├── java/com/example/usermanage/
│   │   │   ├── UserManageApplication.java    # 启动类
│   │   │   ├── entity/
│   │   │   │   └── User.java                 # 用户实体类
│   │   │   ├── mapper/
│   │   │   │   └── UserMapper.java           # MyBatis-Plus Mapper
│   │   │   ├── service/
│   │   │   │   ├── UserService.java          # 服务接口
│   │   │   │   └── impl/
│   │   │   │       └── UserServiceImpl.java  # 服务实现
│   │   │   ├── controller/
│   │   │   │   └── UserController.java       # 控制器
│   │   │   └── config/
│   │   │       └── Knife4jConfig.java        # API文档配置
│   │   └── resources/
│   │       └── application.yml               # 应用配置
│   └── test/java/                            # 测试目录
└── README.md                                  # 本文档
```

### 2. 核心依赖配置 ✅
- Spring Boot Starter Web
- MySQL Connector (mysql-connector-j)
- MyBatis-Plus Boot Starter 3.5.7
- Knife4j OpenAPI3 4.4.0
- Lombok

### 3. 数据库配置 ✅
配置文件位置: `src/main/resources/application.yml`

**⚠️ 部署前必须修改以下配置：**
```yaml
spring:
  datasource:
    # 如果应用和MySQL在同一台服务器，使用localhost
    url: jdbc:mysql://localhost:3306/user_info_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your-username                              # 改为你的数据库用户名
    password: your-password                              # 改为你的数据库密码
```

### 4. 实现的API接口 ✅

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/api/users` | 查询所有用户 | 返回用户列表 |
| GET | `/api/users/{id}` | 根据ID查询用户 | 返回单个用户信息 |
| POST | `/api/users` | 创建用户 | 请求体需包含userName和age |
| PUT | `/api/users` | 更新用户 | 请求体需包含id、userName和age |
| DELETE | `/api/users/{id}` | 删除用户 | 根据ID删除用户 |

---

## 🚀 使用步骤

### 第一步：安装Maven（如果还没安装）
1. 下载Maven: https://maven.apache.org/download.cgi
2. 解压到任意目录，例如 `C:\Program Files\Apache\maven`
3. 配置环境变量：
   - 新建系统变量 `MAVEN_HOME`，值为Maven解压路径
   - 在 `Path` 中添加 `%MAVEN_HOME%\bin`
4. 验证安装：打开命令行输入 `mvn -v`

### 第二步：修改数据库配置
编辑 `src/main/resources/application.yml`，修改为你的真实数据库信息：
```yaml
spring:
  datasource:
    # 如果应用和MySQL在同一台服务器，使用localhost（推荐）
    url: jdbc:mysql://localhost:3306/user_info_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: 你的数据库用户名
    password: 你的数据库密码
```

**重要参数说明：**
- `allowPublicKeyRetrieval=true`: MySQL 8.0必需，解决caching_sha2_password认证问题
- 使用`localhost`而不是公网IP，更安全更快

### 第三步：打包项目
在项目根目录执行：
```bash
mvn clean package -DskipTests
```
打包成功后，会在 `target/` 目录生成 `user-manage-1.0.0.jar`

### 第四步：上传到服务器
将 `target/user-manage-1.0.0.jar` 上传到你的阿里云服务器

### 第五步：运行项目
在服务器上执行：
```bash
java -jar user-manage-1.0.0.jar
```

---

## 📖 API文档访问

项目启动后，访问以下地址查看接口文档：
- **Knife4j文档**: http://localhost:8080/doc.html
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

在文档页面可以直接测试所有接口！

---

## 🧪 接口测试示例

### 1. 创建用户
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"userName":"张三","age":25}'
```

### 2. 查询所有用户
```bash
curl http://localhost:8080/api/users
```

### 3. 根据ID查询用户
```bash
curl http://localhost:8080/api/users/1
```

### 4. 更新用户
```bash
curl -X PUT http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"id":1,"userName":"李四","age":30}'
```

### 5. 删除用户
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

---

## ⚙️ 技术特性

1. **MyBatis-Plus自动CRUD**: 无需手写SQL，自动生成增删改查方法
2. **Knife4j接口文档**: 自动生成美观的API文档，支持在线测试
3. **Lombok简化代码**: 自动生成getter/setter等方法
4. **日志输出**: 控制台输出SQL执行日志，方便调试

---

## ❗ 注意事项

1. **数据库表结构**: 确保MySQL中已存在以下表结构
   ```sql
   CREATE TABLE user_table (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       userName VARCHAR(100),
       age INT
   );
   ```
   **注意**: 字段名是`userName`（驼峰命名），不是`user_name`

2. **端口占用**: 默认使用8080端口，如需修改请编辑application.yml中的server.port

3. **时区设置**: 已配置为Asia/Shanghai时区

4. **字符编码**: 已配置为UTF-8

---

## 📝 操作记录

### 成功完成的操作：
✅ 创建pom.xml配置文件（Spring Boot 3.1.11 + MyBatis-Plus 3.5.7 + Knife4j 4.4.0）
✅ 创建完整的项目目录结构
✅ 创建application.yml配置文件（数据库连接、MyBatis-Plus配置）
✅ 创建UserManageApplication.java启动类
✅ 创建User实体类（对应user_table表）
✅ 创建UserMapper接口（继承MyBatis-Plus BaseMapper）
✅ 创建UserService接口和实现类
✅ 创建UserController控制器（实现增删改查5个接口）
✅ 创建Knife4jConfig配置类（API文档配置）
✅ 生成README.md使用文档

### 遇到的问题及解决：
❌ **问题1 - Spring Boot 3.2.4兼容性**: MyBatis-Plus 3.5.5与Spring Boot 3.2.4存在FactoryBean兼容性问题
   ✅ **解决**: 降级到Spring Boot 3.1.11（LTS稳定版）+ 升级MyBatis-Plus到3.5.7

❌ **问题2 - MySQL权限拒绝**: 使用公网IP连接本地MySQL被拒绝访问
   ✅ **解决**: 改用localhost连接（应用和数据库在同一服务器）

❌ **问题3 - 字段名不匹配**: 数据库字段userName被自动转换为user_name导致查询失败
   ✅ **解决**: 关闭MyBatis-Plus的驼峰转下划线配置（map-underscore-to-camel-case: false）

❌ **问题4 - MySQL 8.0认证**: Public Key Retrieval is not allowed错误
   ✅ **解决**: 在JDBC URL中添加allowPublicKeyRetrieval=true参数

### 版本兼容性说明：
✅ **Java 21** ↔ **Spring Boot 3.1.11**: 完全兼容
✅ **Spring Boot 3.1.11** ↔ **MyBatis-Plus 3.5.7**: 完全兼容（经过实际测试）
✅ **Spring Boot 3.1.11** ↔ **Knife4j 4.4.0**: 完全兼容（使用jakarta命名空间）
✅ **MySQL 8.0** ↔ **mysql-connector-j**: 完全兼容（Spring Boot 3.x默认驱动）

---

## 🎉 项目已就绪！

所有代码已编写完成，项目结构完整。你只需要：
1. 安装Maven
2. 修改数据库配置
3. 执行打包命令
4. 上传jar包到服务器运行

有任何问题随时找我！
