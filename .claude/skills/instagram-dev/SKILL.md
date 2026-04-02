---
name: instagram-dev
description: 按照 Instagram 项目开发标准生成 Spring Boot 代码。自动处理模块创建、Entity/Mapper/Service/Controller 生成，确保包结构、命名规范、注解使用符合项目标准。
user-invocable: true
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
argument-hint: [module-name] [feature-description]
---

# Instagram Spring Boot 开发标准

你是 Instagram Spring Boot 项目的开发助手。严格遵循以下开发标准。

## 项目包结构

```
src/main/java/com/example/instagram/
├── common/                    # 公共模块
│   ├── constant/             # 常量类
│   ├── enums/                # 枚举类
│   ├── exception/            # 自定义异常
│   └── utils/                # 工具类
├── config/                   # 配置类
├── interceptor/              # 拦截器
└── module/                   # 业务模块（按功能划分）
    ├── auth/                 # 认证模块
    ├── user/                 # 用户模块
    ├── post/                 # 帖子模块
    ├── story/                # 快拍模块
    ├── follow/               # 关注模块
    ├── notification/         # 通知模块
    └── explore/              # 探索模块
```

每个业务模块内部结构：
```
module/{模块名}/
├── controller/               # 控制器
├── service/                  # 服务接口
│   └── impl/                # 服务实现
├── mapper/                   # Mapper 接口
├── entity/                   # 实体类
├── dto/                      # 请求参数对象
└── vo/                       # 响应数据对象
```

## 命名规范

### 类命名规则

| 类型 | 命名规则 | 示例 |
|-----|---------|------|
| Controller | `{模块名}Controller` | `UserController` |
| Service 接口 | `{模块名}Service` | `UserService` |
| Service 实现 | `{模块名}ServiceImpl` | `UserServiceImpl` |
| Mapper | `{实体名}Mapper` | `UserMapper` |
| Entity | `{实体名}` | `User`, `Post` |
| DTO | `{功能}DTO` | `LoginDTO`, `CreatePostDTO` |
| VO | `{功能}VO` | `UserInfoVO`, `PostDetailVO` |
| 常量类 | `{模块}Constant` | `AuthConstant` |
| 枚举类 | `{含义}Enum` | `MediaTypeEnum` |
| 工具类 | `{功能}Util` | `JwtUtil` |

### 方法命名规则

| 操作 | 命名规则 | 示例 |
|-----|---------|------|
| 查询单个 | `get{实体}By{条件}` | `getUserById` |
| 查询列表 | `list{实体}By{条件}` | `listPostsByUserId` |
| 分页查询 | `page{实体}` | `pagePostFeed` |
| 新增 | `create{实体}` | `createPost` |
| 更新 | `update{实体}` | `updateUserProfile` |
| 删除 | `delete{实体}By{条件}` | `deletePostById` |
| 判断 | `is{条件}` / `has{条件}` | `isFollowing` |

## 代码模板

### 1. Entity 模板

```java
package com.example.instagram.module.{module}.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("{table_name}")
public class {Entity} {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String field;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
```

### 2. Mapper 模板

```java
package com.example.instagram.module.{module}.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.instagram.module.{module}.entity.{Entity};
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface {Entity}Mapper extends BaseMapper<{Entity}> {
}
```

### 3. DTO 模板

```java
package com.example.instagram.module.{module}.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class {Function}DTO {
    
    @NotBlank(message = "字段不能为空")
    private String field;
}
```

### 4. VO 模板

```java
package com.example.instagram.module.{module}.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class {Function}VO {
    
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;  // ID 统一用 String 避免 JS 精度丢失
    
    private String field;
    
    private Long timestamp;  // 时间戳
}
```

### 5. Service 接口模板

```java
package com.example.instagram.module.{module}.service;

import com.example.instagram.module.{module}.dto.{Function}DTO;
import com.example.instagram.module.{module}.vo.{Function}VO;

public interface {Module}Service {
    
    {Function}VO method({Function}DTO dto);
}
```

### 6. Service 实现模板

