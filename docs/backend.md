# 后端文档

## 技术框架
- Spring Boot 3.2 + Spring MVC + Spring Data JPA + Spring Security
- 认证采用基于数据库的用户名/密码，登录后按角色（ADMIN / TEACHER）授权访问
- 文件处理使用 Apache POI 读取/写入 Excel，并通过本地目录 `uploads/` 分区存储模板、教师提交与汇总文件

## 模块划分
| 模块 | 说明 |
| --- | --- |
| config | `SecurityConfig` 负责安全策略，`StorageProperties`/`FileStorageServiceImpl` 管理文件落地路径，`DataInitializer` 写入默认管理员与教师账号 |
| controller | `AuthController`、`PageController`、`AdminController`、`TeacherController`、`FileController` 分别处理登录、首页重定向、管理员流程、教师流程与附件下载 |
| service | `TemplateService` 处理模板创建、提醒、汇总；`SubmissionService` 处理教师上传；`UserService` 提供基础信息查询；`FileStorageService` 封装文件保存 |
| repository | 基于 Spring Data JPA 的实体仓库，覆盖用户、模板、收件人、提交、提醒、汇总 |
| util | `ExcelAggregationUtil` 负责多份 Excel 的首个工作表合并，并返回合并后的行数 |

## 业务流程
1. **管理员发布任务**
   - 上传 Excel 模板，系统保存文件并创建 `TemplateTask`
   - 根据发送范围自动计算 `TemplateRecipient` 列表，教师立即可见任务
2. **教师提交**
   - 教师在面板中上传填写好的 Excel，系统校验权限、保存附件并计算行数，更新 `TemplateRecipient` 状态为 SUBMITTED
3. **提醒未完成**
   - 管理员一键提醒时，系统筛出尚未 SUBMITTED 的教师，更新状态为 REMINDED 并写入 `ReminderLog`
4. **生成汇总**
   - 读取当前任务所有提交附件，使用 Apache POI 合并首个工作表，生成新的汇总文件并记录 `AggregationResult`
5. **附件下载**
   - 所有模板、提交和汇总文件统一通过 `/files?path=...` 下载接口输出，防止直接暴露物理路径

## 配置与运行
- `application.properties` 中已启用内存 H2 数据库与 H2 Console
- 启动命令：`mvn spring-boot:run` 或运行 `org.example.App`
- 默认登录：
  - 管理员：`admin / admin123`
  - 教师：`alice / teacher123` 等（详见 `DataInitializer`）

