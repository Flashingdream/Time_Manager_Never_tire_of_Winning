# 后端迭代技术总结

## 一、架构总览

```
请求 → Controller (@RestController) → Service (@Service) → Repository (JpaRepository) → MySQL
                                              ↕
                                         Entity → DTO (转换隔离)
                                              ↕
                              ReminderService (@Scheduled) → WebSocket (/topic/reminders)
```

**核心技术栈**：Spring Boot 2.7.18 · Spring Data JPA (Hibernate) · MySQL 8.x · WebSocket (STOMP) · JUnit 5

---

## 二、各迭代后端实现

### 迭代 0 — 项目初始化与备忘录 CRUD

**目标**：搭建基础后端，实现备忘录的增删改查。

**实现技术**：
- `DemoApplication.java` — `@SpringBootApplication` + `@EnableScheduling` 入口
- `CorsConfig.java` — 实现 `WebMvcConfigurer`，允许 `localhost:8081/8082` 跨域访问 `/api/**`
- `Memorandum.java` — JPA 实体，映射 `memorandums` 表，11 个字段（id/title/content/location/tag/startTime/endTime/reminderOffset/createdAt/updatedAt）
- `MemorandumDTO.java` — 数据传输对象，`@JsonFormat` 序列化日期为 `yyyy-MM-dd HH:mm:ss`
- `MemorandumRepository.java` — `JpaRepository<Memorandum, Long>`，含自定义 JPQL `findByStartTimeBetween`
- `MemorandumService.java` / `MemorandumServiceImpl.java` — 标准 CRUD 接口与实现
- `MemorandumController.java` — `@RestController`，路径 `/api/memos`，5 个端点（GET all / GET by id / POST / PUT / DELETE）
- `ApiResponse.java` — 统一响应格式 `{code, msg, data}`

**关键模式**：
- **DTO 隔离**：Entity 不直接暴露给前端，通过 `convertToDTO()` 私有方法转换
- **手动映射**：不使用 ModelMapper/MapStruct，逐字段 set/get
- **时间戳自维护**：Entity 的 setter 方法自动更新 `updatedAt = LocalDateTime.now()`

---

### 迭代 1 — 用户系统

**目标**：建立用户注册/登录/管理功能，`users` 表存储。

**实现技术**：
- `User.java` — 实体，字段：id / userId(unique) / password / role / createdAt
- `UserDTO.java` — 不含 `password` 字段（防止密码泄露到 API 响应）
- `UserRepository.java` — `findByUserId(String)` + `existsByUserId(String)`
- `UserService.java` / `UserServiceImpl.java` — `registerUser` / `loginUser` / `getAllUsers` / `getUserByUserId`
- `UserController.java` — `/api/users`，4 个端点

**关键模式**：
- **密码明文比对**：`existing.get().getPassword().equals(password)`（String.equals）
- **注册重复检测**：`existsByUserId()` → 重复返回 null → Controller 映射 400
- **管理员自动创建**：`@PostConstruct` 方法 `initAdmin()` 启动时检测无 admin 则插入

---

### 迭代 2 — 权限控制

**目标**：为 User 实体增加 `role` 字段区分管理员与普通用户。

**实现技术**：
- `User.java` — 新增 `@Column(nullable = false, length = 20) String role`
- `UserServiceImpl.registerUser` — 构造 `new User(userId, password, "user")`，默认角色为普通用户
- 构造函数改为三参数 `User(String userId, String password, String role)`

**关键模式**：
- **前端权限**：后端不检查 role（无 Spring Security），仅通过 DTO 将 role 返回前端，由前端路由守卫校验
- **最小改动**：后端仅存储和传递 role，不做鉴权判断

---

### 迭代 3 — 搜索功能

**目标**：支持按关键词模糊搜索备忘录。

**实现技术**：
- `MemorandumRepository.java` — 新增 JPQL：
  ```sql
  WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
     OR LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
  ```
- `MemorandumService.java` — 接口新增 `searchMemorandums(String keyword)`
- `MemorandumServiceImpl.java` — 委托 Repository 查询
- `MemorandumController.java` — 新增 `GET /api/memos/search?keyword=`，空关键词返回全部

**关键模式**：
- **大小写不敏感**：使用 `LOWER()` 函数包装字段和关键词
- **降级策略**：空/空白关键词退回 `getAllMemorandums()`

---

### 迭代 4 — 默认提醒时间调整

**目标**：将备忘录默认提醒偏移从 5 分钟改为 15 分钟。

**实现技术**：
- `MemorandumServiceImpl.java` — 两处改动：
  ```java
  // createMemorandum (line 42)
  memorandum.setReminderOffset(dto.getReminderOffset() == null ? 15 : dto.getReminderOffset());
  // updateMemorandum (line 60)
  memorandum.setReminderOffset(dto.getReminderOffset() == null ? 15 : dto.getReminderOffset());
  ```

