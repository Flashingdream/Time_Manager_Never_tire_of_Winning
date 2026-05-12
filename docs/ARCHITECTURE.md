# 时间管理系统 — 代码架构文档

## 项目概览

时间管理系统是一个基于 **Spring Boot 2.7 + Vue 3** 的全栈单页应用（SPA），支持用户注册登录、备忘录/事件管理、日历视图、专注计时与积分排名等功能。

```
技术栈:
  前端: Vue 3 (Composition API) + Vue Router 4 + Axios + Element Plus + WebSocket (STOMP)
  后端: Spring Boot 2.7.18 + Spring Data JPA + MySQL + WebSocket
  语言: Java 11 / JavaScript (ES2020+)
```

---

## 1. 项目结构

```
project/
├── demo/
│   ├── backend/                          # Spring Boot 后端
│   │   ├── pom.xml                       # Maven 配置 (Java 11, SB 2.7.18)
│   │   └── src/main/
│   │       ├── java/time_manager/demo/
│   │       │   ├── DemoApplication.java  # 入口: @SpringBootApplication + @EnableScheduling
│   │       │   └── java/com/
│   │       │       ├── config/           # CorsConfig, WebSocketConfig
│   │       │       ├── controller/       # REST 控制器层
│   │       │       ├── dto/              # 数据传输对象
│   │       │       ├── entity/           # JPA 实体 (对应数据库表)
│   │       │       ├── repository/       # Spring Data JPA 接口
│   │       │       └── service/          # 业务逻辑层
│   │       └── resources/
│   │           └── application.properties # 数据库/服务器配置
│   │
│   └── frontend/                         # Vue 3 前端
│       ├── package.json                  # 依赖: vue3, element-plus, axios, sockjs
│       ├── vue.config.js                 # 开发代理配置
│       └── src/
│           ├── main.js                   # 入口: 创建 Vue 应用
│           ├── App.vue                   # 根组件 (背景层 + NavBar + RouterView)
│           ├── router/index.js           # 路由表 + 导航守卫
│           ├── store/index.js            # Vuex (未使用)
│           ├── notification.js           # WebSocket 提醒通知
│           ├── components/               # 公共组件
│           │   ├── ContentField.vue      # 毛玻璃卡片容器
│           │   └── NabBar.vue            # 顶部导航栏
│           └── views/                    # 页面组件
│               ├── login/LoginView.vue
│               ├── calendar/CalendarIndexView.vue
│               ├── memorandum/MemorandumIndexView.vue
│               ├── event/EventIndexView.vue
│               ├── focus/FocusView.vue
│               └── user/information/UserInformationIndexView.vue
└── docs/                                 # 文档
```

---

## 2. 后端架构

### 2.1 分层架构

```
┌─────────────────────────────────────────┐
│              Controller 层               │  ← REST API 端点, @RestController
│   UserController / MemorandumController  │
│   FocusRecordController                  │
├─────────────────────────────────────────┤
│              Service 层                  │  ← 业务逻辑, @Service
│   UserService / MemorandumService       │
│   FocusRecordService / ReminderService  │
├─────────────────────────────────────────┤
│             Repository 层               │  ← 数据访问, JpaRepository
│   UserRepository / MemorandumRepository │
│   FocusRecordRepository                 │
├─────────────────────────────────────────┤
│              Entity 层                  │  ← JPA 实体, @Entity
│   User / Memorandum / FocusRecord       │
└─────────────────────────────────────────┘
```

### 2.2 数据库表（JPA ddl-auto=update 自动维护）

**users 表**
| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 自增主键 |
| user_id | VARCHAR(50) UNIQUE NOT NULL | 用户标识名 |
| password | VARCHAR(100) NOT NULL | 明文密码 |
| role | VARCHAR(20) NOT NULL | 角色: admin / user |
| created_at | DATETIME | 注册时间 |

**memorandums 表**
| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 自增主键 |
| title | VARCHAR(100) NOT NULL | 事件标题 |
| content | VARCHAR(500) NOT NULL | 事件内容 |
| location | VARCHAR(200) | 地点 |
| tag | VARCHAR(20) | 标签: 生活/学习/工作/娱乐/社交 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| reminder_offset | INT | 提醒偏移(分钟): 0/5/15/30 |
| completed | BOOLEAN NOT NULL | 是否已完成 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

**focus_records 表**
| 列名 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 自增主键 |
| user_id | VARCHAR(50) NOT NULL | 用户标识 |
| year_month | VARCHAR(7) NOT NULL | 年月 (如 2026-05) |
| minutes | INT NOT NULL | 累计专注分钟 |
| points | INT NOT NULL | 累计积分 (minutes/30) |

### 2.3 API 端点汇总（13 个 REST + 1 WebSocket）

