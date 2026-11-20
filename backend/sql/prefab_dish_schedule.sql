/*
 Navicat Premium Dump SQL

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80402 (8.4.2)
 Source Host           : localhost:3306
 Source Schema         : prefab_dish_schedule

 Target Server Type    : MySQL
 Target Server Version : 80402 (8.4.2)
 File Encoding         : 65001

 Date: 09/05/2026 22:56:11

 说明（生鲜版）：
   本文件为「预制菜调度管理系统」时期的历史数据备份，仅作留档，表结构与数据保持原样。
   新建库请执行 init.sql（已按生鲜商品模型重建分类与示例数据）；
   存量库升级为生鲜商品模型请执行 upgrade_to_fresh_goods.sql（补充生鲜属性字段并迁移分类/字典）。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_delivery_person
-- ----------------------------
DROP TABLE IF EXISTS `t_delivery_person`;
CREATE TABLE `t_delivery_person`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户ID',
  `person_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `vehicle_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '车牌号',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-空闲 1-配送中 2-休息',
  `current_task_count` int NULL DEFAULT 0 COMMENT '当前任务数',
  `total_delivery_count` int NULL DEFAULT 0 COMMENT '总配送次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '配送人员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_delivery_person
-- ----------------------------
INSERT INTO `t_delivery_person` VALUES (1, 3, '配送员李四', '13800000002', '京A12345', 0, 0, 2, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_delivery_person` VALUES (2, NULL, '张三', '18585858585', '', 0, 0, 1, '2026-02-16 11:28:41', '2026-02-16 11:28:41', 0);
INSERT INTO `t_delivery_person` VALUES (3, NULL, '王逢源', '18561786159', '鲁B88888', 0, 0, 0, '2026-03-10 16:23:57', '2026-03-10 16:24:02', 1);

-- ----------------------------
-- Table structure for t_delivery_task
-- ----------------------------
DROP TABLE IF EXISTS `t_delivery_task`;
CREATE TABLE `t_delivery_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `delivery_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配送单号',
  `schedule_task_id` bigint NOT NULL COMMENT '调度任务ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `delivery_person_id` bigint NOT NULL COMMENT '配送人员ID',
  `delivery_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配送地址',
  `delivery_status` tinyint NULL DEFAULT 0 COMMENT '配送状态：0-待取货 1-配送中 2-已送达 3-异常',
  `pickup_time` datetime NULL DEFAULT NULL COMMENT '取货时间',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '送达时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_delivery_no`(`delivery_no` ASC) USING BTREE,
  INDEX `idx_schedule_task_id`(`schedule_task_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_delivery_person_id`(`delivery_person_id` ASC) USING BTREE,
  INDEX `idx_delivery_status`(`delivery_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '配送任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_delivery_task
-- ----------------------------
INSERT INTO `t_delivery_task` VALUES (1, 'DEL1771212976659', 1, 2, 1, '123456798', 2, '2026-02-16 11:36:35', '2026-02-16 11:36:51', NULL, '2026-02-16 11:36:17', '2026-02-16 11:36:17', 0);
INSERT INTO `t_delivery_task` VALUES (2, 'DEL1773130981323', 2, 3, 1, '潍坊诸城恐龙博物馆', 2, '2026-03-10 16:23:18', '2026-03-10 16:23:25', NULL, '2026-03-10 16:23:01', '2026-03-10 16:23:01', 0);
INSERT INTO `t_delivery_task` VALUES (3, 'DEL1773881777381', 3, 4, 2, 'etstetst', 2, '2026-03-19 08:56:31', '2026-03-19 08:57:12', NULL, '2026-03-19 08:56:17', '2026-03-19 08:56:17', 0);

-- ----------------------------
-- Table structure for t_delivery_track
-- ----------------------------
DROP TABLE IF EXISTS `t_delivery_track`;
CREATE TABLE `t_delivery_track`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `delivery_task_id` bigint NOT NULL COMMENT '配送任务ID',
  `longitude` decimal(10, 6) NOT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NOT NULL COMMENT '纬度',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_delivery_task_id`(`delivery_task_id` ASC) USING BTREE,
  INDEX `idx_record_time`(`record_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '配送轨迹表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_delivery_track
-- ----------------------------

-- ----------------------------
-- Table structure for t_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `t_dict_data`;
CREATE TABLE `t_dict_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type_id` bigint NOT NULL COMMENT '字典类型ID',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型编码',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典值',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_type_id`(`dict_type_id` ASC) USING BTREE,
  INDEX `idx_dict_code`(`dict_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_dict_data
-- ----------------------------
INSERT INTO `t_dict_data` VALUES (1, 1, 'order_status', '待调度', '0', 1, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (2, 1, 'order_status', '已调度', '1', 2, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (3, 1, 'order_status', '配送中', '2', 3, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (4, 1, 'order_status', '已完成', '3', 4, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (5, 1, 'order_status', '已取消', '4', 5, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (6, 2, 'schedule_status', '待分配', '0', 1, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (7, 2, 'schedule_status', '已分配', '1', 2, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (8, 2, 'schedule_status', '执行中', '2', 3, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (9, 2, 'schedule_status', '已完成', '3', 4, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (10, 3, 'delivery_status', '待取货', '0', 1, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (11, 3, 'delivery_status', '配送中', '1', 2, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (12, 3, 'delivery_status', '已送达', '2', 3, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (13, 3, 'delivery_status', '异常', '3', 4, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (14, 4, 'inventory_type', '入库', '1', 1, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (15, 4, 'inventory_type', '出库', '2', 2, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (16, 6, 'priority_level', '低', '0', 1, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (17, 6, 'priority_level', '中', '1', 2, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (18, 6, 'priority_level', '高', '2', 3, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_data` VALUES (19, 6, 'priority_level', '紧急', '3', 4, 1, NULL, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);

-- ----------------------------
-- Table structure for t_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `t_dict_type`;
CREATE TABLE `t_dict_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_code`(`dict_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_dict_type
-- ----------------------------
INSERT INTO `t_dict_type` VALUES (1, 'order_status', '订单状态', '订单状态字典', 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_type` VALUES (2, 'schedule_status', '调度状态', '调度任务状态', 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_type` VALUES (3, 'delivery_status', '配送状态', '配送任务状态', 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_type` VALUES (4, 'inventory_type', '库存类型', '库存变动类型', 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_type` VALUES (5, 'dish_category', '菜品分类', '预制菜分类', 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dict_type` VALUES (6, 'priority_level', '优先级', '任务优先级', 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);

-- ----------------------------
-- Table structure for t_dish
-- ----------------------------
DROP TABLE IF EXISTS `t_dish`;
CREATE TABLE `t_dish`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称',
  `dish_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品编码',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '份' COMMENT '单位',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片URL',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
  `stock` int NULL DEFAULT 0 COMMENT '当前库存数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dish_code`(`dish_code` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_dish
-- ----------------------------
INSERT INTO `t_dish` VALUES (1, '红烧肉', 'HSR001', 1, 28.00, '份', '精选五花肉，传统配方，入口即化', NULL, 1, 100, '2026-02-16 11:05:54', '2026-02-16 11:17:38', 0);
INSERT INTO `t_dish` VALUES (2, '糖醋排骨', 'TCPG001', 1, 32.00, '份', '酸甜可口，外酥里嫩', NULL, 1, 100, '2026-02-16 11:05:54', '2026-02-16 11:17:38', 0);
INSERT INTO `t_dish` VALUES (3, '宫保鸡丁', 'GBJD001', 1, 26.00, '份', '经典川菜，香辣开胃', NULL, 1, 100, '2026-02-16 11:05:54', '2026-02-16 11:17:38', 0);
INSERT INTO `t_dish` VALUES (4, '鱼香肉丝', 'YXRS001', 1, 24.00, '份', '酸甜咸香，口味丰富', NULL, 1, 100, '2026-02-16 11:05:54', '2026-02-16 11:17:38', 0);
INSERT INTO `t_dish` VALUES (5, '清炒时蔬', 'QCSS001', 2, 12.00, '份', '新鲜时令蔬菜，清淡健康', NULL, 1, 200, '2026-02-16 11:05:54', '2026-02-16 11:17:38', 0);
INSERT INTO `t_dish` VALUES (6, '蒜蓉西兰花', 'SRXLH001', 2, 15.00, '份', '营养丰富，口感清脆', NULL, 1, 200, '2026-02-16 11:05:54', '2026-02-16 11:17:38', 0);
INSERT INTO `t_dish` VALUES (7, '番茄蛋汤', 'FQDT001', 3, 8.00, '份', '酸甜开胃，营养美味', NULL, 1, 300, '2026-02-16 11:05:54', '2026-02-16 11:17:38', 0);
INSERT INTO `t_dish` VALUES (8, '紫菜蛋花汤', 'ZCDHT001', 3, 6.00, '份', '鲜香可口，老少皆宜', NULL, 1, 300, '2026-02-16 11:05:54', '2026-02-16 11:17:38', 0);
INSERT INTO `t_dish` VALUES (9, '红烧坤', 'DISH1773131178748', 1, 2.50, '份', '好吃', '', 1, 0, '2026-03-10 16:26:19', '2026-03-19 09:36:46', 1);
INSERT INTO `t_dish` VALUES (10, 'etst', 'DISH1773884457698', 1, 0.30, '份', '', '/api/file/image/d643447f-3933-461e-a994-9e4cd80ae2bb.jpg', 1, 0, '2026-03-19 09:40:58', '2026-03-19 09:41:04', 1);

-- ----------------------------
-- Table structure for t_dish_category
-- ----------------------------
DROP TABLE IF EXISTS `t_dish_category`;
CREATE TABLE `t_dish_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_dish_category
-- ----------------------------
INSERT INTO `t_dish_category` VALUES (1, '荤菜', 1, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dish_category` VALUES (2, '素菜', 2, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_dish_category` VALUES (3, '汤品', 3, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);

-- ----------------------------
-- Table structure for t_inventory
-- ----------------------------
DROP TABLE IF EXISTS `t_inventory`;
CREATE TABLE `t_inventory`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `current_stock` int NOT NULL DEFAULT 0 COMMENT '当前库存',
  `warning_threshold` int NULL DEFAULT 100 COMMENT '预警阈值',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '份' COMMENT '单位',
  `last_in_time` datetime NULL DEFAULT NULL COMMENT '最后入库时间',
  `last_out_time` datetime NULL DEFAULT NULL COMMENT '最后出库时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dish_id`(`dish_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_inventory
-- ----------------------------
INSERT INTO `t_inventory` VALUES (1, 1, 501, 500, '份', '2026-03-19 08:56:49', '2026-03-10 16:24:23', 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_inventory` VALUES (2, 2, 344, 100, '份', NULL, '2026-03-19 08:57:41', 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_inventory` VALUES (3, 3, 106, 100, '份', '2026-02-16 11:27:12', '2026-03-19 08:55:40', 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_inventory` VALUES (4, 4, 105, 100, '份', '2026-02-16 11:27:11', NULL, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_inventory` VALUES (5, 5, 800, 100, '份', NULL, NULL, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_inventory` VALUES (6, 6, 600, 100, '份', NULL, NULL, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_inventory` VALUES (7, 7, 1000, 100, '份', NULL, NULL, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_inventory` VALUES (8, 8, 1200, 100, '份', NULL, NULL, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_inventory` VALUES (9, 9, 10, 100, '份', '2026-03-12 17:24:08', '2026-03-19 08:56:54', 1, '2026-03-10 16:26:19', '2026-03-10 16:26:19', 0);
INSERT INTO `t_inventory` VALUES (10, 10, 5, 100, '份', NULL, NULL, 1, '2026-03-19 09:40:58', '2026-03-19 09:40:58', 0);

-- ----------------------------
-- Table structure for t_inventory_record
-- ----------------------------
DROP TABLE IF EXISTS `t_inventory_record`;
CREATE TABLE `t_inventory_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inventory_id` bigint NOT NULL COMMENT '库存ID',
  `change_type` tinyint NOT NULL COMMENT '变更类型：1-入库 2-出库',
  `change_quantity` int NOT NULL COMMENT '变更数量',
  `before_stock` int NOT NULL COMMENT '变更前库存',
  `after_stock` int NOT NULL COMMENT '变更后库存',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inventory_id`(`inventory_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存变更记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_inventory_record
-- ----------------------------
INSERT INTO `t_inventory_record` VALUES (1, 3, 0, 1, 80, 81, NULL, '', 1, '2026-02-16 11:26:35');
INSERT INTO `t_inventory_record` VALUES (2, 3, 0, 66, 81, 147, NULL, '', 1, '2026-02-16 11:26:50');
INSERT INTO `t_inventory_record` VALUES (3, 3, 1, 100, 147, 47, NULL, '', 1, '2026-02-16 11:26:58');
INSERT INTO `t_inventory_record` VALUES (4, 3, 0, 50, 47, 97, NULL, '库存预警快速补货', 1, '2026-02-16 11:27:09');
INSERT INTO `t_inventory_record` VALUES (5, 4, 0, 50, 45, 95, NULL, '库存预警快速补货', 1, '2026-02-16 11:27:10');
INSERT INTO `t_inventory_record` VALUES (6, 4, 0, 10, 95, 105, NULL, '库存预警快速补货', 1, '2026-02-16 11:27:11');
INSERT INTO `t_inventory_record` VALUES (7, 3, 0, 10, 97, 107, NULL, '库存预警快速补货', 1, '2026-02-16 11:27:12');
INSERT INTO `t_inventory_record` VALUES (8, 1, 2, 5, 500, 495, 1, '订单号：ORD1771212615643C0BBBF 生成时扣减库存', 1, '2026-02-16 11:30:16');
INSERT INTO `t_inventory_record` VALUES (9, 1, 0, 5, 495, 500, 1, '订单号：ORD1771212615643C0BBBF 取消时补回库存', 1, '2026-02-16 11:30:46');
INSERT INTO `t_inventory_record` VALUES (10, 1, 2, 1, 500, 499, 2, '订单号：ORD1771212703071C39B81 生成时扣减库存', 1, '2026-02-16 11:31:43');
INSERT INTO `t_inventory_record` VALUES (11, 1, 2, 2, 499, 497, 3, '订单号：ORD1773130918302D7C21B 生成时扣减库存', 1, '2026-03-10 16:21:59');
INSERT INTO `t_inventory_record` VALUES (12, 2, 2, 5, 350, 345, 3, '订单号：ORD1773130918302D7C21B 生成时扣减库存', 1, '2026-03-10 16:21:59');
INSERT INTO `t_inventory_record` VALUES (13, 1, 0, 3, 497, 500, NULL, '', 1, '2026-03-10 16:24:15');
INSERT INTO `t_inventory_record` VALUES (14, 1, 1, 100, 500, 400, NULL, '', 1, '2026-03-10 16:24:23');
INSERT INTO `t_inventory_record` VALUES (15, 1, 0, 100, 400, 500, NULL, '库存预警快速补货', 1, '2026-03-10 16:24:50');
INSERT INTO `t_inventory_record` VALUES (16, 9, 0, 100, 10, 110, NULL, '库存预警快速补货', 1, '2026-03-12 17:24:08');
INSERT INTO `t_inventory_record` VALUES (17, 3, 2, 1, 107, 106, 4, '订单号：ORD17738817402442B62A7 生成时扣减库存', 1, '2026-03-19 08:55:40');
INSERT INTO `t_inventory_record` VALUES (18, 1, 0, 1, 500, 501, NULL, '', 1, '2026-03-19 08:56:49');
INSERT INTO `t_inventory_record` VALUES (19, 9, 1, 100, 110, 10, NULL, '', 1, '2026-03-19 08:56:54');
INSERT INTO `t_inventory_record` VALUES (20, 2, 2, 1, 345, 344, 5, '订单号：ORD1773881861374D7BAB6 生成时扣减库存', 1, '2026-03-19 08:57:41');

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `customer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户姓名',
  `customer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户电话',
  `delivery_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配送地址',
  `delivery_area` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配送区域',
  `expected_delivery_time` datetime NOT NULL COMMENT '期望配送时间',
  `actual_delivery_time` datetime NULL DEFAULT NULL COMMENT '实际配送时间',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `order_status` tinyint NULL DEFAULT 0 COMMENT '订单状态：0-待调度 1-已调度 2-配送中 3-已完成 4-已取消',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_status` ASC) USING BTREE,
  INDEX `idx_delivery_area`(`delivery_area` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_order
-- ----------------------------
INSERT INTO `t_order` VALUES (1, 'ORD1771212615643C0BBBF', '客户1', '13845454545', 'test132', '海淀区', '2026-02-16 11:29:36', NULL, 140.00, 4, '用户取消', 1, '2026-02-16 11:30:16', '2026-02-16 11:31:09', 1);
INSERT INTO `t_order` VALUES (2, 'ORD1771212703071C39B81', '客户1', '18345454545', '123456798', '朝阳区', '2026-02-16 11:31:36', '2026-02-16 11:36:51', 28.00, 3, '', 1, '2026-02-16 11:31:43', '2026-02-16 11:31:43', 0);
INSERT INTO `t_order` VALUES (3, 'ORD1773130918302D7C21B', '王逢源', '18561786159', '潍坊诸城恐龙博物馆', '朝阳区', '2026-03-11 07:01:00', '2026-03-10 16:23:25', 216.00, 3, '我爱吃红烧坤', 1, '2026-03-10 16:21:58', '2026-03-10 16:21:58', 0);
INSERT INTO `t_order` VALUES (4, 'ORD17738817402442B62A7', 'test', '18661786159', 'etstetst', '海淀区', '2026-03-19 13:06:00', '2026-03-19 08:57:12', 26.00, 3, '', 1, '2026-03-19 08:55:40', '2026-03-19 08:55:40', 0);
INSERT INTO `t_order` VALUES (5, 'ORD1773881861374D7BAB6', 'test', '18561786159', 'tttttt', '海淀区', '2026-03-20 00:00:00', NULL, 32.00, 0, '', 1, '2026-03-19 08:57:41', '2026-03-19 08:57:41', 0);

-- ----------------------------
-- Table structure for t_order_item
-- ----------------------------
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `unit_price` decimal(10, 2) NOT NULL COMMENT '单价',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_dish_id`(`dish_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_order_item
-- ----------------------------
INSERT INTO `t_order_item` VALUES (1, 1, 1, 5, 28.00, 140.00, '2026-02-16 11:30:16');
INSERT INTO `t_order_item` VALUES (2, 2, 1, 1, 28.00, 28.00, '2026-02-16 11:31:43');
INSERT INTO `t_order_item` VALUES (3, 3, 1, 2, 28.00, 56.00, '2026-03-10 16:21:59');
INSERT INTO `t_order_item` VALUES (4, 3, 2, 5, 32.00, 160.00, '2026-03-10 16:21:59');
INSERT INTO `t_order_item` VALUES (5, 4, 3, 1, 26.00, 26.00, '2026-03-19 08:55:40');
INSERT INTO `t_order_item` VALUES (6, 5, 2, 1, 32.00, 32.00, '2026-03-19 08:57:41');

-- ----------------------------
-- Table structure for t_schedule_log
-- ----------------------------
DROP TABLE IF EXISTS `t_schedule_log`;
CREATE TABLE `t_schedule_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '调度任务ID',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `operation_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作内容',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '调度日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_schedule_log
-- ----------------------------

-- ----------------------------
-- Table structure for t_schedule_task
-- ----------------------------
DROP TABLE IF EXISTS `t_schedule_task`;
CREATE TABLE `t_schedule_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务编号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `delivery_area` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配送区域',
  `expected_delivery_time` datetime NOT NULL COMMENT '期望配送时间',
  `assignee_id` bigint NULL DEFAULT NULL COMMENT '指派人ID（配送员）',
  `task_status` tinyint NULL DEFAULT 0 COMMENT '任务状态：0-待分配 1-已分配 2-执行中 3-已完成',
  `priority` tinyint NULL DEFAULT 1 COMMENT '优先级：0-低 1-中 2-高 3-紧急',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_no`(`task_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_task_status`(`task_status` ASC) USING BTREE,
  INDEX `idx_assignee_id`(`assignee_id` ASC) USING BTREE,
  INDEX `idx_delivery_area`(`delivery_area` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '调度任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_schedule_task
-- ----------------------------
INSERT INTO `t_schedule_task` VALUES (1, 'SCH1771212735935', 2, '朝阳区', '2026-02-16 11:31:36', 1, 3, 1, '', '2026-02-16 11:32:16', '2026-02-16 11:32:16', 0);
INSERT INTO `t_schedule_task` VALUES (2, 'SCH1773130948716', 3, '朝阳区', '2026-03-11 07:01:00', 1, 3, 1, '111', '2026-03-10 16:22:29', '2026-03-10 16:22:29', 0);
INSERT INTO `t_schedule_task` VALUES (3, 'SCH1773881754266', 4, '海淀区', '2026-03-19 13:06:00', 2, 3, 1, '', '2026-03-19 08:55:54', '2026-03-19 08:55:54', 0);
INSERT INTO `t_schedule_task` VALUES (4, 'SCH1773885486532', 5, '海淀区', '2026-03-20 00:00:00', NULL, 0, 1, NULL, '2026-03-19 09:58:07', '2026-03-19 09:58:07', 0);

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `realname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `role` tinyint NOT NULL DEFAULT 2 COMMENT '角色：0-管理员 1-调度员 2-配送员',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'admin', '123456', '系统管理员', '13800000000', 'admin@example.com', 1, 0, NULL, 1, '2026-02-16 11:05:54', '2026-02-16 11:14:33', 0);
INSERT INTO `t_user` VALUES (2, 'scheduler01', '$2a$10$N.zmdrR5MaC6j4rdHdxQ/.Fv4vLuJrwEG3.ZZnj2hh3JnDvXuLSoG', '调度员张三', '13800000001', 'zhangsan@example.com', 1, 1, NULL, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);
INSERT INTO `t_user` VALUES (3, 'delivery01', '$2a$10$N.zmdrR5MaC6j4rdHdxQ/.Fv4vLuJrwEG3.ZZnj2hh3JnDvXuLSoG', '配送员李四', '13800000002', 'lisi@example.com', 1, 2, NULL, 1, '2026-02-16 11:05:54', '2026-02-16 11:05:54', 0);

SET FOREIGN_KEY_CHECKS = 1;
