# Instagram 数据库设计文档

> 基于 MySQL 8.0，使用 InnoDB 存储引擎，字符集 utf8mb4

---

## 一、数据库概述

### 1.1 数据库信息
- **数据库名称**: `instagram_db`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **存储引擎**: InnoDB
- **MySQL 版本**: 8.0+

### 1.2 设计原则
- 所有表使用 `BIGINT` 作为主键 ID，自增
- 所有表包含 `created_at` 和 `updated_at` 时间戳字段
- 使用逻辑删除（`is_deleted` 字段），不物理删除数据
- 外键关系通过索引优化，不使用物理外键约束
- 敏感字段（密码）使用加密存储
- 统计类字段（点赞数、评论数）冗余存储，提高查询性能

---

## 二、数据表设计

### 2.1 用户表 (users)

**表说明**：存储用户基础信息和账户信息

```sql
CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `display_name` VARCHAR(100) NOT NULL COMMENT '显示名称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `bio` VARCHAR(500) DEFAULT NULL COMMENT '个性签名',
  `is_verified` TINYINT(1) DEFAULT 0 COMMENT '是否认证用户 0-否 1-是',
  `is_private` TINYINT(1) DEFAULT 0 COMMENT '是否私密账户 0-否 1-是',
  `posts_count` INT DEFAULT 0 COMMENT '帖子数量（冗余字段）',
  `followers_count` INT DEFAULT 0 COMMENT '粉丝数量（冗余字段）',
  `following_count` INT DEFAULT 0 COMMENT '关注数量（冗余字段）',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 用户ID，主键自增 |
| username | VARCHAR(50) | 用户名，唯一索引 |
| email | VARCHAR(100) | 邮箱，唯一索引，可为空 |
| phone | VARCHAR(20) | 手机号，唯一索引，可为空 |
| password | VARCHAR(255) | 密码，BCrypt 加密 |
| display_name | VARCHAR(100) | 显示名称 |
| avatar | VARCHAR(500) | 头像 CDN URL |
| bio | VARCHAR(500) | 个性签名 |
| is_verified | TINYINT | 是否认证（蓝V标识） |
| is_private | TINYINT | 是否私密账户 |
| posts_count | INT | 帖子数量（冗余统计） |
| followers_count | INT | 粉丝数量（冗余统计） |
| following_count | INT | 关注数量（冗余统计） |

---

### 2.2 帖子表 (posts)

**表说明**：存储用户发布的帖子内容

```sql
CREATE TABLE `posts` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发布者用户ID',
  `content` TEXT COMMENT '帖子文字内容',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '位置信息',
  `media_type` VARCHAR(20) NOT NULL COMMENT '媒体类型 image/video',
  `media_count` INT DEFAULT 1 COMMENT '媒体数量',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL（用于列表展示）',
  `likes_count` INT DEFAULT 0 COMMENT '点赞数（冗余字段）',
  `comments_count` INT DEFAULT 0 COMMENT '评论数（冗余字段）',
  `shares_count` INT DEFAULT 0 COMMENT '分享数（冗余字段）',
  `views_count` INT DEFAULT 0 COMMENT '浏览数（视频专用）',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_media_type` (`media_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 帖子ID，主键自增 |
| user_id | BIGINT | 发布者用户ID |
| content | TEXT | 帖子文字内容 |
| location | VARCHAR(200) | 位置信息 |
| media_type | VARCHAR(20) | 媒体类型：image / video |
| media_count | INT | 媒体数量 |
| cover_url | VARCHAR(500) | 封面图（列表展示用） |
| likes_count | INT | 点赞数（冗余统计） |
| comments_count | INT | 评论数（冗余统计） |
| shares_count | INT | 分享数（冗余统计） |
| views_count | INT | 浏览数（视频专用） |

---

### 2.3 帖子媒体表 (post_media)

**表说明**：存储帖子的媒体文件（图片/视频），一个帖子可以有多个媒体

