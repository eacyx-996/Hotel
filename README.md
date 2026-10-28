# 酒店管理平台 — 项目总结

## 一、项目概述

面向中小型酒店的一体化管理后台，覆盖 **房型管理、客房管理、订单管理、用户管理、数据看板** 五大核心场景。前后端分离架构：管理端浏览器访问 Vue 前端，前端通过 REST API 调用 Spring Boot 后端，数据持久化到 MySQL，热点数据缓存到 Redis。

## 二、技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3.5、Vite 5、Element Plus 2.8、Pinia（含持久化插件）、Vue Router 4、Axios、ECharts 5.5 |
| 后端 | Spring Boot 3.4.1（Java 17）、MyBatis-Plus 3.5.7、Spring Data Redis、jjwt 0.12.6、Spring Security Crypto（BCrypt）、Lombok |
| 存储 | MySQL 8.0（4 张表）、Redis（缓存/会话） |
| 构建 | Maven（mvnw）、npm |

## 三、系统架构

```
浏览器 ──► Vite Dev Server (5173) ──/api 代理──► Spring Boot (8080, context-path /api)
                                                      │
                          JwtAuthFilter（Token 校验 + UserContext）
                                                      │
                              Controller ──► Service ──► MyBatis-Plus ──► MySQL
                                                      │
                                                      └────► Redis（缓存）
```

- **认证**：登录签发 JWT（HS512，24h），前端存 localStorage（Pinia 持久化），请求头携带 `Authorization: Bearer`；后端过滤器解析后放入 `UserContext`（ThreadLocal），请求结束清理。
- **统一响应**：`Result{code, message, data}` + 全局异常处理 + 统一分页对象 `PageResult`。
- **缓存策略**：房型列表、空闲房、房态统计、仪表盘、用户会话均入 Redis，写操作主动失效对应键。
- **数据规范**：全表逻辑删除（@TableLogic）、审计字段自动填充（MetaObjectHandler）、订单创建时冗余房型快照。

## 四、功能清单

| 模块 | 功能 |
|------|------|
| 登录认证 | 登录/退出、获取当前用户、路由守卫、401 自动跳转登录页 |
| 数据看板 | 客房/订单/营收统计卡片、近 7 天订单与营收趋势图、订单状态饼图、房态概览 |
| 房型管理 | 分页、搜索、新增/编辑/删除、启禁用 |
| 客房管理 | 分页（房型/状态筛选）、新增/编辑/删除、房态流转（空闲/预订/入住/清洁/维修） |
| 订单管理 | 分页（状态筛选）、创建订单（自动算价、校验房态）、确认/入住/退房/取消流转、支付状态管理 |
| 用户管理 | 分页、搜索、新增/编辑/删除、重置密码、角色区分 ADMIN/STAFF |

## 五、目录结构

```
Hotel/
├── Backend/                    # Spring Boot 后端
│   ├── sql/init.sql            # 数据库初始化脚本（含种子数据）
│   └── src/main/java/com/example/backend/
│       ├── config/             # JWT、Redis、MyBatis-Plus、CORS、过滤器配置
│       ├── controller/         # 6 个 REST 控制器
│       ├── service/            # 业务逻辑（含 Redis 缓存）
│       ├── mapper/             # MyBatis-Plus Mapper
│       ├── entity/             # 4 个实体
│       ├── common/             # Result / PageResult
│       └── exception/          # 全局异常处理
├── frontend/                   # Vue3 + Vite 前端
│   └── src/
│       ├── views/              # Dashboard / RoomType / Room / Order / User / Login
│       ├── layouts/            # MainLayout（侧边栏 + 顶栏）
│       ├── store/              # Pinia（用户状态持久化）
│       └── utils/request.js    # Axios 封装（Token 注入、401 处理）
└── docs/                       # 接口文档、数据库文档、项目总结
```

## 六、本地运行

前置：JDK 17+、Node 18+、MySQL 8、Redis 5+。

```powershell
# 1. 初始化数据库
mysql -uroot -proot < Backend/sql/init.sql

# 2. 启动后端（端口 8080，上下文 /api）
cd Backend; .\mvnw.cmd spring-boot:run

# 3. 启动前端（端口 5173，自动代理 /api）
cd frontend; npm install; npm run dev
```

访问 `http://localhost:5173`，管理员账号 `admin / 123456`。

## 七、测试与质量

已通过浏览器端到端实测：登录、仪表盘（含连续刷新）、房型/客房/订单/用户的列表与增删改、订单状态流转、退出登录，全部通过，控制台无报错。

实测中修复的关键问题：

1. **Bean 冲突**：`JwtAuthFilter` 同时被 `@Component` 与 `FilterConfig.@Bean` 注册导致启动失败 → 注册 Bean 改名。
2. **Redis 序列化**：`GenericJackson2JsonRedisSerializer` 默认 ObjectMapper 不支持 `LocalDateTime`，导致房型/客房/订单接口 500 → 注册 JavaTimeModule 并启用类型信息。
3. **ThreadLocal 泄漏**：`UserContext.clear()` 移入 finally。
4. **ECharts 重复初始化**：Dashboard 刷新前先 dispose 旧实例。
5. **环境兼容**：Lombok 升级 1.18.48 + 显式声明注解处理器路径，兼容高版本 JDK。
