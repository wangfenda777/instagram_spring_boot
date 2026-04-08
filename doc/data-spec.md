# Instagram 项目数据规范

## 一、通用数据结构

### 1.1 统一响应格式

所有接口统一返回以下格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1711987200000
}
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| code | number | 状态码，200 成功 |
| message | string | 提示信息 |
| data | any | 业务数据 |
| timestamp | number | 服务器时间戳 |

### 1.2 分页数据格式

涉及列表查询的接口，统一使用以下分页格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "hasMore": true
  }
}
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| list | array | 数据列表 |
| total | number | 总数量 |
| page | number | 当前页码 |
| pageSize | number | 每页数量 |
| hasMore | boolean | 是否还有更多数据 |

### 1.3 错误码规范

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 token 失效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 二、核心数据模型

> **ID 类型说明**：数据库中所有 ID 字段为 `BIGINT` 类型，接口返回时统一序列化为 `string` 类型。
> 这是为了避免 JavaScript 处理大数时的精度丢失问题（JS 的 Number 最大安全整数为 2^53 - 1）。
> 前端传参时也使用 string 类型传递 ID。

### 2.1 用户信息（User）

```json
{
  "userId": "1001",
  "username": "qiwang6189",
  "displayName": "Qi Wang",
  "avatar": "https://cdn.example.com/avatar/1.jpg",
  "bio": "个性签名",
  "isVerified": false,
  "isPrivate": false
}
```

**字段说明**：
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | string | 是 | 用户 ID |
| username | string | 是 | 用户名（唯一） |
| displayName | string | 是 | 显示名称 |
| avatar | string | 是 | 头像 URL |
| bio | string | 否 | 个性签名 |
| isVerified | boolean | 是 | 是否认证用户 |
| isPrivate | boolean | 是 | 是否私密账户 |

### 2.2 用户统计信息（UserStats）

```json
{
  "userId": "1001",
  "postsCount": 123,
  "followersCount": 5678,
  "followingCount": 234
}
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| userId | string | 用户 ID |
| postsCount | number | 帖子数量 |
| followersCount | number | 粉丝数量 |
| followingCount | number | 关注数量 |

### 2.3 帖子信息（Post）

```json
{
  "postId": "2001",
  "userId": "1001",
  "username": "qiwang6189",
  "avatar": "https://cdn.example.com/avatar/1.jpg",
  "isVerified": false,
  "location": "Beijing, China",
  "content": "帖子内容文字 #travel #food",
  "mediaType": "image",
  "mediaList": [
    {
      "url": "https://cdn.example.com/post/1.jpg",
      "width": 1080,
      "height": 1080,
      "type": "image"
    }
  ],
  "tags": [
    {
      "tagId": "1",
      "name": "#travel",
      "heat": 128,
      "postCount": 35
    }
  ],
  "createdAt": 1711987200000
}
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| postId | string | 帖子 ID |
| userId | string | 发布者 ID |
| username | string | 发布者用户名 |
| avatar | string | 发布者头像 |
| isVerified | boolean | 是否认证用户 |
| location | string | 位置信息 |
| content | string | 帖子内容 |
| mediaType | string | 媒体类型：image/video |
| mediaList | array | 媒体列表 |
| tags | array | 帖子关联的标签列表 |
| createdAt | number | 发布时间戳 |

### 2.4 标签信息（Tag）

```json
{
  "tagId": "1",
  "name": "#travel",
  "heat": 128,
  "postCount": 35
}
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| tagId | string | 标签 ID |
| name | string | 标签名称，统一带 `#` 前缀返回 |
| heat | number | 标签热度 |
| postCount | number | 关联帖子数 |

### 2.5 帖子统计信息（PostStats）

```json
{
  "postId": "2001",
  "likesCount": 12345,
  "commentsCount": 678,
  "sharesCount": 234,
  "isLiked": false,
  "isSaved": false
}
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| postId | string | 帖子 ID |
| likesCount | number | 点赞数 |
| commentsCount | number | 评论数 |
| sharesCount | number | 分享数 |
| isLiked | boolean | 当前用户是否已点赞 |
| isSaved | boolean | 当前用户是否已收藏 |

### 2.5 快拍信息（Story）

```json
{
  "storyId": "3001",
  "userId": "1001",
  "username": "qiwang6189",
  "avatar": "https://cdn.example.com/avatar/1.jpg",
  "hasUnread": true,
  "expiredAt": 1712073600000
}
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| storyId | string | 快拍 ID |
| userId | string | 用户 ID |
| username | string | 用户名 |
| avatar | string | 头像 URL |
| hasUnread | boolean | 是否有未读快拍 |
| expiredAt | number | 过期时间戳（24小时） |

### 2.6 探索内容项（ExploreItem）

```json
{
  "postId": "2001",
  "mediaType": "image",
  "coverUrl": "https://cdn.example.com/post/1.jpg",
  "mediaCount": 3,
  "width": 1080,
  "height": 1080
}
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| postId | string | 帖子 ID |
| mediaType | string | 类型：image / video |
| coverUrl | string | 封面图 URL |
| mediaCount | number | 媒体总数（> 1 显示多图标识） |
| width | number | 宽度 |
| height | number | 高度 |

---

## 三、接口设计原则

1. **职责单一**：每个接口只做一件事，不在一个接口里返回过多数据
2. **用户基础信息和统计信息分离**：用户头像、昵称等基础信息和粉丝数、帖子数等统计信息由不同接口提供
3. **帖子信息和互动数据分离**：帖子的内容信息和点赞数、评论数等统计信息分开，减少后端计算压力
4. **分页加载**：所有列表接口都支持分页，避免一次加载过多数据
5. **公共接口复用**：登录、退出、查询当前用户信息等多处使用的接口放在公共文档中统一管理