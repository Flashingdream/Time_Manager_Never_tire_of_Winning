# 时间管理系统 — 迭代变更日志

## 迭代 0：项目初始化

**基础项目搭建**
- Spring Boot 2.7 后端项目初始化（Maven + Java 11 + MySQL）
- Vue 3 前端项目初始化（Vue CLI + Element Plus + Vue Router + Vuex）
- 基础登录页面（硬编码 admin/123456）
- 前端路由守卫（localStorage.isLogin 校验）
- CORS 跨域配置
- WebSocket 配置（STOMP）

**数据库表**：`memorandums`（标题、内容、地点、标签、开始/结束时间、提醒偏移、创建/更新时间）

**API**：`/api/memos` 完整 CRUD

---

## 迭代 1：管理员与用户系统

**用户注册与登录系统**

后端新增：
- `users` 表（userId, password, role, createdAt）
- `User` 实体 / `UserDTO` / `UserRepository` / `UserService` / `UserController`
- `POST /api/users/register` — 注册端点
- `POST /api/users/login` — 登录端点（验证密码）
- `GET /api/users` — 用户列表
- `GET /api/users/{userId}` — 查单个用户
- `UserServiceImpl.initAdmin()` — 启动时自动创建默认管理员 admin/123456

前端变更：
- 登录页改为登录/注册双选项卡
- 注册需确认密码
- 登录成功存储 `role` 到 localStorage
- 管理员控制台页面（`/user/information`）：显示管理员信息 + 用户列表表格

---

## 迭代 2：权限控制

**角色权限系统**
- `User` 实体新增 `role` 字段（admin / user）
- 注册用户默认 role = "user"
- 管理员路由 `/user/information` 添加 `requireAdmin` 元数据
- 路由守卫检查 `localStorage.role === 'admin'`
- 导航栏"用户管理"链接仅对管理员可见（`v-if="isAdmin"`）
- 退出登录时清除 `role`

---

## 迭代 3：UI 美化与交互增强

**前端 UI 大修**

日历页面：
- 移除"当日事件"区域
- 新增 Edge 风格搜索栏（药丸形、聚焦阴影）
- 底部快捷添加栏（标签下拉 + 输入框 + 圆形按钮）
- 备忘录条目悬停动画

备忘录管理页：
- 新增搜索栏
- 表格新增标签列（彩色标签）
- 添加/编辑对话框增加标签下拉

全局样式：
- 所有按钮圆角 10px
- 悬停下沉动画（`translateY(-1px)`）
- 点击缩放动画（`scale(0.97)`）
- `ContentField` 卡片毛玻璃效果（`backdrop-filter: blur(10px)`）

标签颜色方案：生活(绿)/学习(蓝)/工作(橙)/娱乐(红)/社交(紫)

后端新增：
- `GET /api/memos/search?keyword=` — 关键词搜索（标题+内容模糊匹配）

---

## 迭代 4：交互功能迭代

**可点击标签、提醒时间、透明背景、头像与背景自定义**

功能变更：
- 标签可点击切换（`el-popover` + `PUT /api/memos/{id}`）
- 提醒时间可点击修改（弹出选项：准时/5/15/30 分钟前）
- 后端默认提醒时间从 5 分钟改为 **15 分钟**
- 所有卡片背景改为半透明（`rgba(255,255,255,0.72)`）
- 导航栏半透明（`rgba(44,62,80,0.92)`）
- 用户头像上传（FileReader → base64 → localStorage）
- 自定义背景图上传（同上）
- 背景透明度滑块（10%-100%，即时生效）
- `App.vue` 新增背景图层 `<div class="bg-overlay">`（z-index: -1）

---

## 迭代 5：日历增强与事件总览

**日历中文化、节假日、时间轴视图**

日历页：
- Element Plus 中文语言包（`zh-cn`）
- 日历缩小至 420px 宽度
- 日期单元格自定义渲染（`#date-cell` 插槽）
- 2026 年中国法定节假日标注（红色节日名称）
- 有事件的日期显示蓝色圆点
- 鼠标滚轮切换月份（80ms 防抖）

导航栏："添加当日事件" → "事件总览"

事件总览页（原备忘录管理页）重构：
- 今日时间轴视图（按时间排序的时间线，彩色圆点 + 卡片）
- 无事件时显示「☀ 今天没有特别规划」
- 今日日期中文显示（年月日 + 星期）
- 全部事件列表保留搜索和表格功能

---

## 迭代 6：专注模式

**专注计时与积分排名**

新增页面：`/focus` 专注模式

功能：
- 本月累计专注时长 + 累计积分卡片（渐变图标）
- 各月份专注时间统计柱状图（纯 CSS 实现）
- 月度用户专注积分排名 TOP 3（数据库查询）
- 积分规则：30 分钟 = 1 积分

全屏专注：
- 点击"开始专注"进入浏览器全屏
- 全屏界面：大号数字时钟 + 累计计时器
- 按 Esc / 点击屏幕 / 退出全屏 结束专注
- 退出后自动保存专注记录到后端

后端新增：
- `focus_records` 表（userId, yearMonth, minutes, points）
- `FocusRecord` 实体 / DTO / Repository / Service / Controller
- `POST /api/focus/save` — 保存专注记录（同月累加）
- `GET /api/focus/ranking?yearMonth=` — 月度排行 TOP 3

---

## 迭代 7：事件完成状态与考试倒计时

**完成标记、过期样式、倒计时提醒**

后端：
- `memorandums` 表新增 `completed` 字段（Boolean）
- `PUT /api/memos/{id}/toggle-complete` — 切换完成状态
- `MemorandumDTO` 包含 `completed` 字段

前端：
- 备忘录条目新增 ✓ 完成按钮
- 已完成事件：深灰背景 + 删除线
- 过期事件（endTime < 当前时间）：同样的深灰 + 删除线样式
- 表格行自动检测并应用过期样式
- 导航栏新增考试倒计时：检测含"考试"/"test"的事件，显示"还有 N 天"
- 考试倒计时徽章：红色半透明 + 脉冲呼吸动画

---

## 技术债务与已知限制

| 项目 | 说明 |
|------|------|
| 密码存储 | 明文存储，未使用哈希（bcrypt/argon2） |
| 认证 | 无 Token/JWT，仅依赖 localStorage 前端校验 |
| 状态管理 | Vuex 未使用，跨组件状态共享较困难 |
| API 调用 | 各组件直接硬编码后端 URL，无统一 API 层 |
| 日历组件 | 依赖 Element Plus el-calendar，自定义能力有限 |
| 节日数据 | 仅含 2026 年，未实现农历/动态节日计算 |
| 测试 | 后端仅 1 个冒烟测试，前端无测试 |
