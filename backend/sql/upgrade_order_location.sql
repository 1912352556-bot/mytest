-- ============================================
-- 订单表补充配送坐标（地图选点）
-- 适用：已执行过 init.sql 的 goods_schedule 库
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 goods_schedule < backend/sql/upgrade_order_location.sql
-- 说明：只需执行一次；重复执行会报「字段已存在」，可忽略
-- ============================================

USE `goods_schedule`;

ALTER TABLE `t_order`
    ADD COLUMN `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '配送地址经度（地图选点）' AFTER `delivery_area`,
    ADD COLUMN `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '配送地址纬度（地图选点）' AFTER `longitude`;

SELECT `id`, `order_no`, `delivery_address`, `longitude`, `latitude`
FROM `t_order`
ORDER BY `id` DESC
LIMIT 5;