**关键模式**：
- **层次覆盖**：Service 层的默认值覆盖了 Entity 构造函数的默认值（5 → 15），实际生效的是 Service 层的判断逻辑

---

### 迭代 5 — 无后端变更

**前端独占迭代**（日历中文化、节假日、时间轴），后端无变化。

---

### 迭代 6 — 专注模式后端

**目标**：支持专注记录的持久化存储和月度积分排行。

**新增内容**：

**实体层**：`FocusRecord.java`
- 映射 `focus_records` 表
- 字段：id / userId / yearMonth("YYYY-MM") / minutes / points

**DTO 层**：`FocusRecordDTO.java`

**Repository 层**：`FocusRecordRepository.java`
- `findByUserIdAndYearMonth(String userId, String yearMonth)` — 精确查找用户某月记录
- `findByYearMonthOrderByPointsDesc(String yearMonth)` — 按积分降序排列

**Service 层**：`FocusRecordService.java` / `FocusRecordServiceImpl.java`
- `saveFocus` — Upsert 逻辑：同用户同月则累加，否则新建
- 积分公式：`points = totalMinutes / 30`（整数除法）
- `getMonthlyRanking(yearMonth, limit)` — 返回前 N 名

**Controller 层**：`FocusRecordController.java`
- `POST /api/focus/save` — body: `{userId, yearMonth, minutes}`
- `GET /api/focus/ranking?yearMonth=` — 返回 TOP 3
- `GET /api/focus/user?userId=&yearMonth=` — 查询个人月记录

**关键决策**：
- **Upsert 累积**：不创建新的 FocusRecord，而是累加到已有记录上
- **积分重算**：每次 save 时 `points = minutes / 30`，基于累计分钟重新计算
- **排名限制硬编码**：Controller 中固定 `limit = 3`

---

### 迭代 7 — 事件完成状态

**目标**：支持将备忘录标记为已完成。

**实现技术**：
- `Memorandum.java` — 新增 `@Column(nullable = false) Boolean completed = false`
- `MemorandumDTO.java` — 新增 `completed` 字段，构造函数增加 1 参数
- `MemorandumService.java` — 接口新增 `toggleComplete(Long id)`
- `MemorandumServiceImpl.java` — 实现：
  ```java
  m.setCompleted(!Boolean.TRUE.equals(m.getCompleted()));
  m.setUpdatedAt(LocalDateTime.now());
  ```
- `MemorandumController.java` — 新增 `PUT /api/memos/{id}/toggle-complete`

**关键决策**：
- **null 安全切换**：使用 `!Boolean.TRUE.equals()` 而非 `!m.getCompleted()`，防止 NPE
- **手动时间戳**：Service 层显式设置 `updatedAt`，因为 Entity 的 `setCompleted` 未实现自动更新（与其他 setter 不一致）

---

## 三、数据流向总图

```
┌──────────────────────────────────────────────────────────┐
│                    MySQL (user_event_info)               │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │  users   │  │ memorandums  │  │  focus_records   │   │
│  │          │  │              │  │                  │   │
│  │ 3 行     │  │ 11 列        │  │ 5 列             │   │
│  └──────────┘  └──────────────┘  └──────────────────┘   │
└───────────────┬──────────────────┬──────────────────────┘
                │                  │
    ┌───────────┴──┐    ┌─────────┴──────────┐
    │ UserRepo     │    │ MemorandumRepo     │  FocusRecordRepo
    │ JpaRepository│    │ JpaRepository      │  JpaRepository
    └───────┬──────┘    └─────────┬──────────┘
            │                     │
    ┌───────┴──────┐    ┌─────────┴──────────┐
    │ UserService  │    │ MemorandumService  │  FocusRecordService
    │ (Impl)       │    │ (Impl)             │  (Impl)
    └───────┬──────┘    └─────────┬──────────┘
            │                     │
    ┌───────┴──────┐    ┌─────────┴──────────┐
    │UserController│    │MemorandumController│  FocusRecordController
    │ /api/users   │    │ /api/memos         │  /api/focus
    └───────┬──────┘    └─────────┬──────────┘
            │                     │
            └─────────────────────┴──────────────→ 前端 Vue 3
                                                      ↓
                                              ReminderService
                                              (@Scheduled 60s)
                                              → WebSocket /topic/reminders
```

## 四、单元测试

### 4.1 现有测试

| 文件 | 内容 | 结果 |
|------|------|------|
| `DemoApplicationTests.java` (14 行) | `@SpringBootTest` + `contextLoads()` | 验证 Spring 容器正常启动（冒烟测试） |

### 4.2 测试方法建议

**当前测试覆盖率为 0%**（无业务逻辑测试）。以下是推荐的测试方案：

#### 控制器层测试（`@WebMvcTest`）