```
POST   /api/users/register        注册 (body: userId, password)
POST   /api/users/login           登录 (body: userId, password)
GET    /api/users                 用户列表
GET    /api/users/{userId}        查询用户

POST   /api/focus/save            保存专注记录 (body: userId, yearMonth, minutes)
GET    /api/focus/ranking         月度排行 TOP3 (?yearMonth=)
GET    /api/focus/user            用户月记录 (?userId=&yearMonth=)

GET    /api/memos                 全部备忘录
GET    /api/memos/search          关键词搜索 (?keyword=)
GET    /api/memos/{id}            查询单条
POST   /api/memos                 创建
PUT    /api/memos/{id}            更新
PUT    /api/memos/{id}/toggle-complete  切换完成状态
DELETE /api/memos/{id}            删除

WebSocket /ws (STOMP)             → /topic/reminders 定时提醒推送
```

### 2.4 关键设计决策

- **认证方式**：前端 localStorage (`isLogin`, `username`, `role`) + 前端路由守卫，后端无 Session/Token
- **密码存储**：明文（未做哈希），仅用于简单项目演示
- **管理员初始化**：`UserServiceImpl.initAdmin()` (@PostConstruct) 启动时自动插入 admin/123456
- **默认提醒时间**：备忘录创建时默认 `reminderOffset = 15` 分钟
- **定时任务**：`ReminderService` 每分钟扫描即将开始的事件，通过WebSocket推送提醒

---

## 3. 前端架构

### 3.1 路由设计

| 路径 | 组件 | 登录要求 | 权限要求 |
|------|------|----------|----------|
| `/login` | LoginView.vue | 否 | — |
| `/calendar` | CalendarIndexView.vue | 是 | — |
| `/memorandum` | MemorandumIndexView.vue | 是 | — |
| `/event` | EventIndexView.vue | 是 | — |
| `/focus` | FocusView.vue | 是 | — |
| `/user/information` | UserInformationIndexView.vue | 是 | admin |

**路由守卫逻辑** (`router.beforeEach`):
1. 检查 `localStorage.isLogin === 'true'`
2. 如果需要管理员权限 (`requireAdmin`)，检查 `localStorage.role === 'admin'`

### 3.2 组件树

```
App.vue
├── .bg-overlay (背景图层，z-index: -1)
├── NabBar.vue (v-if="isLogin")
│   ├── 导航链接 (router-link)
│   ├── 考试倒计时 (条件渲染)
│   ├── 设置弹窗 (ElPopover)
│   │   ├── 头像上传 (FileReader → base64 → localStorage)
│   │   ├── 背景图上传 (同上)
│   │   └── 透明度滑块
│   ├── 用户头像
│   └── 退出按钮
└── router-view
    ├── LoginView.vue           (/login)
    ├── CalendarIndexView.vue   (/calendar)
    ├── MemorandumIndexView.vue (/memorandum)
    ├── EventIndexView.vue      (/event)
    ├── FocusView.vue           (/focus)
    └── UserInformationIndexView.vue (/user/information)
```

### 3.3 数据流

```
用户操作 → axios → 后端 REST API → MySQL
                ↕
          localStorage (isLogin, username, role, avatar, bgImage, focusData)
                ↕
          Vue 响应式状态 (ref, computed)
                ↕
          组件重新渲染
```

- **axios 调用方式**：各页面组件内部直接设置 `axios.defaults.baseURL = 'http://localhost:8080/api'` 并调用
- **状态管理**：未使用 Vuex，全部通过 `localStorage` + 组件内 `ref()` 管理
- **API 响应格式**：统一 `{ code: 200/400, msg: "...", data: ... }`

### 3.4 关键前端模式

- **Composition API**：所有页面使用 `<script setup>` 语法
- **Element Plus 中文**：`main.js` 中 `app.use(ElementPlus, { locale: zhCn })`
- **毛玻璃背景**：`ContentField.vue` 卡片使用 `backdrop-filter: blur(10px)` + 半透明 `rgba` 背景
- **图表**：专注月统计使用纯 CSS 柱状图（无需图表库）

---

## 4. 配置汇总

| 配置项 | 值 | 位置 |
|--------|-----|------|
| 后端端口 | 8080 | application.properties |
| 前端开发端口 | 8081 | vue.config.js (默认) |
| 数据库 | MySQL / user_event_info | application.properties |
| 数据库用户 | root | application.properties |
| JPA DDL | update (自动建表) | application.properties |
| CORS 允许 | localhost:8081, 8082 | CorsConfig.java |
| 默认管理员 | admin / 123456 | UserServiceImpl.initAdmin() |
| 默认提醒 | 15 分钟前 | MemorandumServiceImpl |
| WebSocket | /ws (SockJS/STOMP) | WebSocketConfig.java |
