# 首页接口文档

> 首页包含：快拍列表、帖子 Feed 流

---

## 1. 获取快拍列表

### 请求信息
- **接口地址**: `GET /api/story/feed`
- **接口说明**: 获取当前用户已关注的用户列表中有未过期快拍的用户，按最新发布时间倒序排列（最近发布快拍的用户排在最前面）。仅返回每个用户的头像和是否有未读快拍，不返回快拍详情内容。

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "storyId": "3001",
      "userId": "1002",
      "username": "kaihavertz29",
      "avatar": "https://cdn.example.com/avatar/2.jpg",
      "hasUnread": true,
      "expiredAt": 1712073600000
    },
    {
      "storyId": "3002",
      "userId": "1003",
      "username": "shio_fujiwara",
      "avatar": "https://cdn.example.com/avatar/3.jpg",
      "hasUnread": true,
      "expiredAt": 1712073600000
    }
  ]
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| storyId | string | 最新一条快拍 ID |
| userId | string | 用户 ID |
| username | string | 用户名 |
| avatar | string | 头像 URL |
| hasUnread | boolean | 是否有未读快拍 |
| expiredAt | number | 最新快拍过期时间 |

---

## 2. 获取首页帖子 Feed 流

### 请求信息
- **接口地址**: `GET /api/post/feed?lastId=0&pageSize=6`
- **接口说明**: 获取首页帖子 Feed。每次固定按“4 条关注帖 + 2 条推荐帖”目标返回；关注不足时补历史关注帖，推荐不足时补热门帖。分页基于 `lastId` 游标，而不是传统 `page` 页码。

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|-------|------|------|------|-------|
| lastId | number | 否 | 分页游标。第一页传 `0`，下一页传上一次返回的 `lastId` | 0 |
| pageSize | number | 否 | 每页数量，当前首页推荐流默认按 6 条设计 | 6 |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
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
        "savedCount": 980,
        "commentsCount": 1305,
        "sharesCount": 5423,
        "isLiked": false,
        "isSaved": false,
        "isFollowing": true,
        "createdAt": 1711987200000
      }
    ],
    "total": 6,
    "page": 1,
    "pageSize": 6,
    "hasMore": true,
    "lastId": 1995
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
| mediaList | array | 媒体列表（图片或视频） |
| mediaCount | number | 媒体数量 |
| likesCount | number | 点赞数 |
| savedCount | number | 收藏数 |
| commentsCount | number | 评论数 |
| sharesCount | number | 分享数 |
| isLiked | boolean | 当前用户是否已点赞 |
| isSaved | boolean | 当前用户是否已收藏 |
| isFollowing | boolean | 当前用户是否已关注该发布者 |
| createdAt | number | 发布时间戳 |
| lastId | number | 下一页继续请求时使用的游标 |

---

## 接口调用流程

首页加载时，前端按以下顺序并行调用：

```
页面加载
├── GET /api/user/me                       → 获取当前用户信息（显示快拍头像）
├── GET /api/story/feed                    → 获取快拍列表
└── GET /api/post/feed?lastId=0&pageSize=6 → 获取首页首批帖子
```

继续下拉时：

```
滚动触底
└── GET /api/post/feed?lastId=上次返回的lastId&pageSize=6
```
