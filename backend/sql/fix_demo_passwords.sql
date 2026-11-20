-- ============================================
-- 演示账号密码统一为 123456（仅用于演示/开发库）
-- 背景：init.sql / test_data.sql 早期使用的 BCrypt 密文与文档写的 123456 不匹配，
--       导致 delivery02~05、scheduler02 等账号无法登录。
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 goods_schedule < backend/sql/fix_demo_passwords.sql
-- ============================================

USE `goods_schedule`;

-- 123456 对应的 BCrypt 密文（与 admin / delivery01 / customer01 一致）
SET @pwd = '$2a$10$EKXOFOjn2Bve3t45194KkOdzGzywmeRw3yeekVf.YeURs2Z4.IaHi';

UPDATE `t_user`
SET `password` = @pwd
WHERE `username` IN ('admin', 'scheduler01', 'scheduler02', 'delivery01', 'delivery02', 'delivery03', 'delivery04', 'delivery05', 'customer01', 'customer02');

SELECT `id`, `username`, `realname`, `role`, `status` FROM `t_user` ORDER BY `role`, `id`;
