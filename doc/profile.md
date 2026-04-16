# 个人主页接口文档

> 个人主页包含：用户信息、帖子网格、视频列表、发现推荐用户

---

## 1. 获取用户帖子列表（网格）

### 请求信息
- **接口地址**: `GET /api/user/posts?userId=1001&page=1&pageSize=18`
- **接口说明**: 获取指定用户的帖子列表，以网格缩略图形式展示。只返回封面信息，不返回完整内容。

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| userId | string | 是 | 用户 ID | - |
| page | number | 否 | 页码 | 1 |
| pageSize | number | 否 | 每页数量 | 18 |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "postId": "2001",
        "coverUrl": "https://cdn.example.com/post/1.jpg",
        "mediaType": "image",
        "mediaCount": 2
      },
      {
        "postId": "2002",
        "coverUrl": "https://cdn.example.com/post/2.jpg",
        "mediaType": "image",
        "mediaCount": 1
      }
    ],
    "total": 3,
    "page": 1,
    "pageSize": 18,
    "hasMore": false
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| postId | string | 帖子 ID |
| coverUrl | string | 封面缩略图 URL |
| mediaType | string | 类型：image / video |
| mediaCount | number | 媒体数量（多图显示标识） |

---

## 2. 获取用户帖子详情列表（游标加载）

### 请求信息
- **接口地址**: `GET /api/user/posts/detail?userId=24&postId=100`
- **接口说明**: 查询指定用户的帖子详情列表，以某条帖子为锚点。初次加载返回锚点帖子前5条 + 锚点本身 + 后5条（共最多11条）；传 direction 时返回锚点之前或之后的5条。用于用户在个人主页网格点击帖子后，进入帖子详情滚动列表。

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| userId | number | 是 | 目标用户 ID | - |
| postId | number | 是 | 锚点帖子 ID | - |
| direction | string | 否 | 加载方向：不传 = 初次加载（前5条 + 锚点 + 后5条，最多11条）；`before` = 往上加载更新的5条；`after` = 往下加载更老的5条 | - |

### 三种使用方式

**1. 初次加载（不传 direction）**

```
GET /api/user/posts/detail?userId=24&postId=100
```

返回：比 postId=100 更新的5条 + postId=100 本身 + 比它更老的5条，最多11条，按 id 从大到小排列。如果前后不足5条，有几条返回几条。

**2. 往上加载（direction=before）**

```
GET /api/user/posts/detail?userId=24&postId=105&direction=before
```

返回：比 postId=105 更新的5条帖子（不含 postId=105 本身）。

**3. 往下加载（direction=after）**

```
GET /api/user/posts/detail?userId=24&postId=95&direction=after
```

返回：比 postId=95 更老的5条帖子（不含 postId=95 本身）。

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "postId": "105",
        "userId": "24",
        "username": "arsenal",
        "avatar": "https://cdn.example.com/avatar/2.jpg",
        "isVerified": true,
        "location": "London",
        "content": "Match day!",
        "mediaType": "image",
        "mediaList": [
          {
            "url": "https://cdn.example.com/post/5.jpg",
            "type": "image"
          }
        ],
        "mediaCount": 1,
        "likesCount": 120,
        "savedCount": 30,
        "commentsCount": 15,
        "sharesCount": 5,
        "isLiked": true,
        "isSaved": false,
        "tags": [],
        "createdAt": 1711987200000
      }
    ],
    "hasMore": true
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| postId | string | 帖子 ID |
| userId | string | 发布者 ID |
| username | string | 发布者用户名 |
| avatar | string | 发布者头像 |
| isVerified | boolean | 发布者是否认证 |
| location | string | 位置信息 |
| content | string | 帖子文字内容 |
| mediaType | string | 媒体类型：image / video |
| mediaList | array | 媒体列表 |
| mediaCount | number | 媒体数量 |
| likesCount | number | 点赞数 |
| savedCount | number | 收藏数 |
| commentsCount | number | 评论数 |
| sharesCount | number | 分享数 |
| isLiked | boolean | 当前用户是否已点赞 |
| isSaved | boolean | 当前用户是否已收藏 |
| tags | array | 标签列表 |
| createdAt | number | 发布时间戳 |
| hasMore | boolean | 是否还有更多数据可加载 |

> 注：此接口不返回 `isFollowing` 字段（因为都是同一个用户的帖子）。

---

## 3. 获取用户视频列表（Reels）

### 请求信息
- **接口地址**: `GET /api/user/reels?userId=1001&page=1&pageSize=18`
- **接口说明**: 获取指定用户的视频/Reels 列表

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| userId | string | 是 | 用户 ID | - |
| page | number | 否 | 页码 | 1 |
| pageSize | number | 否 | 每页数量 | 18 |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "postId": "2010",
        "coverUrl": "https://cdn.example.com/post/video_cover_1.jpg",
        "duration": 30,
        "viewsCount": 12500
      }
    ],
    "total": 2,
    "page": 1,
    "pageSize": 18,
    "hasMore": false
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| postId | string | 帖子/视频 ID |
| coverUrl | string | 视频封面 URL |
| duration | number | 视频时长（秒） |
| viewsCount | number | 播放次数 |

---

## 4. 获取用户粉丝列表

