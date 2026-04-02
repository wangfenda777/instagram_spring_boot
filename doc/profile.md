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

## 2. 获取用户视频列表（Reels）

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

## 3. 获取推荐用户列表

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

## 4. 编辑用户资料

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

## 5. 上传头像

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
└── GET /api/user/discover?limit=10               → 获取推荐用户

切换 Tab
├── Tab 0: GET /api/user/posts?userId=1001&page=1   → 帖子列表（已加载可缓存）
└── Tab 1: GET /api/user/reels?userId=1001&page=1   → 视频列表

用户操作
├── 关注用户: POST /api/user/follow         → 公共接口
├── 编辑资料: POST /api/user/profile/update
└── 上传头像: POST /api/upload/avatar
```
