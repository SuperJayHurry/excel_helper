# 数据库文档

## 使用说明
- 采用 MYSQL 内存库，启动时由 JPA 自动建表，可替换为 MySQL/PG 仅需修改 `application.properties`
- 所有实体均继承 `BaseEntity`，默认包含 `id`, `created_at`, `updated_at`
- `Department`、`UserRole`、`RecipientStatus` 以字符串形式持久化，便于阅读

## 实体与建表 SQL

### 1. 用户 `users`
字段：用户名、加密密码、姓名、邮箱、所属系、角色、是否启用  
示例建表：
```
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(120) NOT NULL,
  department VARCHAR(64) NOT NULL,
  role VARCHAR(32) NOT NULL,
  active BOOLEAN NOT NULL
);

初始化在src/main/java/org/example/config/DataInitializer.java
```

### 2. 汇总任务 `template_tasks`
字段：任务名称、描述、截止日期、模板文件名/路径、创建人  
```
CREATE TABLE template_tasks (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(400),
  deadline DATE,
  template_file_name VARCHAR(255) NOT NULL,
  template_file_path VARCHAR(255) NOT NULL,
  created_by BIGINT,
  CONSTRAINT fk_task_creator FOREIGN KEY (created_by) REFERENCES users(id)
);
```

### 3. 任务收件人 `template_recipients`
字段：任务、教师、状态（PENDING/REMINDED/SUBMITTED）、最后提醒时间、提醒次数  
```
CREATE TABLE template_recipients (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  template_task_id BIGINT NOT NULL,
  recipient_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  last_reminder_at TIMESTAMP,
  reminder_count INT NOT NULL,
  CONSTRAINT fk_recipient_task FOREIGN KEY (template_task_id) REFERENCES template_tasks(id),
  CONSTRAINT fk_recipient_user FOREIGN KEY (recipient_id) REFERENCES users(id)
);
```

### 4. 教师提交 `submissions`
字段：任务、提交人、文件名/路径、提交时间、Excel 行数  
```
CREATE TABLE submissions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  template_task_id BIGINT NOT NULL,
  submitter_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  submitted_at TIMESTAMP,
  total_rows INT,
  CONSTRAINT fk_submission_task FOREIGN KEY (template_task_id) REFERENCES template_tasks(id),
  CONSTRAINT fk_submission_user FOREIGN KEY (submitter_id) REFERENCES users(id)
);
```

### 5. 汇总结果 `aggregation_results`
字段：任务、文件名/路径、总行数、生成时间  
```
CREATE TABLE aggregation_results (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  template_task_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  total_rows INT,
  generated_at TIMESTAMP,
  CONSTRAINT fk_aggregation_task FOREIGN KEY (template_task_id) REFERENCES template_tasks(id)
);
```

### 6. 提醒日志 `reminder_logs`
字段：任务、教师、提醒内容  
```
CREATE TABLE reminder_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  template_task_id BIGINT NOT NULL,
  recipient_id BIGINT NOT NULL,
  message VARCHAR(300) NOT NULL,
  CONSTRAINT fk_reminder_task FOREIGN KEY (template_task_id) REFERENCES template_tasks(id),
  CONSTRAINT fk_reminder_user FOREIGN KEY (recipient_id) REFERENCES users(id)
);
```

