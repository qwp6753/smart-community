# 智慧小区管理系统（全栈一期）设计文档

日期：2026-07-02  
范围：全栈一期（Web 管理端 + 移动端 + 后端 + 数据库 + 第三方能力预留）

## 1. 目标与成功标准

### 1.1 目标

- 在同一交付目录中完成三端工程初始化并跑通联调：`server`（后端）、`web-admin`（Web 管理端）、`mobile-app`（UniApp 移动端）
- 打通核心业务链路：登录鉴权（JWT）→ 菜单/按钮权限（RBAC）→ 物业管理（小区/摄像头/居民）→ 门禁管理（出入记录/访客登记/人脸识别占位）
- 第三方服务（腾讯 AI 人脸识别、百度地图）提供真实接口结构与可配置项，默认 mock 降级保证项目可启动
- 提供数据库初始化脚本与演示数据，开箱即用

### 1.2 成功标准（验收点）

- 后端可本地启动并提供 OpenAPI/Swagger 文档
- Web 端可登录、动态菜单生效、核心列表 CRUD 可用、首页图表可展示
- 移动端可登录、可进行居民查询/访客登记/照片上传入口/地图页展示
- 未配置第三方密钥时系统仍可运行（走 mock/降级逻辑）

## 2. 技术栈

- Web 前端：Vue 3.x + Element Plus + ECharts 5.x
- 移动端：UniApp + uView UI + Vue 3
- 服务端：SpringBoot 3.x + MyBatis Plus 3.5.x
- 认证授权：JWT + 拦截器
- 数据库：MySQL 8.0 + Redis 5.x
- 第三方：腾讯 AI 人脸识别 + 百度地图

## 3. 工程组织（前后端分仓风格）

在 `c:\Users\yxl\Desktop\smart-community` 下生成如下目录：

```
smart-community/
  server/                     # SpringBoot 后端
  web-admin/                  # Vue3 后台管理端
  mobile-app/                 # UniApp 移动端
  sql/                        # MySQL 初始化脚本
  docs/                       # 文档与规范
```

## 4. 总体架构与分层

### 4.1 分层原则

- 后端只暴露 REST API；前端与移动端只通过 API 访问数据
- 第三方服务统一由后端封装；前端不持有密钥
- 安全统一入口：JWT 解析 + 权限校验 + 统一异常处理
- 业务按模块拆分：系统管理、物业管理、门禁管理

### 4.2 后端模块划分

- `system`：用户、角色、菜单、RBAC、操作日志
- `property`：小区、摄像头、居民、统计聚合
- `access`：人脸识别、出入记录、访客登记

### 4.3 统一约定

- 接口统一返回结构：`code / message / data`
- 统一分页：`page / size` 入参，返回 `records / total / page / size`
- 统一审计字段：`create_time / update_time`（以及必要时的 `create_by / update_by`）
- 逻辑删除：统一 `deleted` 字段（0/1）

## 5. 业务流程与数据流

### 5.1 登录与授权

1) Web/移动端提交账号密码  
2) 后端校验用户状态与密码  
3) 生成 JWT（包含用户 ID、用户名、角色信息/权限摘要）  
4) 前端持久化 token，并在请求头携带  
5) 后端拦截器解析 JWT，写入上下文  
6) 权限注解/权限拦截校验接口访问

### 5.2 门禁识别（一期：真实接口预留 + mock 降级）

1) 移动端拍照/上传图片 → 后端上传接口  
2) 后端调用 `FaceRecognitionService`：  
   - 配置完整且可用：调用腾讯 AI  
   - 配置缺失或调用异常：走 mock/降级返回  
3) 后端匹配居民档案（`person.face_url` / 人脸特征占位）  
4) 写入 `in_out_record`（识别结果、时间、地点、照片 URL、verified）

### 5.3 地图展示（一期：展示坐标与定位，真实 AK 预留）

- 后端返回社区经纬度（`community.map_lng / community.map_lat`）
- Web/移动端渲染地图并打点；AK 未配置时提示并降级为坐标信息展示

## 6. 数据库设计（MySQL 8.0）

### 6.1 核心表

系统管理：

- `user`：user_id, username, password, real_name, mobile, status, deleted, create_time, update_time
- `role`：role_id, role_name, role_code, description, status, deleted, create_time, update_time
- `menu`：menu_id, menu_name, path, component, icon, parent_id, sort, type, permission, status, deleted, create_time, update_time
- `user_role`：user_id, role_id
- `role_menu`：role_id, menu_id

物业管理：

- `community`：community_id, name, address, map_lng, map_lat, total_building, total_house, status, deleted, create_time, update_time
- `camera`：camera_id, name, ip_address, community_id, location, status, deleted, create_time, update_time
- `person`：person_id, community_id, user_name, mobile, sex, house_no, person_type, face_url, state, remark, deleted, create_time, update_time