> **存储方式说明**：每个媒体文件单独一行记录，不使用逗号分隔存储多个 URL。
> 例如一个帖子有 3 张图片，则在 `post_media` 表中插入 3 条记录，通过 `sort_order` 字段控制展示顺序。
> 这样设计的好处：
> - 单独管理每个媒体文件（增删改查更灵活）
> - 可以为每个媒体单独记录类型和时长
> - 避免字符串拼接解析的性能开销和数据一致性问题
>
> **示例数据**（一个帖子包含 3 张图片）：
> | id | post_id | media_url | media_type | sort_order |
> |----|---------|-----------|------------|------------|
> | 1  | 2001    | https://cdn.example.com/post/1.jpg | image | 0 |
> | 2  | 2001    | https://cdn.example.com/post/2.jpg | image | 1 |
> | 3  | 2001    | https://cdn.example.com/post/3.jpg | image | 2 |

```sql
CREATE TABLE `post_media` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '媒体ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `media_url` VARCHAR(500) NOT NULL COMMENT '媒体文件URL',
  `media_type` VARCHAR(20) NOT NULL COMMENT '媒体类型 image/video',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `duration` INT DEFAULT NULL COMMENT '视频时长（秒），仅视频有值',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子媒体表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 媒体ID，主键自增 |
| post_id | BIGINT | 所属帖子ID |
| media_url | VARCHAR(500) | 媒体文件 CDN URL |
| media_type | VARCHAR(20) | 媒体类型：image / video |
| sort_order | INT | 排序顺序（多图时使用） |
| duration | INT | 视频时长（秒），仅视频 |

---

### 2.4 快拍表 (stories)

**表说明**：存储用户发布的快拍（24小时后自动过期）

```sql
CREATE TABLE `stories` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '快拍ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发布者用户ID',
  `media_url` VARCHAR(500) NOT NULL COMMENT '媒体文件URL',
  `media_type` VARCHAR(20) NOT NULL COMMENT '媒体类型 image/video',
  `duration` INT DEFAULT 5 COMMENT '展示时长（秒）',
  `expired_at` TIMESTAMP NOT NULL COMMENT '过期时间（24小时后）',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expired_at` (`expired_at`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='快拍表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 快拍ID，主键自增 |
| user_id | BIGINT | 发布者用户ID |
| media_url | VARCHAR(500) | 媒体文件 CDN URL |
| media_type | VARCHAR(20) | 媒体类型：image / video |
| duration | INT | 展示时长（秒） |
| expired_at | TIMESTAMP | 过期时间（发布后24小时） |

---

### 2.5 快拍阅读记录表 (story_views)

**表说明**：记录用户查看快拍的状态，用于判断 `hasUnread` 字段

```sql
CREATE TABLE `story_views` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `story_id` BIGINT UNSIGNED NOT NULL COMMENT '快拍ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '查看者用户ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '查看时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_story_user` (`story_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='快拍阅读记录表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 记录ID，主键自增 |
| story_id | BIGINT | 快拍ID |
| user_id | BIGINT | 查看者用户ID |
| created_at | TIMESTAMP | 查看时间 |

**查询 hasUnread 逻辑**：
> 查询某用户关注的人中，是否有未读快拍：
> ```sql
> -- 获取关注用户的未过期快拍，排除已读的
> SELECT s.*, u.username, u.avatar FROM stories s
> JOIN users u ON u.id = s.user_id
> JOIN follows f ON f.following_id = s.user_id AND f.follower_id = 当前用户ID
> WHERE s.expired_at > NOW() AND s.is_deleted = 0
> AND s.id NOT IN (
>   SELECT story_id FROM story_views WHERE user_id = 当前用户ID
> )
> ```

---

### 2.6 通知表 (notifications)

**表说明**：存储用户的通知消息（点赞、评论、关注等），用于支持 `hasNotification` 字段

```sql
CREATE TABLE `notifications` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收通知的用户ID',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '触发通知的用户ID',
  `type` VARCHAR(20) NOT NULL COMMENT '通知类型 like/comment/follow/mention',
  `target_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联目标ID（帖子ID或评论ID）',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容摘要',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读 0-否 1-是',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`user_id`, `is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 通知ID，主键自增 |
| user_id | BIGINT | 接收通知的用户ID |
| sender_id | BIGINT | 触发通知的用户ID |
| type | VARCHAR(20) | 通知类型：like / comment / follow / mention |
| target_id | BIGINT | 关联目标ID（帖子ID 或评论ID） |
| content | VARCHAR(500) | 通知内容摘要 |
| is_read | TINYINT | 是否已读 |

