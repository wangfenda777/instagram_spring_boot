# 公共接口文档

> 以下接口涉及多个页面复用，统一在此管理

---

## 1. 用户登录

### 请求信息
- **接口地址**: `POST /api/auth/login`
- **接口说明**: 用户使用用户名/邮箱/手机号 + 密码登录

### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|-------|------|------|------|------|
| account | string | 是 | 用户名/邮箱/手机号 | "qiwang6189" |
| password | string | 是 | 密码 | "123456" |

### 请求示例
```json
{
  "account": "qiwang6189",
  "password": "123456"
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| token | string | 登录凭证（Bearer Token） |
| refreshToken | string | 刷新 token 的凭证 |
| expiresIn | number | token 有效期（秒） |

---

## 2. 刷新 Token

### 请求信息
- **接口地址**: `POST /api/auth/refresh`
- **接口说明**: 使用 refreshToken 换取新的 token，避免用户频繁重新登录

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| refreshToken | string | 是 | 刷新令牌 |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  }
}
```

---

## 3. 退出登录

### 请求信息
- **接口地址**: `POST /api/auth/logout`
- **接口说明**: 退出登录，服务端清除 token

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 响应数据
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

## 4. 获取当前用户基础信息

### 请求信息
- **接口地址**: `GET /api/user/me`
- **接口说明**: 获取当前登录用户的基础信息（头像、用户名、显示名等）。多个页面复用：首页顶部、个人主页、快拍等。

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "1001",
    "username": "qiwang6189",
    "displayName": "Qi Wang",
    "avatar": "https://cdn.example.com/avatar/1.jpg",
    "bio": "",
    "isVerified": false,
    "isPrivate": false,
    "hasNotification": true
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
| bio | string | 个性签名 |
| isVerified | boolean | 是否认证 |
| isPrivate | boolean | 是否私密账户 |
| hasNotification | boolean | 是否有未读通知 |

---

## 5. 获取指定用户基础信息

### 请求信息
- **接口地址**: `GET /api/user/info?userId=1002`
- **接口说明**: 获取任意用户的基础信息。首页帖子展示、他人主页等场景使用。

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | string | 是 | 目标用户 ID |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "1002",
    "username": "kaihavertz29",
    "displayName": "Kai Havertz",
    "avatar": "https://cdn.example.com/avatar/2.jpg",
    "bio": "Professional footballer",
    "isVerified": true,
    "isPrivate": false,
    "isFollowing": true
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
| bio | string | 个人简介 |
| isVerified | boolean | 是否认证 |
| isPrivate | boolean | 是否私密账户 |
| isFollowing | boolean | 当前登录用户是否已关注该用户 |

---

## 6. 获取用户统计信息

### 请求信息
- **接口地址**: `GET /api/user/stats?userId=1001`
- **接口说明**: 获取用户的帖子数、粉丝数、关注数。与基础信息分开查询，避免一个接口查询过多数据。

### 请求参数（Query）
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | string | 是 | 用户 ID |

### 响应数据
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "1001",
    "postsCount": 1,
    "followersCount": 9,
    "followingCount": 44
  }
}
```

---

## 7. 关注用户

### 请求信息
- **接口地址**: `POST /api/user/follow`
- **接口说明**: 关注指定用户。首页帖子关注按钮、个人主页发现用户、搜索结果等多处使用。

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | string | 是 | 目标用户 ID |

### 请求示例
```json
{
  "userId": "1002"
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "关注成功",
  "data": {
    "isFollowing": true
  }
}
```

---

## 8. 取消关注

### 请求信息
- **接口地址**: `POST /api/user/unfollow`
- **接口说明**: 取消关注指定用户

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| userId | string | 是 | 目标用户 ID |

### 请求示例
```json
{
  "userId": "1002"
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "取消关注成功",
  "data": {
    "isFollowing": false
  }
}
```

---

## 9. 帖子点赞

### 请求信息
- **接口地址**: `POST /api/post/like`
- **接口说明**: 点赞指定帖子。首页帖子列表、探索页帖子详情等多处使用。

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| postId | string | 是 | 帖子 ID |

### 请求示例
```json
{
  "postId": "2001"
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "isLiked": true,
    "likesCount": 12346
  }
}
```

---

## 10. 取消点赞

### 请求信息
- **接口地址**: `POST /api/post/unlike`
- **接口说明**: 取消点赞指定帖子

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| postId | string | 是 | 帖子 ID |

### 请求示例
```json
{
  "postId": "2001"
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "取消点赞",
  "data": {
    "isLiked": false,
    "likesCount": 12345
  }
}
```

---

## 11. 收藏帖子

### 请求信息
- **接口地址**: `POST /api/post/save`
- **接口说明**: 收藏指定帖子

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| postId | string | 是 | 帖子 ID |

### 请求示例
```json
{
  "postId": "2001"
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "收藏成功",
  "data": {
    "isSaved": true,
    "savedCount": 101
  }
}
```

---

## 12. 取消收藏

### 请求信息
- **接口地址**: `POST /api/post/unsave`
- **接口说明**: 取消收藏指定帖子

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| postId | string | 是 | 帖子 ID |

### 请求示例
```json
{
  "postId": "2001"
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "取消收藏",
  "data": {
    "isSaved": false,
    "savedCount": 100
  }
}
```

---

## 13. 发布帖子

### 请求信息
- **接口地址**: `POST /api/post/create`
- **接口说明**: 发布一条新帖子，需先通过上传接口获取图片 URL，再将 URL 列表传入

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| content | string | 否 | 帖子文案，可直接包含 `#travel`、`#food` 这类标签，后端会自动提取并建立标签关联 |
| location | string | 否 | 位置信息 |
| mediaType | string | 是 | 媒体类型：image / video |
| mediaUrls | array | 是 | 媒体 URL 列表（至少一项） |

