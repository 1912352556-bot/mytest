-- ============================================
-- 生鲜即时配送调度系统 — 存量库升级脚本
-- 适用：已按旧版 init.sql 建过库（prefab_dish_schedule），需要平滑升级
-- 执行：mysql -uroot -p prefab_dish_schedule < upgrade_to_fresh_goods.sql
-- 建议：执行前先备份，例如 mysqldump -uroot -p prefab_dish_schedule > bak.sql
-- 注意：本脚本一次性执行，重复执行会在 ALTER 处报“列已存在”，可跳过该语句继续
-- ============================================

USE `prefab_dish_schedule`;

-- --------------------------------------------
-- 一、商品表补充生鲜属性（表名/字段名沿用 dish，避免接口与存量数据迁移）
-- --------------------------------------------
ALTER TABLE `t_dish`
    ADD COLUMN `shelf_life_days` INT DEFAULT NULL COMMENT '保质期（天）' AFTER `status`,
    ADD COLUMN `storage_type` TINYINT DEFAULT 1 COMMENT '储存温层：1-常温 2-冷藏 3-冷冻' AFTER `shelf_life_days`,
    ADD COLUMN `origin` VARCHAR(100) DEFAULT NULL COMMENT '产地' AFTER `storage_type`,
    ADD COLUMN `spec` VARCHAR(50) DEFAULT NULL COMMENT '规格，如 500g/份' AFTER `origin`,
    ADD COLUMN `loss_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '损耗率（%）' AFTER `spec`,
    ADD INDEX `idx_storage_type` (`storage_type`);

ALTER TABLE `t_dish` COMMENT = '生鲜商品表';
ALTER TABLE `t_dish_category` COMMENT = '生鲜商品分类表';
ALTER TABLE `t_inventory` MODIFY COLUMN `dish_id` BIGINT NOT NULL COMMENT '商品ID';
ALTER TABLE `t_order_item` MODIFY COLUMN `dish_id` BIGINT NOT NULL COMMENT '商品ID';

-- 老数据兜底：默认常温、7 天保质、无损耗
UPDATE `t_dish` SET `shelf_life_days` = 7 WHERE `shelf_life_days` IS NULL;
UPDATE `t_dish` SET `storage_type` = 1 WHERE `storage_type` IS NULL;
UPDATE `t_dish` SET `loss_rate` = 0 WHERE `loss_rate` IS NULL;

-- --------------------------------------------
-- 二、分类改造：旧分类（荤菜/素菜/汤品）换成生鲜品类
-- --------------------------------------------
UPDATE `t_dish_category` SET `category_name` = '蔬菜', `sort_order` = 1 WHERE `id` = 1;
UPDATE `t_dish_category` SET `category_name` = '水果', `sort_order` = 2 WHERE `id` = 2;
UPDATE `t_dish_category` SET `category_name` = '肉禽蛋', `sort_order` = 3 WHERE `id` = 3;

INSERT INTO `t_dish_category` (`category_name`, `sort_order`, `status`)
SELECT t.`name`, t.`sort_order`, 1
FROM (SELECT '水产海鲜' AS `name`, 4 AS `sort_order`
      UNION ALL SELECT '乳品烘焙', 5
      UNION ALL SELECT '粮油调味', 6) t
         LEFT JOIN `t_dish_category` c ON c.`category_name` = t.`name`
WHERE c.`id` IS NULL;

-- --------------------------------------------
-- 三、旧预制菜商品下架（保留记录，历史订单仍可正常展示商品名）
-- --------------------------------------------
UPDATE `t_dish` SET `status` = 0
WHERE `dish_code` IN ('HSR001', 'TCPG001', 'GBJD001', 'YXRS001', 'QCSS001', 'SRXLH001', 'FQDT001', 'ZCDHT001');

-- --------------------------------------------
-- 四、导入生鲜示例商品（按商品编码去重，可重复执行）
-- --------------------------------------------
INSERT IGNORE INTO `t_dish`
(`dish_name`, `dish_code`, `category_id`, `price`, `unit`, `description`,
 `shelf_life_days`, `storage_type`, `origin`, `spec`, `loss_rate`, `status`)
SELECT t.`dish_name`, t.`dish_code`, t.`category_id`, t.`price`, t.`unit`, t.`description`,
       t.`shelf_life_days`, t.`storage_type`, t.`origin`, t.`spec`, t.`loss_rate`, 1
FROM (SELECT '有机小白菜' AS `dish_name`, 'SCC001' AS `dish_code`, 1 AS `category_id`, 6.80 AS `price`, '份' AS `unit`, '当日采摘，叶片脆嫩，适合清炒或煮汤' AS `description`, 3 AS `shelf_life_days`, 2 AS `storage_type`, '山东寿光' AS `origin`, '500g/份' AS `spec`, 8.00 AS `loss_rate`
      UNION ALL SELECT '沙瓤番茄', 'FQ001', 1, 5.50, '份', '自然成熟，酸甜多汁，凉拌炒菜皆宜', 5, 1, '山东寿光', '500g/份', 10.00
      UNION ALL SELECT '西兰花', 'XLH001', 1, 8.90, '份', '花球紧实，冷链直发，焯水即食', 7, 2, '云南昆明', '400g/份', 6.00
      UNION ALL SELECT '赣南脐橙', 'GNQC001', 2, 19.90, '份', '果园直采，果肉饱满，甜度12°以上', 15, 1, '江西赣州', '2.5kg/箱', 5.00
      UNION ALL SELECT '海南香蕉', 'HNXJ001', 2, 12.80, '份', '七成熟采摘，到家自然转黄', 7, 1, '海南三亚', '1kg/份', 12.00
      UNION ALL SELECT '丹东红颜草莓', 'HYCM001', 2, 39.90, '盒', '单果15g以上，冷链锁鲜，易损耗请轻放', 2, 2, '辽宁丹东', '250g/盒', 15.00
      UNION ALL SELECT '黑猪里脊', 'HZLJ001', 3, 32.00, '份', '当日冷鲜分割，无注水，适合炒制', 3, 2, '四川成都', '500g/份', 3.00
      UNION ALL SELECT '谷饲西冷牛排', 'GSNP001', 3, 89.00, '盒', '原切非拼接，独立真空包装', 180, 3, '内蒙古锡林郭勒', '2片/盒', 2.00
      UNION ALL SELECT '散养土鸡蛋', 'TJD001', 3, 26.00, '盒', '谷物喂养，蛋黄橙红，防震包装', 30, 1, '湖北荆州', '30枚/盒', 1.50
      UNION ALL SELECT '鲜活基围虾', 'JWS001', 4, 68.00, '份', '活虾现捞，冰鲜配送，建议当日食用', 1, 2, '广东湛江', '500g/份', 5.00
      UNION ALL SELECT '冰鲜鲈鱼', 'BXLY001', 4, 42.00, '条', '已去鳞去内脏，清蒸首选', 2, 2, '福建宁德', '600g/条', 4.00
      UNION ALL SELECT '巴氏鲜牛奶', 'BSNN001', 5, 19.90, '瓶', '75℃巴氏杀菌，全程2-6℃冷链', 7, 2, '内蒙古呼和浩特', '950ml/瓶', 2.00
      UNION ALL SELECT '现烤原味吐司', 'XKTS001', 5, 12.90, '袋', '中央厨房当日现烤，无防腐剂', 3, 1, '本地中央厨房', '400g/袋', 3.00
      UNION ALL SELECT '五常稻花香大米', 'WCDM001', 6, 59.90, '袋', '当季新米，真空包装，米香浓郁', 365, 1, '黑龙江五常', '5kg/袋', 0.50) t;

-- --------------------------------------------
-- 五、为缺少库存记录的在售商品补建库存
-- --------------------------------------------
INSERT INTO `t_inventory` (`dish_id`, `current_stock`, `warning_threshold`, `unit`)
SELECT d.`id`, 50, 15, d.`unit`
FROM `t_dish` d
         LEFT JOIN `t_inventory` i ON i.`dish_id` = d.`id`
WHERE i.`id` IS NULL
  AND d.`status` = 1;

-- --------------------------------------------
-- 六、数据字典同步：分类字典 + 新增储存温层字典
-- --------------------------------------------
UPDATE `t_dict_type`
SET `dict_name`   = '生鲜商品分类',
    `description` = '蔬菜/水果/肉禽蛋/水产海鲜/乳品烘焙/粮油调味'
WHERE `dict_code` = 'dish_category';

INSERT INTO `t_dict_type` (`dict_code`, `dict_name`, `description`, `status`)
SELECT 'storage_type', '储存温层', '常温/冷藏/冷冻', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `t_dict_type` WHERE `dict_code` = 'storage_type');

-- 分类字典项
INSERT INTO `t_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`)
SELECT (SELECT `id` FROM `t_dict_type` WHERE `dict_code` = 'dish_category'),
       t.`dict_code`, t.`dict_label`, t.`dict_value`, t.`sort_order`
FROM (SELECT 'dish_category' AS `dict_code`, '蔬菜' AS `dict_label`, '1' AS `dict_value`, 1 AS `sort_order`
      UNION ALL SELECT 'dish_category', '水果', '2', 2
      UNION ALL SELECT 'dish_category', '肉禽蛋', '3', 3
      UNION ALL SELECT 'dish_category', '水产海鲜', '4', 4
      UNION ALL SELECT 'dish_category', '乳品烘焙', '5', 5
      UNION ALL SELECT 'dish_category', '粮油调味', '6', 6) t
         LEFT JOIN `t_dict_data` b
                   ON b.`dict_code` = t.`dict_code` AND b.`dict_value` = t.`dict_value`
WHERE b.`id` IS NULL;

-- 储存温层字典项
INSERT INTO `t_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`)
SELECT (SELECT `id` FROM `t_dict_type` WHERE `dict_code` = 'storage_type'),
       t.`dict_code`, t.`dict_label`, t.`dict_value`, t.`sort_order`
FROM (SELECT 'storage_type' AS `dict_code`, '常温' AS `dict_label`, '1' AS `dict_value`, 1 AS `sort_order`
      UNION ALL SELECT 'storage_type', '冷藏', '2', 2
      UNION ALL SELECT 'storage_type', '冷冻', '3', 3) t
         LEFT JOIN `t_dict_data` b
                   ON b.`dict_code` = t.`dict_code` AND b.`dict_value` = t.`dict_value`
WHERE b.`id` IS NULL;

-- --------------------------------------------
-- 七、校验
-- --------------------------------------------
-- 生鲜商品清单（温层：1-常温 2-冷藏 3-冷冻）
SELECT d.`id`, d.`dish_name`, c.`category_name`, d.`spec`, d.`storage_type`,
       d.`shelf_life_days`, d.`origin`, d.`loss_rate`, i.`current_stock`, i.`warning_threshold`
FROM `t_dish` d
         LEFT JOIN `t_dish_category` c ON c.`id` = d.`category_id`
         LEFT JOIN `t_inventory` i ON i.`dish_id` = d.`id`
WHERE d.`deleted` = 0
ORDER BY c.`sort_order`, d.`id`;
