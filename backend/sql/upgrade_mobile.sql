-- ============================================
-- 移动端升级脚本（客户 / 配送员）
-- 适用：已执行过 init.sql 的 goods_schedule 库
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 goods_schedule < backend/sql/upgrade_mobile.sql
-- 说明：脚本可重复执行，不会重复插入账号
-- ============================================

USE `goods_schedule`;

-- 1. 角色说明扩展：新增 3-客户（默认值保持 2-配送员，避免影响原有流程）
ALTER TABLE `t_user`
    MODIFY COLUMN `role` TINYINT NOT NULL DEFAULT 2 COMMENT '角色：0-管理员 1-调度员 2-配送员 3-客户';

-- 2. 客户演示账号（密码均为 123456，BCrypt 密文与初始管理员一致）
INSERT INTO `t_user` (`username`, `password`, `realname`, `phone`, `email`, `gender`, `role`, `status`)
SELECT 'customer01', '$2a$10$EKXOFOjn2Bve3t45194KkOdzGzywmeRw3yeekVf.YeURs2Z4.IaHi', '客户王五', '13800000003', 'customer01@example.com', 1, 3, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `t_user` WHERE `username` = 'customer01');

INSERT INTO `t_user` (`username`, `password`, `realname`, `phone`, `email`, `gender`, `role`, `status`)
SELECT 'customer02', '$2a$10$EKXOFOjn2Bve3t45194KkOdzGzywmeRw3yeekVf.YeURs2Z4.IaHi', '客户赵六', '13800000004', 'customer02@example.com', 2, 3, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `t_user` WHERE `username` = 'customer02');

-- 3. 确保配送员账号 delivery01 有对应的配送员档案（移动端按 user_id 关联）
INSERT INTO `t_delivery_person` (`user_id`, `person_name`, `phone`, `vehicle_no`, `status`, `current_task_count`, `total_delivery_count`)
SELECT u.`id`, u.`realname`, u.`phone`, '京A12345', 0, 0, 0
FROM `t_user` u
WHERE u.`username` = 'delivery01'
  AND NOT EXISTS (SELECT 1 FROM `t_delivery_person` p WHERE p.`user_id` = u.`id`);

-- 4. 检查结果
SELECT `id`, `username`, `realname`, `phone`, `role`, `status` FROM `t_user` WHERE `role` = 3;
SELECT `id`, `user_id`, `person_name`, `phone`, `status` FROM `t_delivery_person`;
