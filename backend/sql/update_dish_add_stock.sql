-- 历史补丁：给商品表（t_dish）添加库存冗余字段
-- 注：新版 init.sql 已包含 stock 字段，本脚本仅在升级早期版本数据库时使用
ALTER TABLE t_dish ADD COLUMN `stock` INT DEFAULT 0 COMMENT '当前库存数量' AFTER `status`;

-- 更新现有商品的初始库存
UPDATE t_dish SET stock = 100 WHERE id IN (1, 2, 3, 4);
UPDATE t_dish SET stock = 200 WHERE id IN (5, 6);
UPDATE t_dish SET stock = 300 WHERE id IN (7, 8);