```java
package com.example.instagram.module.{module}.service.impl;

import com.example.instagram.module.{module}.entity.{Entity};
import com.example.instagram.module.{module}.mapper.{Entity}Mapper;
import com.example.instagram.module.{module}.service.{Module}Service;
import com.example.instagram.module.{module}.dto.{Function}DTO;
import com.example.instagram.module.{module}.vo.{Function}VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class {Module}ServiceImpl implements {Module}Service {
    
    @Autowired
    private {Entity}Mapper {entity}Mapper;
    
    @Override
    public {Function}VO method({Function}DTO dto) {
        // 业务逻辑实现
        return new {Function}VO();
    }
}
```

### 7. Controller 模板

```java
package com.example.instagram.module.{module}.controller;

import com.example.instagram.common.result.Result;
import com.example.instagram.module.{module}.dto.{Function}DTO;
import com.example.instagram.module.{module}.vo.{Function}VO;
import com.example.instagram.module.{module}.service.{Module}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/{module}")
@Tag(name = "{模块名}管理")
public class {Module}Controller {
    
    @Autowired
    private {Module}Service {module}Service;
    
    @Operation(summary = "{接口说明}")
    @PostMapping("/{path}")
    public Result<{Function}VO> method(@Valid @RequestBody {Function}DTO dto) {
        return Result.success({module}Service.method(dto));
    }
}
```

## 统一响应格式

### Result 类

```java
package com.example.instagram.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
```

### PageResult 类

```java
package com.example.instagram.common.result;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Boolean hasMore;
}
```

## 关键规则

### 1. ID 类型处理
- **Entity 中**：使用 `Long` 类型
- **VO/DTO 中**：使用 `String` 类型，加 `@JsonSerialize(using = ToStringSerializer.class)` 注解
- **原因**：避免 JavaScript 处理大数时精度丢失

### 2. 时间字段处理
- **数据库**：`TIMESTAMP` 类型
- **Entity**：`LocalDateTime` 类型
- **VO 返回**：`Long` 类型时间戳

### 3. 逻辑删除
- 使用 `is_deleted` 字段（0-未删除，1-已删除）
- Entity 中加 `@TableLogic` 注解
- MyBatis-Plus 自动处理

### 4. 字段映射
- **数据库**：下划线命名（`created_at`）
- **Java**：驼峰命名（`createdAt`）
- 使用 `@TableField` 注解显式映射

### 5. 必需注解
- Entity：`@Data`, `@TableName`, `@TableId`, `@TableField`, `@TableLogic`
- Mapper：`@Mapper`
- Service：`@Service`
- Controller：`@RestController`, `@RequestMapping`, `@Tag`, `@Operation`
- DTO：`@Data`, `@NotBlank`, `@NotNull` 等校验注解
- VO：`@Data`, `@JsonSerialize`

## 开发流程

当用户要求开发某个功能时，按以下步骤执行：

1. **阅读接口文档**：查看 `doc/` 目录下的相关接口文档
2. **确认数据库表**：参考 `doc/instagram数据库设计文档.md`
3. **创建 Entity**：对应数据库表结构
4. **创建 Mapper**：继承 `BaseMapper<T>`
5. **创建 DTO/VO**：请求参数和响应数据对象
6. **创建 Service**：接口和实现类
7. **创建 Controller**：处理 HTTP 请求
8. **确保文件位置正确**：严格按照包结构放置文件

## 参考文档

- 完整开发标准：`doc/开发标准.md`
- 数据库设计：`doc/instagram数据库设计文档.md`
- 接口文档：
  - `doc/common.md` - 公共接口
  - `doc/home.md` - 首页接口
  - `doc/profile.md` - 个人主页接口
  - `doc/explore.md` - 探索页接口
  - `doc/data-spec.md` - 数据规范

## 使用示例

**调用方式**：
```
/instagram-dev user login
/instagram-dev post create
/instagram-dev follow 关注用户
```

**执行时**：
1. 自动读取对应的接口文档
2. 按标准创建所有必需的类
3. 确保包结构、命名、注解符合规范
4. 生成完整可运行的代码
