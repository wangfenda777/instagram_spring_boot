# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and run

- `mvn spring-boot:run` — start the backend locally.
- `mvn clean package` — build the runnable jar.
- `mvn test` — run the test suite.
- `mvn -Dtest=ClassName test` — run a single test class.
- `mvn -Dtest=ClassName#methodName test` — run a single test method.

The app is a Maven Spring Boot 3.1 project targeting Java 21. Default HTTP port is `8081` in `src/main/resources/application.yml`.

## Runtime configuration

Key local settings live in `src/main/resources/application.yml`:
- MySQL datasource points to `instagram_db`.
- MyBatis-Plus uses `map-underscore-to-camel-case: false`, so entity field names are expected to match database column names directly.
- Uploaded files are stored under `upload.path` and served from `/uploads/**` via `WebConfig`.
- Knife4j / OpenAPI docs are enabled.

## Architecture overview

This is a modular Spring Boot backend for Instagram-like social features. The root package is `com.example.instagram`, and modules are organized by domain under `module/<domain>/`.

Each domain follows the same layered pattern:
- `controller` — REST endpoints under `/api/**`
- `service` and `service/impl` — business logic
- `mapper` — MyBatis-Plus persistence
- `entity` — database models
- `dto` / `vo` — request and response shapes

Current domains include `auth`, `user`, `post`, `follow`, `story`, `explore`, `upload`, and `notification`.

## Request flow and cross-cutting behavior

- `InstagramApplication` enables mapper scanning with `@MapperScan("com.example.instagram.module.*.mapper")`.
- `WebConfig` applies `AuthInterceptor` to `/api/**`, excluding login, token refresh, uploads, and API docs.
- `AuthInterceptor` reads the `Authorization: Bearer <token>` header, validates the JWT with `JwtUtil`, and stores the current user ID in `UserContext` for downstream services.
- `GlobalExceptionHandler` converts `BusinessException`, validation errors, and unexpected exceptions into the shared `Result<T>` response envelope.
- Pagination is provided centrally through `MybatisPlusConfig` with `PaginationInnerInterceptor`.

## Data and business model

The codebase is centered on denormalized social data with counters stored on rows and updated in service logic rather than derived at read time.

Examples:
- `FollowServiceImpl` inserts/deletes follow rows and updates `User.followersCount` / `User.followingCount`.
- `PostServiceImpl` creates posts plus `PostMedia` rows, and updates like/save state through `PostLike` and `PostSave` tables.
- `UserServiceImpl` reads profile-level aggregates such as post/follower/following counts directly from the `User` row.

When changing write paths, check whether a counter or related row also needs to be updated in the same transaction.

## Module responsibilities

- `auth`: login, refresh token rotation, logout. Refresh tokens are persisted in the database and revoked on refresh/logout. Most authenticated flows depend on `UserContext` being populated by the interceptor.
- `user`: current user profile, other user profiles, profile editing, profile grids/reels, and recommended users.
- `post`: post creation/edit/delete, feed listing, detail view, like/unlike, save/unsave. `buildPostFeedVO(...)` assembles feed responses by joining user info, media, like/save state, and follow state.
- `follow`: follow/unfollow actions and follower/following counter maintenance.
- `story`: returns active stories from followed users and computes unread state from `StoryView` rows.
- `explore`: trending/discovery post feed plus user/post search.
- `upload`: image upload only; stores files on disk and returns a URL under the configured upload prefix.

## API conventions

- Controllers generally return `Result.success(...)` or `Result.error(...)` envelopes rather than raw payloads.
- Authenticated endpoints usually read the actor from `UserContext` instead of trusting a user ID from the request.
- Many write endpoints use `POST` even for update/delete-style actions, so preserve existing route conventions when extending modules.
- VO classes frequently serialize IDs as strings, even when entity IDs are `Long`.
