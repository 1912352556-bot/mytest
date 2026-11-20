-- ============================================
-- 生鲜即时配送调度系统 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `goods_schedule` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `goods_schedule`;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `realname` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `role` TINYINT NOT NULL DEFAULT 2 COMMENT '角色：0-管理员 1-调度员 2-配送员',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 数据字典类型表
-- ============================================
DROP TABLE IF EXISTS `t_dict_type`;
CREATE TABLE `t_dict_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典编码',
    `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典类型表';

-- ============================================
-- 3. 数据字典数据表
-- ============================================
DROP TABLE IF EXISTS `t_dict_data`;
CREATE TABLE `t_dict_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type_id` BIGINT NOT NULL COMMENT '字典类型ID',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典类型编码',
    `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典值',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type_id` (`dict_type_id`),
    KEY `idx_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典数据表';

-- ============================================
-- 4. 生鲜商品分类表
-- ============================================
DROP TABLE IF EXISTS `t_dish_category`;
CREATE TABLE `t_dish_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生鲜商品分类表';

-- ============================================
-- 5. 生鲜商品表
-- ============================================
DROP TABLE IF EXISTS `t_dish`;
CREATE TABLE `t_dish` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dish_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `dish_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `unit` VARCHAR(20) DEFAULT '份' COMMENT '单位：份/盒/kg/袋/瓶',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `shelf_life_days` INT DEFAULT NULL COMMENT '保质期（天）',
    `storage_type` TINYINT DEFAULT 1 COMMENT '储存温层：1-常温 2-冷藏 3-冷冻',
    `origin` VARCHAR(100) DEFAULT NULL COMMENT '产地',
    `spec` VARCHAR(50) DEFAULT NULL COMMENT '规格，如 500g/份',
    `loss_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '损耗率（%）',
    `stock` INT DEFAULT 0 COMMENT '当前库存数量（冗余字段，权威值以t_inventory为准）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dish_code` (`dish_code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_storage_type` (`storage_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生鲜商品表';

-- ============================================
-- 6. 库存表
-- ============================================
DROP TABLE IF EXISTS `t_inventory`;
CREATE TABLE `t_inventory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dish_id` BIGINT NOT NULL COMMENT '商品ID',
    `current_stock` INT NOT NULL DEFAULT 0 COMMENT '当前库存',
    `warning_threshold` INT DEFAULT 100 COMMENT '预警阈值',
    `unit` VARCHAR(20) DEFAULT '份' COMMENT '单位',
    `last_in_time` DATETIME DEFAULT NULL COMMENT '最后入库时间',
    `last_out_time` DATETIME DEFAULT NULL COMMENT '最后出库时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- ============================================
