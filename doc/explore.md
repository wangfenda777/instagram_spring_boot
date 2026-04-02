# 探索页接口文档

> 探索页包含：搜索功能、瀑布流推荐内容

---

## 1. 获取探索页推荐内容

### 请求信息
- **接口地址**: `GET /api/explore/feed`
- **接口说明**: 获取探索页瀑布流推荐内容。只返回封面图和类型等必要信息，帖子详情通过点击后单独请求。

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| page | number | 否 | 页码 | 1 |
| pageSize | number | 否 | 每页数量 | 24 |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "postId": "2001",
        "mediaType": "image",
        "coverUrl": "https://cdn.example.com/post/1.jpg",
        "mediaCount": 1
      },
      {
        "postId": "2002",
        "mediaType": "video",
        "coverUrl": "https://cdn.example.com/post/2_cover.jpg",
        "mediaCount": 1
      },
      {
        "postId": "2003",
        "mediaType": "image",
        "coverUrl": "https://cdn.example.com/post/3.jpg",
        "mediaCount": 3
      }
    ],
    "total": 200,
    "page": 1,
    "pageSize": 24,
    "hasMore": true
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| postId | string | 帖子 ID |
| mediaType | string | 类型：image / video |
| coverUrl | string | 封面图 URL |
| mediaCount | number | 媒体数量（> 1 显示多图标识） |

---

## 2. 搜索用户

### 请求信息
- **接口地址**: `GET /api/search/user`
- **接口说明**: 根据关键词搜索用户。搜索功能拆分为用户搜索和内容搜索两个接口，避免单个接口返回混合数据增加后端计算复杂度。

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| keyword | string | 是 | 搜索关键词 | - |
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
        "followersCount": 5000000,
        "isFollowing": false
      }
    ],
    "total": 15,
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
| followersCount | number | 粉丝数（方便展示热度） |
| isFollowing | boolean | 当前用户是否已关注 |

---

## 3. 搜索帖子/标签

### 请求信息
- **接口地址**: `GET /api/search/post`
- **接口说明**: 根据关键词搜索帖子内容或标签（hashtag）

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| keyword | string | 是 | 搜索关键词 | - |
| type | string | 否 | 搜索类型：all / tag / content | "all" |
| page | number | 否 | 页码 | 1 |
| pageSize | number | 否 | 每页数量 | 24 |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "postId": "2010",
        "mediaType": "image",
        "coverUrl": "https://cdn.example.com/post/10.jpg",
        "mediaCount": 2
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 24,
    "hasMore": true
  }
}
```

---

## 4. 获取帖子详情

### 请求信息
- **接口地址**: `GET /api/post/detail?postId=2001`
- **接口说明**: 点击探索页瀑布流某个内容后，获取帖子完整详情（内容、媒体列表、发布者信息、互动数据）

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| postId | string | 是 | 帖子 ID |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "postId": "2001",
    "userId": "1002",
    "username": "arsenal",
    "avatar": "https://cdn.example.com/avatar/2.jpg",
    "isVerified": true,
    "location": "Wembley Stadium",
    "content": "Looking for more of the same...",
    "mediaType": "image",
    "mediaList": [
      {
        "url": "https://cdn.example.com/post/1.jpg",
        "type": "image"
      }
    ],
    "mediaCount": 1,
    "likesCount": 315000,
    "commentsCount": 1305,
    "sharesCount": 5423,
    "isLiked": false,
    "isSaved": false,
    "isFollowing": false,
    "createdAt": 1711987200000
  }
}
```

---

## 接口调用流程

```
探索页加载
└── GET /api/explore/feed?page=1            → 获取瀑布流推荐内容

用户输入关键词搜索（并行调用）
├── GET /api/search/user?keyword=xxx        → 搜索用户
└── GET /api/search/post?keyword=xxx        → 搜索帖子/标签

点击某个帖子
└── GET /api/post/detail?postId=2001        → 获取帖子详情
```
