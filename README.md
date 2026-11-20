# 生鲜即时配送调度系统

面向中小型生鲜零售 / 前置仓企业的调度管理系统，覆盖「下单 → 调度 → 配送 → 库存」全流程。项目由原「预制菜调度管理系统」改造而来，商品模型已切换为生鲜商品（蔬菜、水果、肉禽蛋、水产海鲜、乳品烘焙、粮油调味），代码层英文标识（`t_dish`、`Dish`、`/dish`）保持不变，避免存量数据与接口迁移成本。

## 技术栈

| 层次 | 选型 |
| --- | --- |
| 后端 | JDK 17、Spring Boot 3.2、MyBatis-Plus 3.5.x、MySQL 8.0、Redis、JWT、Knife4j |
| 前端 | Vue 3、Vite、Element Plus、Pinia、Axios、ECharts、高德地图 JS SDK |

## 目录结构

```
a-a-a/
├── backend/                     # Spring Boot 后端
│   ├── sql/
│   │   ├── init.sql             # 全新建库脚本（已按生鲜商品模型重建）
│   │   ├── upgrade_to_fresh_goods.sql  # 存量库升级脚本（预制菜库 → 生鲜商品模型）
│   │   ├── update_dish_add_stock.sql   # 历史补丁：商品库存冗余字段
│   │   └── prefab_dish_schedule.sql    # 预制菜版本历史数据备份（仅留档）
│   └── src/main/java/com/prefab/       # controller / service / mapper / entity / dto / vo
├── src/                         # Vue 3 前端
│   ├── api/                     # 接口封装（dish.ts 对应生鲜商品）
│   ├── views/                   # 订单 / 调度 / 配送 / 库存 / 统计 / 系统管理
│   └── router/                  # 路由与菜单
├── architecture.html            # 系统架构图
├── er-diagram.html              # 数据库 ER 图
└── flowchart.html               # 业务流程图
```

## 生鲜商品模型

`t_dish` 在原有字段基础上补充了生鲜属性：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `spec` | VARCHAR(50) | 规格，如 500g/份、30枚/盒 |
| `shelf_life_days` | INT | 保质期（天），3 天以内视为短保 |
| `storage_type` | TINYINT | 储存温层：1-常温 2-冷藏 3-冷冻 |
| `origin` | VARCHAR(100) | 产地 |
| `loss_rate` | DECIMAL(5,2) | 损耗率（%），用于库存与采购预估 |

商品分类：蔬菜 / 水果 / 肉禽蛋 / 水产海鲜 / 乳品烘焙 / 粮油调味，分类走 `t_dish_category` 表，前端不再写死品类。

## 移动端（`/m`）

移动端与 PC 端共用同一个工程和同一套后端接口，使用 Vant 4 组件库，按角色分为两个入口：

| 角色 | 账号（密码 123456） | 入口 | 功能 |
| --- | --- | --- | --- |
| 客户（role=3） | `customer01` / `customer02` | `/m/customer/home` | 浏览生鲜商品、加入购物车、地图选点下单、我的订单、取消订单 |
| 配送员（role=2） | `delivery01` | `/m/courier/tasks` | 查看分配给自己的配送任务、开始配送、确认送达、异常上报、地图导航、工作状态切换 |

- 登录页：`http://localhost:8088/m/login`（管理员 / 调度员请使用 PC 端）
- 客户下单后进入「待调度」，由 PC 端调度员在「调度管理 → 任务分配」指派给配送员，配送员即可在移动端看到该任务
- 移动端接口统一在 `MobileController`（`/mobile/**`），身份由 JWT 解析，客户和配送员的接口互相隔离
- 升级已有数据库：`mysql -uroot -p goods_schedule < backend/sql/upgrade_mobile.sql`（新增客户账号、扩展角色说明、补齐配送员档案关联，可重复执行）

### 地图选点

客户移动端下单与 PC 端「新增订单」使用同一套高德地图 SDK（`index.html` 中加载 Geocoder / PlaceSearch 插件）：

- 移动端组件：`src/components/MobileMapPicker.vue`（Vant 全屏弹层：搜索地址、点击/拖动地图选点、重新定位、逆地理编码回填）
- PC 端组件：`src/components/MapPicker.vue`（Element Dialog 版，原有逻辑）
- 选点结果会写入订单的 `longitude` / `latitude`，配送员任务详情出现「导航」按钮（唤起高德地图），客户订单详情出现「查看地图选点」
- 已有库需补充坐标字段：`mysql -uroot -p goods_schedule < backend/sql/upgrade_order_location.sql`

### 自动派单

移动端客户下单、PC 端新增订单都会在创建订单后立即自动派单（`AutoDispatchService`）：

1. 建调度任务（已有则复用）
2. 选配送员：必须已绑定登录账号、未处于「休息」，按当前任务数升序 + id 升序取最闲的一个
3. 建配送任务（待取货），订单状态置为「已调度」，配送员任务数 +1
4. 没有可用配送员时订单留在「待调度」，PC「待调度订单」页可点「自动分配」批量补救（批量调度也会顺带自动分配）

配套规则：

- 开关：`backend/src/main/resources/application.yml` 中 `dispatch.auto-assign-on-create`，设为 `false` 则全部回到人工调度
- 客户取消订单 / 配送员上报异常时，未完成的配送任务会被置为异常、调度任务取消、配送员任务额度归还；异常单还会退回「待调度」，可重新自动分配
- `/schedule/available-persons` 返回「已绑定账号且未休息」的配送员，按任务数升序，供调度员手工指派
- 演示账号密码统一为 123456：`mysql -uroot -p goods_schedule < backend/sql/fix_demo_passwords.sql`（修复历史种子数据里与文档不符的密文）

## 启动步骤

### 1. 数据库

新建库（推荐）：

```bash
mysql -uroot -p < backend/sql/init.sql
```

已有旧库（预制菜版本）需要保留历史订单时，执行升级脚本：

```bash
mysql -uroot -p prefab_dish_schedule < backend/sql/upgrade_to_fresh_goods.sql
```

升级脚本会：补充生鲜属性字段 → 旧分类改名为生鲜品类并新增 3 个品类 → 旧预制菜商品置为下架（历史订单仍可正常展示） → 导入 14 个生鲜示例商品与库存 → 同步数据字典（含新增「储存温层」）。

### 2. 后端

修改 `backend/src/main/resources/application.yml` 中的数据库与 Redis 连接信息，然后：

```bash
cd backend
mvn spring-boot:run
```

服务端口 `8888`，接口前缀 `/api`，接口文档 `http://localhost:8888/api/doc.html`。

### 3. 前端

```bash
npm install
npm run dev
```

默认代理到 `http://localhost:8888`，可在 `vite.config.ts` 中调整。

默认账号：`admin / 123456`（管理员）、`scheduler01 / 123456`（调度员）、`delivery01 / 123456`（配送员）。

## 核心模块

- **生鲜商品管理**：商品增删改查、分类维护、上下架、按分类与储存温层筛选，短保商品高亮提示。
- **订单管理**：手动下单、按商品/状态/时间查询、订单详情、超时预警。
- **调度管理**：待调度订单、按区域与时间自动分配、任务手工调整、调度日志。
- **配送管理**：配送人员、配送任务、轨迹记录、签收确认（完成后扣减库存）。
- **库存管理**：库存查询、出入库记录、阈值预警。
- **统计分析**：数据概览、订单统计、调度统计、库存统计与热销商品排行。