**查询 hasNotification 逻辑**：
> ```sql
> SELECT COUNT(*) > 0 AS hasNotification FROM notifications
> WHERE user_id = 当前用户ID AND is_read = 0;
> ```

---

### 2.7 关注关系表 (follows)

**表说明**：存储用户之间的关注关系

> **查询方式说明**：粉丝列表和关注列表通过同一张表的不同查询方向实现：
>
> **查询某用户的关注列表**（我关注了谁）：
> ```sql
> SELECT u.* FROM follows f
> JOIN users u ON u.id = f.following_id
> WHERE f.follower_id = 当前用户ID
> ORDER BY f.created_at DESC;
> ```
>
> **查询某用户的粉丝列表**（谁关注了我）：
> ```sql
> SELECT u.* FROM follows f
> JOIN users u ON u.id = f.follower_id
> WHERE f.following_id = 当前用户ID
> ORDER BY f.created_at DESC;
> ```
>
> **判断是否已关注某用户**：
> ```sql
> SELECT COUNT(*) FROM follows
> WHERE follower_id = 当前用户ID AND following_id = 目标用户ID;
> ```

```sql
CREATE TABLE `follows` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `follower_id` BIGINT UNSIGNED NOT NULL COMMENT '关注者用户ID',
  `following_id` BIGINT UNSIGNED NOT NULL COMMENT '被关注者用户ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 关注ID，主键自增 |
| follower_id | BIGINT | 关注者用户ID（A 关注 B，A 是 follower） |
| following_id | BIGINT | 被关注者用户ID（A 关注 B，B 是 following） |
| created_at | TIMESTAMP | 关注时间 |

**索引说明**：
- `uk_follower_following`：唯一索引，防止重复关注
- `idx_follower_id`：查询某用户关注了谁
- `idx_following_id`：查询某用户的粉丝列表

---

### 2.8 帖子点赞表 (post_likes)

**表说明**：存储用户对帖子的点赞记录

```sql
CREATE TABLE `post_likes` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 点赞ID，主键自增 |
| post_id | BIGINT | 帖子ID |
| user_id | BIGINT | 点赞用户ID |
| created_at | TIMESTAMP | 点赞时间 |

**索引说明**：
- `uk_post_user`：唯一索引，防止重复点赞
- `idx_user_id`：查询某用户点赞的帖子列表

---

### 2.9 帖子收藏表 (post_saves)

**表说明**：存储用户对帖子的收藏记录

```sql
CREATE TABLE `post_saves` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '收藏用户ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 收藏ID，主键自增 |
| post_id | BIGINT | 帖子ID |
| user_id | BIGINT | 收藏用户ID |
| created_at | TIMESTAMP | 收藏时间 |

---

### 2.10 评论表 (comments)

**表说明**：存储用户对帖子的评论

```sql
CREATE TABLE `comments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
  `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID（回复评论时使用）',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `likes_count` INT DEFAULT 0 COMMENT '点赞数（冗余字段）',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 评论ID，主键自增 |
| post_id | BIGINT | 帖子ID |
| user_id | BIGINT | 评论用户ID |
| parent_id | BIGINT | 父评论ID（回复时使用，顶级评论为 NULL） |
| content | TEXT | 评论内容 |
| likes_count | INT | 评论点赞数 |

---

### 2.11 标签表 (tag)

**表说明**：存储所有用户在帖子内容中创建或使用过的话题标签，如 `travel`、`food`、`fashion`。用于标签搜索、热门话题统计与推荐。

```sql
CREATE TABLE `tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(100) NOT NULL COMMENT '标签名，唯一，不带#，统一小写存储',
  `heat` INT DEFAULT 0 COMMENT '标签热度',
  `post_count` INT DEFAULT 0 COMMENT '关联帖子数（冗余字段）',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_heat` (`heat`),
  KEY `idx_post_count` (`post_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 标签ID，主键自增 |
| name | VARCHAR(100) | 标签名称，不带 `#`，统一小写 |
| heat | INT | 标签热度 |
| post_count | INT | 关联帖子数 |

---

### 2.12 帖子标签关联表 (post_tag)

**表说明**：建立帖子与标签的多对多关系，一条帖子可关联多个标签，一个标签也可关联多条帖子。