-- 7. 库存变更记录表
-- ============================================
DROP TABLE IF EXISTS `t_inventory_record`;
CREATE TABLE `t_inventory_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `inventory_id` BIGINT NOT NULL COMMENT '库存ID',
    `change_type` TINYINT NOT NULL COMMENT '变更类型：1-入库 2-出库',
    `change_quantity` INT NOT NULL COMMENT '变更数量',
    `before_stock` INT NOT NULL COMMENT '变更前库存',
    `after_stock` INT NOT NULL COMMENT '变更后库存',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_inventory_id` (`inventory_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变更记录表';

-- ============================================
-- 8. 订单表
-- ============================================
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `customer_name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
    `customer_phone` VARCHAR(20) NOT NULL COMMENT '客户电话',
    `delivery_address` VARCHAR(255) NOT NULL COMMENT '配送地址',
    `delivery_area` VARCHAR(50) NOT NULL COMMENT '配送区域',
    `expected_delivery_time` DATETIME NOT NULL COMMENT '期望配送时间',
    `actual_delivery_time` DATETIME DEFAULT NULL COMMENT '实际配送时间',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `order_status` TINYINT DEFAULT 0 COMMENT '订单状态：0-待调度 1-已调度 2-配送中 3-已完成 4-已取消',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_delivery_area` (`delivery_area`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 9. 订单明细表
-- ============================================
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `dish_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL COMMENT '数量',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ============================================
-- 10. 调度任务表
-- ============================================
DROP TABLE IF EXISTS `t_schedule_task`;
CREATE TABLE `t_schedule_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_no` VARCHAR(50) NOT NULL COMMENT '任务编号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `delivery_area` VARCHAR(50) NOT NULL COMMENT '配送区域',
    `expected_delivery_time` DATETIME NOT NULL COMMENT '期望配送时间',
    `assignee_id` BIGINT DEFAULT NULL COMMENT '指派人ID（配送员）',
    `task_status` TINYINT DEFAULT 0 COMMENT '任务状态：0-待分配 1-已分配 2-执行中 3-已完成',
    `priority` TINYINT DEFAULT 1 COMMENT '优先级：0-低 1-中 2-高 3-紧急',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_no` (`task_no`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_task_status` (`task_status`),
    KEY `idx_assignee_id` (`assignee_id`),
    KEY `idx_delivery_area` (`delivery_area`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调度任务表';

-- ============================================
-- 11. 调度日志表
-- ============================================
DROP TABLE IF EXISTS `t_schedule_log`;
CREATE TABLE `t_schedule_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT NOT NULL COMMENT '调度任务ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `operation_content` VARCHAR(500) NOT NULL COMMENT '操作内容',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调度日志表';

-- ============================================
-- 12. 配送人员表
-- ============================================
DROP TABLE IF EXISTS `t_delivery_person`;
CREATE TABLE `t_delivery_person` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联用户ID',
    `person_name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `vehicle_no` VARCHAR(20) DEFAULT NULL COMMENT '车牌号',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-空闲 1-配送中 2-休息',
    `current_task_count` INT DEFAULT 0 COMMENT '当前任务数',
    `total_delivery_count` INT DEFAULT 0 COMMENT '总配送次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配送人员表';

-- ============================================
-- 13. 配送任务表
-- ============================================
DROP TABLE IF EXISTS `t_delivery_task`;
CREATE TABLE `t_delivery_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `delivery_no` VARCHAR(50) NOT NULL COMMENT '配送单号',
    `schedule_task_id` BIGINT NOT NULL COMMENT '调度任务ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `delivery_person_id` BIGINT NOT NULL COMMENT '配送人员ID',
    `delivery_address` VARCHAR(255) NOT NULL COMMENT '配送地址',
    `delivery_status` TINYINT DEFAULT 0 COMMENT '配送状态：0-待取货 1-配送中 2-已送达 3-异常',
    `pickup_time` DATETIME DEFAULT NULL COMMENT '取货时间',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '送达时间',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_delivery_no` (`delivery_no`),
    KEY `idx_schedule_task_id` (`schedule_task_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_delivery_person_id` (`delivery_person_id`),
    KEY `idx_delivery_status` (`delivery_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配送任务表';

-- ============================================
-- 14. 配送轨迹表
-- ============================================
DROP TABLE IF EXISTS `t_delivery_track`;
CREATE TABLE `t_delivery_track` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `delivery_task_id` BIGINT NOT NULL COMMENT '配送任务ID',
    `longitude` DECIMAL(10,6) NOT NULL COMMENT '经度',
    `latitude` DECIMAL(10,6) NOT NULL COMMENT '纬度',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `record_time` DATETIME NOT NULL COMMENT '记录时间',
    PRIMARY KEY (`id`),
    KEY `idx_delivery_task_id` (`delivery_task_id`),
    KEY `idx_record_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配送轨迹表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化管理员账号（密码：123456，BCrypt加密）
INSERT INTO `t_user` (`username`, `password`, `realname`, `phone`, `email`, `gender`, `role`, `status`) VALUES
('admin', '$2a$10$EKXOFOjn2Bve3t45194KkOdzGzywmeRw3yeekVf.YeURs2Z4.IaHi', '系统管理员', '13800000000', 'admin@example.com', 1, 0, 1),
('scheduler01', '$2a$10$EKXOFOjn2Bve3t45194KkOdzGzywmeRw3yeekVf.YeURs2Z4.IaHi', '调度员张三', '13800000001', 'zhangsan@example.com', 1, 1, 1),
('delivery01', '$2a$10$EKXOFOjn2Bve3t45194KkOdzGzywmeRw3yeekVf.YeURs2Z4.IaHi', '配送员李四', '13800000002', 'lisi@example.com', 1, 2, 1);

-- 初始化数据字典类型
INSERT INTO `t_dict_type` (`dict_code`, `dict_name`, `description`) VALUES
('order_status', '订单状态', '订单状态字典'),
('schedule_status', '调度状态', '调度任务状态'),
('delivery_status', '配送状态', '配送任务状态'),
('inventory_type', '库存类型', '库存变动类型'),
('dish_category', '生鲜商品分类', '蔬菜/水果/肉禽蛋/水产海鲜/乳品烘焙/粮油调味'),
('priority_level', '优先级', '任务优先级'),
('storage_type', '储存温层', '常温/冷藏/冷冻');

-- 初始化数据字典数据
INSERT INTO `t_dict_data` (`dict_type_id`, `dict_code`, `dict_label`, `dict_value`, `sort_order`) VALUES
(1, 'order_status', '待调度', '0', 1),
(1, 'order_status', '已调度', '1', 2),
(1, 'order_status', '配送中', '2', 3),
(1, 'order_status', '已完成', '3', 4),
(1, 'order_status', '已取消', '4', 5),
(2, 'schedule_status', '待分配', '0', 1),
(2, 'schedule_status', '已分配', '1', 2),
(2, 'schedule_status', '执行中', '2', 3),
(2, 'schedule_status', '已完成', '3', 4),
(3, 'delivery_status', '待取货', '0', 1),
(3, 'delivery_status', '配送中', '1', 2),
(3, 'delivery_status', '已送达', '2', 3),
(3, 'delivery_status', '异常', '3', 4),
(4, 'inventory_type', '入库', '1', 1),
(4, 'inventory_type', '出库', '2', 2),
(6, 'priority_level', '低', '0', 1),
(6, 'priority_level', '中', '1', 2),
(6, 'priority_level', '高', '2', 3),
(6, 'priority_level', '紧急', '3', 4),
(5, 'dish_category', '蔬菜', '1', 1),
(5, 'dish_category', '水果', '2', 2),
(5, 'dish_category', '肉禽蛋', '3', 3),
(5, 'dish_category', '水产海鲜', '4', 4),
(5, 'dish_category', '乳品烘焙', '5', 5),
(5, 'dish_category', '粮油调味', '6', 6),
(7, 'storage_type', '常温', '1', 1),
(7, 'storage_type', '冷藏', '2', 2),
(7, 'storage_type', '冷冻', '3', 3);

-- 初始化生鲜商品分类
INSERT INTO `t_dish_category` (`category_name`, `sort_order`) VALUES
('蔬菜', 1),
('水果', 2),
('肉禽蛋', 3),
('水产海鲜', 4),
('乳品烘焙', 5),
('粮油调味', 6);

-- 初始化生鲜商品（含保质期、储存温层、产地、规格、损耗率等生鲜属性）
INSERT INTO `t_dish` (`dish_name`, `dish_code`, `category_id`, `price`, `unit`, `description`, `shelf_life_days`, `storage_type`, `origin`, `spec`, `loss_rate`) VALUES
('有机小白菜', 'SCC001', 1, 6.80, '份', '当日采摘，叶片脆嫩，适合清炒或煮汤', 3, 2, '山东寿光', '500g/份', 8.00),
('沙瓤番茄', 'FQ001', 1, 5.50, '份', '自然成熟，酸甜多汁，凉拌炒菜皆宜', 5, 1, '山东寿光', '500g/份', 10.00),
('西兰花', 'XLH001', 1, 8.90, '份', '花球紧实，冷链直发，焯水即食', 7, 2, '云南昆明', '400g/份', 6.00),
('赣南脐橙', 'GNQC001', 2, 19.90, '份', '果园直采，果肉饱满，甜度12°以上', 15, 1, '江西赣州', '2.5kg/箱', 5.00),
('海南香蕉', 'HNXJ001', 2, 12.80, '份', '七成熟采摘，到家自然转黄', 7, 1, '海南三亚', '1kg/份', 12.00),
('丹东红颜草莓', 'HYCM001', 2, 39.90, '盒', '单果15g以上，冷链锁鲜，易损耗请轻放', 2, 2, '辽宁丹东', '250g/盒', 15.00),
('黑猪里脊', 'HZLJ001', 3, 32.00, '份', '当日冷鲜分割，无注水，适合炒制', 3, 2, '四川成都', '500g/份', 3.00),
('谷饲西冷牛排', 'GSNP001', 3, 89.00, '盒', '原切非拼接，独立真空包装', 180, 3, '内蒙古锡林郭勒', '2片/盒', 2.00),
('散养土鸡蛋', 'TJD001', 3, 26.00, '盒', '谷物喂养，蛋黄橙红，防震包装', 30, 1, '湖北荆州', '30枚/盒', 1.50),
('鲜活基围虾', 'JWS001', 4, 68.00, '份', '活虾现捞，冰鲜配送，建议当日食用', 1, 2, '广东湛江', '500g/份', 5.00),
('冰鲜鲈鱼', 'BXLY001', 4, 42.00, '条', '已去鳞去内脏，清蒸首选', 2, 2, '福建宁德', '600g/条', 4.00),
('巴氏鲜牛奶', 'BSNN001', 5, 19.90, '瓶', '75℃巴氏杀菌，全程2-6℃冷链', 7, 2, '内蒙古呼和浩特', '950ml/瓶', 2.00),
('现烤原味吐司', 'XKTS001', 5, 12.90, '袋', '中央厨房当日现烤，无防腐剂', 3, 1, '本地中央厨房', '400g/袋', 3.00),
('五常稻花香大米', 'WCDM001', 6, 59.90, '袋', '当季新米，真空包装，米香浓郁', 365, 1, '黑龙江五常', '5kg/袋', 0.50);

-- 初始化库存（预警阈值按生鲜日销节奏设置）
INSERT INTO `t_inventory` (`dish_id`, `current_stock`, `warning_threshold`, `unit`) VALUES
(1, 120, 30, '份'),
(2, 150, 40, '份'),
(3, 90, 25, '份'),
(4, 60, 20, '份'),
(5, 80, 25, '份'),
(6, 40, 15, '盒'),
(7, 70, 20, '份'),
(8, 35, 10, '盒'),
(9, 50, 15, '盒'),
(10, 25, 10, '份'),
(11, 30, 10, '条'),
(12, 60, 20, '瓶'),
(13, 45, 15, '袋'),
(14, 200, 50, '袋');

-- 初始化配送人员
INSERT INTO `t_delivery_person` (`user_id`, `person_name`, `phone`, `vehicle_no`, `status`) VALUES
(3, '配送员李四', '13800000002', '京A12345', 0);

-- 订单、调度、配送、库存流水等演示数据见同目录 test_data.sql
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 goods_schedule < backend/sql/test_data.sql