```java
@WebMvcTest(MemorandumController.class)
class MemorandumControllerTest {

    @MockBean
    private MemorandumService memorandumService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllMemorandums_ShouldReturn200() throws Exception {
        when(memorandumService.getAllMemorandums()).thenReturn(List.of());

        mockMvc.perform(get("/api/memos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void createMemorandum_ShouldReturnCreated() throws Exception {
        MemorandumDTO dto = new MemorandumDTO();
        dto.setTitle("测试");
        dto.setContent("测试内容");
        when(memorandumService.createMemorandum(any())).thenReturn(dto);

        mockMvc.perform(post("/api/memos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"测试\",\"content\":\"测试内容\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

#### 服务层测试（`@SpringBootTest` + `@DataJpaTest`）

```java
@DataJpaTest
class MemorandumServiceImplTest {

    @Autowired
    private MemorandumRepository memorandumRepository;

    private MemorandumServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MemorandumServiceImpl();
        // 注入 repository（需反射或改用 @SpringBootTest）
    }

    @Test
    void createMemorandum_ShouldSetDefaultReminder15() {
        MemorandumDTO dto = new MemorandumDTO();
        dto.setTitle("测试");
        dto.setContent("内容");
        dto.setReminderOffset(null);

        MemorandumDTO result = service.createMemorandum(dto);
        // 验证 reminderOffset = 15...
    }

    @Test
    void toggleComplete_ShouldFlipCompleted() {
        Memorandum m = new Memorandum();
        m.setCompleted(false);
        memorandumRepository.save(m);

        service.toggleComplete(m.getId());
        Optional<Memorandum> updated = memorandumRepository.findById(m.getId());
        assertTrue(updated.get().getCompleted());
    }
}
```

#### Repository 层测试

```java
@DataJpaTest
class MemorandumRepositoryTest {

    @Autowired
    private MemorandumRepository repository;

    @Test
    void searchByKeyword_ShouldFindByTitle() {
        Memorandum m = new Memorandum();
        m.setTitle("考试复习");
        m.setContent("准备期末考试");
        repository.save(m);

        List<Memorandum> results = repository.searchByKeyword("考试");
        assertFalse(results.isEmpty());
    }

    @Test
    void searchByKeyword_ShouldBeCaseInsensitive() {
        Memorandum m = new Memorandum();
        m.setTitle("Test Exam");
        m.setContent("important");
        repository.save(m);

        List<Memorandum> results = repository.searchByKeyword("test");
        assertFalse(results.isEmpty());
    }
}
```

#### 用户服务测试

```java
@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerUser_ShouldCreateWithUserRole() {
        UserDTO result = userService.registerUser("testuser", "pass123");
        assertEquals("user", result.getRole());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void registerUser_ShouldRejectDuplicate() {
        userService.registerUser("duplicate", "pass");
        UserDTO result = userService.registerUser("duplicate", "other");
        assertNull(result); // 重复应返回 null
    }

    @Test
    void loginUser_ShouldVerifyPassword() {
        userService.registerUser("logintest", "correct");
        UserDTO result = userService.loginUser("logintest", "correct");
        assertNotNull(result);

        UserDTO fail = userService.loginUser("logintest", "wrong");
        assertNull(fail);
    }
}
```

### 4.3 测试覆盖率目标

| 层级 | 推荐工具 | 覆盖内容 |
|------|----------|----------|
| Controller | `@WebMvcTest` + MockMvc | 端点可达性、参数校验、HTTP 状态码 |
| Service | `@SpringBootTest` 或纯单元测试 | 业务逻辑、边界条件、默认值 |
| Repository | `@DataJpaTest` | JPQL 查询、关键字搜索、大小写 |
| 集成测试 | `@SpringBootTest(webEnvironment=RANDOM_PORT)` | 端到端 API 调用流程 |

## 五、已知技术债务

| 问题 | 严重程度 | 位置 |
|------|----------|------|
| 密码明文存储与比较 | 高 | `User.java` / `UserServiceImpl.loginUser()` |
| `show-sql=true` 泄露 SQL | 中 | `application.properties` |
| 硬编码管理员凭据 | 中 | `UserServiceImpl.initAdmin()` |
| 无任何鉴权机制 | 高 | 所有 Controller |
| 无全局异常处理 | 中 | 无 `@ControllerAdvice` |
| DELETE 无存在性检查 | 低 | `MemorandumServiceImpl.deleteMemorandum()` |
| `setCompleted` 不自动更新 `updatedAt` | 低 | `Memorandum.java` |
| 排名 limit 硬编码 | 低 | `FocusRecordController.getRanking()` |
| 无 `@Transactional` 显式声明 | 低 | 所有 Service |
| 零业务测试覆盖率 | 中 | `src/test` |
| Lombok 引入但未使用 | 低 | `pom.xml` |