```sql
CREATE TABLE `post_tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 关联ID，主键自增 |
| post_id | BIGINT | 帖子ID |
| tag_id | BIGINT | 标签ID |
| created_at | TIMESTAMP | 关联创建时间 |

---

### 2.13 标签搜索记录表 (tag_search_history)

**表说明**：记录用户搜索过的标签，用于搜索推荐、最近搜索与标签热度统计。

```sql
CREATE TABLE `tag_search_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '搜索记录ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `keyword` VARCHAR(100) NOT NULL COMMENT '搜索关键词（小写）',
  `search_count` INT DEFAULT 1 COMMENT '累计搜索次数',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_keyword` (`user_id`, `keyword`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签搜索历史';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 搜索记录ID，主键自增 |
| user_id | BIGINT | 用户ID |
| keyword | VARCHAR(100) | 搜索关键词（统一小写存储） |
| search_count | INT | 累计搜索次数 |

---

### 2.14 刷新令牌表 (refresh_tokens)

**表说明**：存储用户的刷新令牌，用于 token 刷新机制

```sql
CREATE TABLE `refresh_tokens` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '令牌ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `token` VARCHAR(500) NOT NULL COMMENT '刷新令牌',
  `expired_at` TIMESTAMP NOT NULL COMMENT '过期时间',
  `is_revoked` TINYINT(1) DEFAULT 0 COMMENT '是否已撤销 0-否 1-是',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expired_at` (`expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷新令牌表';
```

**字段说明**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 令牌ID，主键自增 |
| user_id | BIGINT | 用户ID |
| token | VARCHAR(500) | 刷新令牌（JWT） |
| expired_at | TIMESTAMP | 过期时间 |
| is_revoked | TINYINT | 是否已撤销（退出登录时撤销） |

---

## 三、表关系说明

### 3.1 ER 关系图

```
users (用户表)
  ├── 1:N → posts (帖子表)
  ├── 1:N → stories (快拍表)
  ├── 1:N → story_views (快拍阅读记录表)
  ├── 1:N → notifications (通知表，作为 user_id 接收方)
  ├── 1:N → notifications (通知表，作为 sender_id 发送方)
  ├── 1:N → follows (关注关系表，作为 follower_id)
  ├── 1:N → follows (关注关系表，作为 following_id)
  ├── 1:N → post_likes (点赞表)
  ├── 1:N → post_saves (收藏表)
  ├── 1:N → comments (评论表)
  ├── 1:N → refresh_tokens (刷新令牌表)
  └── 1:N → tag_search_history (标签搜索记录表)

posts (帖子表)
  ├── 1:N → post_media (媒体表)
  ├── 1:N → post_likes (点赞表)
  ├── 1:N → post_saves (收藏表)
  ├── 1:N → comments (评论表)
  └── 1:N → post_tag (帖子标签关联表)

tag (标签表)
  └── 1:N → post_tag (帖子标签关联表)

stories (快拍表)
  └── 1:N → story_views (快拍阅读记录表)

comments (评论表)
  └── 1:N → comments (自关联，parent_id)