### 请求信息
- **接口地址**: `GET /api/user/followers?userId=1001&page=1&pageSize=20`
- **接口说明**: 获取指定用户的粉丝列表，用于个人主页粉丝弹窗/粉丝列表页展示。

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| userId | string | 是 | 用户 ID | - |
| page | number | 否 | 页码 | 1 |
| pageSize | number | 否 | 每页数量 | 20 |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": "1002",
        "username": "kaihavertz29",
        "displayName": "Kai Havertz",
        "avatar": "https://cdn.example.com/avatar/2.jpg",
        "isVerified": true,
        "isFollowing": true
      },
      {
        "userId": "1003",
        "username": "messi10",
        "displayName": "Lionel Messi",
        "avatar": "https://cdn.example.com/avatar/3.jpg",
        "isVerified": true,
        "isFollowing": false
      }
    ],
    "total": 2,
    "page": 1,
    "pageSize": 20,
    "hasMore": false
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| userId | string | 用户 ID |
| username | string | 用户名 |
| displayName | string | 显示名称 |
| avatar | string | 头像 URL |
| isVerified | boolean | 是否认证 |
| isFollowing | boolean | 当前登录用户是否已关注该用户 |

---

## 5. 获取用户关注列表

### 请求信息
- **接口地址**: `GET /api/user/following?userId=1001&page=1&pageSize=20`
- **接口说明**: 获取指定用户的关注列表，用于个人主页关注弹窗/关注列表页展示。

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| userId | string | 是 | 用户 ID | - |
| page | number | 否 | 页码 | 1 |
| pageSize | number | 否 | 每页数量 | 20 |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": "1004",
        "username": "neymarjr",
        "displayName": "Neymar Jr",
        "avatar": "https://cdn.example.com/avatar/4.jpg",
        "isVerified": true,
        "isFollowing": true
      },
      {
        "userId": "1005",
        "username": "haaland9",
        "displayName": "Erling Haaland",
        "avatar": "https://cdn.example.com/avatar/5.jpg",
        "isVerified": false,
        "isFollowing": false
      }
    ],
    "total": 8,
    "page": 1,
    "pageSize": 20,
    "hasMore": false
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| userId | string | 用户 ID |
| username | string | 用户名 |
| displayName | string | 显示名称 |
| avatar | string | 头像 URL |
| isVerified | boolean | 是否认证 |
| isFollowing | boolean | 当前登录用户是否已关注该用户 |

---

## 6. 获取推荐用户列表

### 请求信息
- **接口地址**: `GET /api/user/discover?limit=10`
- **接口说明**: 获取为当前用户推荐的用户列表（"发现用户"模块）

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| limit | number | 否 | 返回数量 | 10 |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "userId": "1010",
      "username": "user_one",
      "avatar": "https://cdn.example.com/avatar/10.jpg",
      "tag": "为你推荐",
      "isFollowing": false
    },
    {
      "userId": "1011",
      "username": "user_two",
      "avatar": "https://cdn.example.com/avatar/11.jpg",
      "tag": "为你推荐",
      "isFollowing": false
    }
  ]
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| userId | string | 用户 ID |
| username | string | 用户名 |
| avatar | string | 头像 URL |
| tag | string | 推荐原因标签（后端算法生成，非数据库存储字段） |
| isFollowing | boolean | 是否已关注 |

---

## 6. 编辑用户资料

### 请求信息
- **接口地址**: `POST /api/user/profile/update`
- **接口说明**: 编辑当前用户的个人资料（显示名、个性签名等）

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| displayName | string | 否 | 显示名称 |
| bio | string | 否 | 个性签名 |
| avatar | string | 否 | 头像 URL（上传后的地址） |

### 请求示例
```json
{
  "displayName": "Qi Wang",
  "bio": "前端工程师"
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "资料更新成功",
  "data": {
    "userId": "1001",
    "username": "qiwang6189",
    "displayName": "Qi Wang",
    "avatar": "https://cdn.example.com/avatar/1.jpg",
    "bio": "前端工程师",
    "isVerified": false,
    "isPrivate": false
  }
}
```

---

## 7. 上传头像

### 请求信息
- **接口地址**: `POST /api/upload/avatar`
- **接口说明**: 上传用户头像图片，返回 CDN 地址

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |
| Content-Type | string | 是 | multipart/form-data |

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| file | File | 是 | 图片文件（支持 jpg/png，最大 5MB） |

### 响应数据
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://cdn.example.com/avatar/new_1.jpg"
  }
}
```

---

## 接口调用流程

```
个人主页加载（并行调用）
├── GET /api/user/me                              → 获取当前用户基础信息（公共接口）
├── GET /api/user/stats?userId=1001               → 获取用户统计信息（公共接口）
├── GET /api/user/posts?userId=1001&page=1        → 获取帖子网格（默认 Tab）
├── GET /api/user/followers?userId=1001&page=1    → 获取粉丝列表
├── GET /api/user/following?userId=1001&page=1    → 获取关注列表
└── GET /api/user/discover?limit=10               → 获取推荐用户

切换 Tab
├── Tab 0: GET /api/user/posts?userId=1001&page=1   → 帖子列表（已加载可缓存）
└── Tab 1: GET /api/user/reels?userId=1001&page=1   → 视频列表

用户操作
├── 关注用户: POST /api/user/follow         → 公共接口
├── 编辑资料: POST /api/user/profile/update
└── 上传头像: POST /api/upload/avatar
```
