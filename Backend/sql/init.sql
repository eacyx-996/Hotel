-- =============================================
-- 酒店管理平台 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS hotel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hotel;

-- =============================================
-- 1. 用户表
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role` VARCHAR(20) NOT NULL DEFAULT 'STAFF' COMMENT '角色: ADMIN/STAFF',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 房型表
-- =============================================
DROP TABLE IF EXISTS `room_type`;
CREATE TABLE `room_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '房型名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '房型描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价(元/天)',
  `area` DECIMAL(8,2) DEFAULT NULL COMMENT '面积(平方米)',
  `capacity` INT NOT NULL DEFAULT 2 COMMENT '可容纳人数',
  `facilities` VARCHAR(500) DEFAULT NULL COMMENT '设施(逗号分隔)',
  `image` VARCHAR(255) DEFAULT NULL COMMENT '房型图片',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房型表';

-- =============================================
-- 3. 客房表
-- =============================================
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `room_number` VARCHAR(20) NOT NULL COMMENT '房间号',
  `room_type_id` BIGINT NOT NULL COMMENT '房型ID',
  `floor` INT DEFAULT NULL COMMENT '楼层',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0空闲 1已预订 2已入住 3清洁中 4维修中',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_number` (`room_number`),
  KEY `idx_room_type_id` (`room_type_id`),
  CONSTRAINT `fk_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `room_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客房表';

-- =============================================
-- 4. 订单表
-- =============================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `room_id` BIGINT NOT NULL COMMENT '客房ID',
  `room_type_id` BIGINT NOT NULL COMMENT '房型ID(冗余)',
  `guest_name` VARCHAR(50) NOT NULL COMMENT '客人姓名',
  `guest_phone` VARCHAR(20) NOT NULL COMMENT '客人手机',
  `guest_id_card` VARCHAR(30) DEFAULT NULL COMMENT '客人身份证号',
  `check_in_date` DATE NOT NULL COMMENT '入住日期',
  `check_out_date` DATE NOT NULL COMMENT '离店日期',
  `days` INT NOT NULL COMMENT '入住天数',
  `price` DECIMAL(10,2) NOT NULL COMMENT '房费单价',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总额',
  `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态: 0未支付 1已支付 2已退款',
  `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0待确认 1已确认 2已入住 3已退房 4已取消',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_order_status` (`order_status`),
  CONSTRAINT `fk_order_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =============================================
-- 初始化数据
-- =============================================

-- 管理员账号: admin / 123456  (BCrypt加密)
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800138000', 'ADMIN'),
('staff01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '前台小王', '13800138001', 'STAFF'),
('staff02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '前台小李', '13800138002', 'STAFF');

-- 房型
INSERT INTO `room_type` (`name`, `description`, `price`, `area`, `capacity`, `facilities`) VALUES
('标准大床房', '1.8米大床,简洁舒适,适合商务出差', 388.00, 28.00, 2, 'WiFi,电视,空调,热水器,洗漱用品'),
('标准双床房', '两张1.2米单人床,适合结伴出行', 388.00, 28.00, 2, 'WiFi,电视,空调,热水器,洗漱用品'),
('豪华大床房', '2.0米大床,独立阳台,城市景观', 588.00, 35.00, 2, 'WiFi,电视,空调,热水器,浴缸,洗漱用品,迷你吧'),
('豪华套房', '独立客厅+卧室,适合家庭或商务会客', 888.00, 55.00, 4, 'WiFi,电视,空调,热水器,浴缸,洗漱用品,迷你吧,咖啡机'),
('行政套房', '高层景观,专属行政待遇', 1288.00, 65.00, 2, 'WiFi,电视,空调,热水器,浴缸,洗漱用品,迷你吧,咖啡机,早餐'),
('家庭房', '一张大床+一张单人床,适合家庭出游', 568.00, 42.00, 3, 'WiFi,电视,空调,热水器,洗漱用品,儿童用品');

-- 客房
INSERT INTO `room` (`room_number`, `room_type_id`, `floor`, `status`) VALUES
('101', 1, 1, 0), ('102', 1, 1, 0), ('103', 2, 1, 0), ('104', 2, 1, 0),
('105', 2, 1, 0), ('201', 3, 2, 0), ('202', 3, 2, 0), ('203', 3, 2, 0),
('204', 6, 2, 0), ('205', 6, 2, 0), ('301', 4, 3, 0), ('302', 4, 3, 0),
('303', 4, 3, 0), ('304', 5, 3, 0), ('305', 5, 3, 0), ('306', 5, 3, 0);

-- 示例订单
INSERT INTO `orders` (`order_no`, `room_id`, `room_type_id`, `guest_name`, `guest_phone`, `check_in_date`, `check_out_date`, `days`, `price`, `total_amount`, `pay_status`, `order_status`) VALUES
('H20261026001', 1, 1, '张三', '13912345678', '2026-10-26', '2026-10-28', 2, 388.00, 776.00, 1, 2),
('H20261026002', 6, 3, '李四', '13987654321', '2026-10-25', '2026-10-27', 2, 588.00, 1176.00, 1, 2),
('H20261026003', 11, 4, '王五', '13611112222', '2026-10-27', '2026-10-29', 2, 888.00, 1776.00, 0, 1),
('H20261026004', 14, 5, '赵六', '13633334444', '2026-10-28', '2026-10-30', 2, 1288.00, 2576.00, 0, 0);