```

### 3.2 关系说明

1. **用户与帖子**：一对多关系，一个用户可以发布多个帖子
2. **帖子与媒体**：一对多关系，一个帖子可以包含多个图片/视频
3. **用户与快拍**：一对多关系，一个用户可以发布多条快拍
4. **快拍与阅读记录**：一对多关系，一条快拍可以被多个用户查看
5. **用户与通知**：一对多关系，一个用户可以收到多条通知
6. **用户与关注**：多对多关系，通过 `follows` 表实现
7. **用户与点赞**：多对多关系，通过 `post_likes` 表实现
8. **用户与收藏**：多对多关系，通过 `post_saves` 表实现
9. **帖子与评论**：一对多关系，一个帖子可以有多条评论
10. **评论与回复**：自关联关系，通过 `parent_id` 实现评论的回复
11. **帖子与标签**：多对多关系，通过 `post_tag` 表实现
12. **用户与标签搜索记录**：一对多关系，一个用户可以有多条标签搜索记录

---

## 四、索引设计说明

### 4.1 主键索引
所有表都使用 `id` 作为主键，自增 BIGINT 类型。

### 4.2 唯一索引
- `users.username`：用户名唯一
- `users.email`：邮箱唯一
- `users.phone`：手机号唯一
- `follows.follower_id + following_id`：防止重复关注
- `post_likes.post_id + user_id`：防止重复点赞
- `post_saves.post_id + user_id`：防止重复收藏
- `story_views.story_id + user_id`：防止重复阅读记录
- `refresh_tokens.token`：令牌唯一
- `tag.name`：标签名唯一
- `post_tag.post_id + tag_id`：防止帖子重复关联同一标签
- `tag_search_history.user_id + keyword`：同一用户对同一搜索词保留一条历史记录

### 4.3 普通索引
- `posts.user_id`：查询某用户的帖子列表
- `posts.created_at`：按时间排序查询
- `post_media.post_id`：查询帖子的媒体列表
- `stories.user_id`：查询某用户的快拍
- `stories.expired_at`：查询未过期的快拍
- `notifications.user_id + is_read`：查询某用户的未读通知
- `notifications.created_at`：按时间排序通知列表
- `comments.post_id`：查询帖子的评论列表
- `comments.user_id`：查询某用户的评论
- `tag.heat`：热门标签排序
- `tag.post_count`：按关联帖子数排序标签
- `post_tag.post_id`：查询帖子关联标签
- `post_tag.tag_id`：按标签查询帖子
- `tag_search_history.user_id`：查询用户最近搜索标签
- `tag_search_history.keyword`：按搜索词聚合或检索历史

---

## 五、数据库初始化 SQL

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `instagram_db` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `instagram_db`;

-- 按顺序执行上述各表的 CREATE TABLE 语句
-- 1. users
-- 2. posts
-- 3. post_media
-- 4. stories
-- 5. story_views
-- 6. notifications
-- 7. follows
-- 8. post_likes
-- 9. post_saves
-- 10. comments
-- 11. tag
-- 12. post_tag
-- 13. tag_search_history
-- 14. refresh_tokens
```

---

## 六、性能优化建议

### 6.1 冗余字段设计
- `users` 表中的 `posts_count`、`followers_count`、`following_count` 为冗余统计字段
- `posts` 表中的 `likes_count`、`comments_count`、`shares_count` 为冗余统计字段
- `tag` 表中的 `post_count`、`heat` 为冗余统计字段
- 冗余字段通过触发器或应用层代码维护，避免频繁 COUNT 查询

### 6.2 分页查询优化
- 使用 `created_at` 或 `id` 作为分页游标，避免 OFFSET 大数据量时性能问题
- 示例：`WHERE id < last_id ORDER BY id DESC LIMIT 20`

### 6.3 缓存策略
- 用户基础信息（`users` 表）：Redis 缓存，TTL 30分钟
- 帖子详情：Redis 缓存，TTL 10分钟
- 点赞/收藏状态：Redis Set 结构，实时更新
- 关注关系：Redis Set 结构，实时更新

### 6.4 读写分离
- 主库：写操作（INSERT、UPDATE、DELETE）
- 从库：读操作（SELECT）
- 统计类查询（点赞数、评论数）优先从从库读取

---

## 七、数据安全与备份

### 7.1 敏感数据加密
- `users.password`：使用 BCrypt 加密存储
- `refresh_tokens.token`：使用 JWT 签名

### 7.2 逻辑删除
- 所有核心表（`users`、`posts`、`stories`、`comments`）使用 `is_deleted` 字段实现逻辑删除
- 物理删除仅在数据归档时执行

### 7.3 备份策略
- 全量备份：每天凌晨 2:00 执行
- 增量备份：每 6 小时执行一次
- Binlog 保留 7 天，用于数据恢复

---

## 八、扩展性设计

### 8.1 分库分表
当数据量增长到千万级别时，考虑以下分表策略：
- `posts` 表：按 `user_id` 哈希分表
- `post_likes` 表：按 `post_id` 哈希分表
- `follows` 表：按 `follower_id` 哈希分表

### 8.2 未来功能预留
- 私信消息表（`messages`）
- 举报表（`reports`）
- 黑名单表（`blocks`）

---

**文档版本**: v1.0  
**最后更新**: 2026-04-02  
**维护人**: 启哥
