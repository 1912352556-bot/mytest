---
trigger: always_on
---
项目规则说明
技术栈：后端（JDK17 + SpringBoot + MyBatis-Plus + MySQL）、前端（Vue3 + Vite）
一、通用基础规则
版本环境：后端统一使用 JDK17，前端 Node.js 18.x，依赖版本锁定，文件编码 UTF-8，换行符 LF。
命名规范：
后端类名大驼峰，方法 / 变量小驼峰，常量全大写 + 下划线
数据库表 / 字段小写 + 下划线，表名前缀 t_
前端组件名大驼峰 / 短横线，接口 URL 小写 + 短横线
Git 规范：分支分为 master、dev、feature/xxx、bugfix/xxx；提交信息格式：类型: 描述（如 feat: 新增订单调度接口）
二、后端规则
SpringBoot 规范：
启动类命名为 XXXApplication，根包下
配置用 application.yml，按环境拆分，通过 @ConfigurationProperties 绑定配置
全局异常处理用 @RestControllerAdvice，接口返回统一封装 Result<T>
MyBatis-Plus 规范：
实体类 @TableName+@TableId，主键策略自增用 AUTO，分布式用 ASSIGN_ID
查询用 LambdaQueryWrapper，禁止拼接 SQL，批量操作单次≤1000 条
MySQL 规范：
表引擎 InnoDB，字符集 utf8mb4，每个表必须有 bigint 自增主键
高频字段加唯一索引，联合索引遵循最左前缀原则，单表索引≤5 个
禁止 SELECT *，禁止 WHERE 条件用函数导致索引失效
代码质量：类 / 方法必须加注释，方法行数≤80，类行数≤500，核心方法加 @Transactional
三、前端规则
工程结构：按功能模块划分目录，路由懒加载，添加权限守卫
Vue3 规范：
使用 Composition API+setup 语法糖，Props 定义类型 / 默认值 / 校验
样式用 scoped，全局样式统一存放
接口请求封装 axios，统一拦截处理
性能优化：列表渲染加 key（用 id），复杂逻辑抽离为 computed，高频操作防抖 / 节流
四、安全规范
后端安全：入参用 Jakarta Validation 校验，防 SQL 注入 / XSS，密码 BCrypt 加密，敏感信息用配置中心
前端安全：输入内容 XSS 过滤，敏感信息不存 localStorage，接口请求可加签名校验
五、可维护性规范
文档规范：后端用 knife4j 生成接口文档，前端项目根目录写 README.md
代码审查：功能完成后提交 MR，至少 1 人审查通过再合并，重点检查规范、逻辑、安全