门禁管理：

- `in_out_record`：record_id, person_id, person_name, community_id, type, time, location, photo_url, verified
- `visitor`：visitor_id, person_id, name, mobile, id_card, community_id, visit_time, leave_time, reason, status
- `operation_log`：log_id, user_id, username, module, operation, method, params, ip, time, create_time

### 6.2 约束与索引

- 全表：InnoDB 引擎、utf8mb4 字符集、字段注释、合理默认值
- 唯一索引：`user.username`、`role.role_code`
- 普通索引：`person.mobile`、`person.community_id`、`camera.community_id`、`in_out_record.time`、`visitor.visit_time`
- 外键：`user_role.user_id -> user.user_id`、`user_role.role_id -> role.role_id`、`role_menu.role_id -> role.role_id`、`role_menu.menu_id -> menu.menu_id`、`camera.community_id -> community.community_id`、`person.community_id -> community.community_id`、`in_out_record.person_id -> person.person_id`

### 6.3 初始化脚本

- `sql/schema.sql`：建库建表（含外键）
- `sql/indexes.sql`：额外索引（如需要拆分）
- `sql/seed.sql`：管理员、基础角色、菜单树、演示小区/居民/摄像头数据

## 7. 后端接口设计（概要）

### 7.1 认证

- `POST /api/auth/login`：账号密码登录，返回 token、用户信息、权限与菜单
- `GET /api/auth/me`：获取当前用户信息

### 7.2 系统管理

- 用户：`/api/system/users`（分页、创建、更新、启停、删除）
- 角色：`/api/system/roles`（分页、创建、更新、授权菜单）
- 菜单：`/api/system/menus`（树查询、创建、更新、删除）
- 操作日志：`/api/system/operation-logs`（分页查询）

### 7.3 物业管理

- 小区：`/api/property/communities`
- 摄像头：`/api/property/cameras`
- 居民：`/api/property/persons`

### 7.4 门禁管理

- 出入记录：`/api/access/in-out-records`
- 访客登记：`/api/access/visitors`
- 人脸识别：`POST /api/access/face/verify`（上传图片/图片 URL，返回识别结果与匹配信息）

### 7.5 统计

- `GET /api/stats/overview`：首页卡片统计
- `GET /api/stats/person-distribution`：居民类型/性别/小区分布等（ECharts）

## 8. Web 管理端设计（页面与权限）

### 8.1 公共能力

- 登录页、主布局、动态路由与菜单
- 按钮级权限控制（基于权限码）
- 统一请求封装（携带 token、统一错误处理）

### 8.2 页面清单

- 仪表盘：统计卡片 + ECharts 图表
- 系统管理：用户管理、角色管理、菜单管理、操作日志
- 物业管理：小区管理（含坐标展示）、摄像头管理、居民管理（含人脸图片字段）
- 门禁管理：出入记录、访客登记

## 9. 移动端设计（页面与能力）

- 登录
- 首页（快捷入口 + 统计摘要）
- 居民查询（支持扫码结果带入搜索）
- 访客登记
- 人脸上传/识别入口（拍照/相册上传）
- 地图页（定位 + 小区坐标展示）

## 10. 第三方服务预留设计

### 10.1 腾讯 AI 人脸识别

- 后端封装 `FaceRecognitionService`，提供：
  - `verify(image)`：返回置信度、匹配人员、是否通过
  - mock 降级实现：返回可演示的固定结果/随机结果（可配置）
- 配置项（环境变量或配置文件占位）：`tencent.face.secretId/secretKey/region` 等

### 10.2 百度地图

- 后端封装 `MapService`（一期以数据回传与配置占位为主）
- Web/移动端地图组件在 AK 缺失时降级为坐标显示与提示

## 11. 安全与审计

- JWT 认证 + 权限拦截
- 密码加密存储
- 重要操作写入 `operation_log`
- 上传文件限制类型与大小，统一资源访问路径

## 12. 非功能要求落地策略

- 性能：核心列表分页、必要索引、减少 N+1 查询
- 并发：接口无状态，token 校验轻量化；Redis 用于缓存（一期先预留）
- 可用性：第三方服务失败降级，不影响主流程
- 兼容性：Web 适配 Chrome/Edge；移动端按 UniApp 官方兼容策略

## 13. 一期不做清单（明确边界）

- 摄像头实时视频流与设备协议接入
- 复杂人脸库同步、图片特征离线处理
- 推送通知、审批流、多租户隔离
- Excel 批量导入导出完整闭环（一期仅预留入口与接口结构）