### 请求示例
```json
{
  "content": "今天天气真好 #travel #food",
  "location": "上海",
  "mediaType": "image",
  "mediaUrls": [
    "/uploads/abc123.jpg",
    "/uploads/def456.jpg"
  ]
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "postId": "2010",
    "userId": "1001",
    "username": "qiwang6189",
    "avatar": "https://cdn.example.com/avatar/1.jpg",
    "isVerified": false,
    "location": "上海",
    "content": "今天天气真好",
    "mediaType": "image",
    "mediaList": [
      { "url": "/uploads/abc123.jpg", "type": "image" },
      { "url": "/uploads/def456.jpg", "type": "image" }
    ],
    "mediaCount": 2,
    "likesCount": 0,
    "savedCount": 0,
    "commentsCount": 0,
    "sharesCount": 0,
    "isLiked": false,
    "isSaved": false,
    "isFollowing": false,
    "tags": [
      { "tagId": "1", "name": "#travel", "heat": 12, "postCount": 3 },
      { "tagId": "2", "name": "#food", "heat": 8, "postCount": 2 }
    ],
    "createdAt": 1711987200000
  }
}
```

---

## 14. 编辑帖子

### 请求信息
- **接口地址**: `POST /api/post/update`
- **接口说明**: 编辑自己的帖子，可修改文案和位置信息

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| postId | number | 是 | 帖子 ID |
| content | string | 否 | 新的文案内容 |
| location | string | 否 | 新的位置信息 |

### 请求示例
```json
{
  "postId": 2010,
  "content": "修改后的文案",
  "location": "北京"
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "编辑成功",
  "data": null
}
```

---

## 15. 删除帖子

### 请求信息
- **接口地址**: `POST /api/post/delete`
- **接口说明**: 删除自己的帖子（逻辑删除），只能删除自己发布的帖子

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| postId | number | 是 | 帖子 ID |

### 请求示例
```json
{
  "postId": 2010
}
```

### 响应数据
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 16. 上传图片

### 请求信息
- **接口地址**: `POST /api/upload/image`
- **接口说明**: 上传图片文件，支持 jpg/png/gif/webp 格式。上传成功后返回图片访问 URL，可用于发布帖子等场景。

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |
| Content-Type | string | 是 | multipart/form-data |

### 请求参数（form-data）
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| file | file | 是 | 图片文件（jpg/png/gif/webp） |

### 响应数据
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "/uploads/images/a1b2c3d4e5f6.jpg",
    "originalName": "photo.jpg"
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| url | string | 文件访问路径，可直接拼接域名访问 |
| originalName | string | 原始文件名 |

---

## 17. 上传视频

### 请求信息
- **接口地址**: `POST /api/upload/video`
- **接口说明**: 上传视频文件，支持 mp4/mov/avi/webm 格式，文件大小不超过 50MB。上传成功后返回视频访问 URL，可用于发布帖子等场景。

### 请求头
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| Authorization | string | 是 | Bearer {token} |
| Content-Type | string | 是 | multipart/form-data |

### 请求参数（form-data）
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| file | file | 是 | 视频文件（mp4/mov/avi/webm），最大 50MB |

### 响应数据
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "/uploads/videos/a1b2c3d4e5f6.mp4",
    "originalName": "video.mp4"
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|-------|------|------|
| url | string | 文件访问路径，可直接拼接域名访问 |
| originalName | string | 原始文件名 |

### 错误示例
```json
{
  "code": 400,
  "message": "仅支持 mp4/mov/avi/webm 格式的视频",
  "data": null
}
```

---

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 400 | 参数错误（缺少必填参数或格式不正确） |
| 401 | 未登录或 token 失效 |
| 403 | 无权限（例如访问私密用户的内容） |
| 404 | 资源不存在（用户/帖子不存在） |
| 409 | 冲突（例如重复关注） |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
