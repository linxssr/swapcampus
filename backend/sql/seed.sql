-- ============================================
-- SwapCampus 种子数据 SQL
-- 管理员密码: admin123
-- 用户密码:   user123
-- ============================================

USE swapcampus_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE report;
TRUNCATE TABLE comment;
TRUNCATE TABLE chat_message;
TRUNCATE TABLE orders;
TRUNCATE TABLE item_collect;
TRUNCATE TABLE item_image;
TRUNCATE TABLE item;
TRUNCATE TABLE category;
TRUNCATE TABLE admin;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 1. 管理员 (密码: admin123)
-- BCrypt hash of "admin123"
-- ============================================
INSERT INTO admin (admin_name, password, real_name, phone, status, create_time) VALUES
('admin',      '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lF1yVhO7CLmqQdXxa', '系统管理员', '13800138000', 1, NOW()),
('superadmin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lF1yVhO7CLmqQdXxa', '超级管理员', '13800138001', 1, NOW());

-- ============================================
-- 2. 普通用户 20 个 (密码: user123)
-- BCrypt hash of "user123"
-- ============================================
INSERT INTO user (student_id, username, password, phone, avatar, credit_score, status, create_time, update_time) VALUES
('2021001001', '张小明', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345001', NULL, 100, 1, NOW(), NOW()),
('2021001002', '李小红', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345002', NULL,  98, 1, NOW(), NOW()),
('2021001003', '王小刚', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345003', NULL, 105, 1, NOW(), NOW()),
('2021001004', '赵小丽', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345004', NULL,  95, 1, NOW(), NOW()),
('2021001005', '刘小华', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345005', NULL, 100, 1, NOW(), NOW()),
('2021001006', '陈小军', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345006', NULL, 102, 1, NOW(), NOW()),
('2021001007', '杨小芳', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345007', NULL, 100, 1, NOW(), NOW()),
('2021001008', '周小伟', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345008', NULL,  97, 1, NOW(), NOW()),
('2021001009', '吴小静', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345009', NULL, 100, 1, NOW(), NOW()),
('2021001010', '郑小强', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345010', NULL, 100, 1, NOW(), NOW()),
('2022002001', '孙小梅', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345011', NULL, 100, 1, NOW(), NOW()),
('2022002002', '朱小龙', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345012', NULL, 103, 1, NOW(), NOW()),
('2022002003', '林小玉', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345013', NULL, 100, 1, NOW(), NOW()),
('2022002004', '胡小磊', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345014', NULL,  99, 1, NOW(), NOW()),
('2022002005', '高小婷', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345015', NULL, 100, 1, NOW(), NOW()),
('2022002006', '何小鹏', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345016', NULL, 100, 1, NOW(), NOW()),
('2023003001', '马小慧', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345017', NULL, 100, 1, NOW(), NOW()),
('2023003002', '罗小凯', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345018', NULL, 101, 1, NOW(), NOW()),
('2023003003', '宋小雨', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345019', NULL, 100, 1, NOW(), NOW()),
('2023003004', '韩小浩', '$2a$10$EblZqNptyYvc7.qb2Zy4suUaZzBPkzgOFqKvzjCKtM.s2xO/YhXvq', '13912345020', NULL, 100, 1, NOW(), NOW());

-- ============================================
-- 3. 分类 10 个
-- ============================================
INSERT INTO category (category_name, sort, status) VALUES
('教材教辅', 10, 1),
('数码电子', 20, 1),
('生活用品', 30, 1),
('服饰鞋包', 40, 1),
('运动器材', 50, 1),
('文具办公', 60, 1),
('美妆护理', 70, 1),
('图书杂志', 80, 1),
('乐器音响', 90, 1),
('其他物品', 100, 1);

-- ============================================
-- 4. 商品 200 条
-- user_id 1-20, category_id 1-10, 每类 20 条
-- audit_status: 1=通过, quality: 1-4
-- cover_url 使用 picsum.photos 占位图
-- ============================================
INSERT INTO item (user_id, category_id, title, description, price, quality, cover_url, audit_status, audit_admin_id, publish_status, create_time, update_time) VALUES
(1,  1, '高等数学教材第七版', '九成新，笔记清晰，适合大一学生', 35.00, 2, 'https://picsum.photos/seed/item1/400/300', 1, 1, 1, '2024-01-15 10:30:00', '2024-01-15 10:30:00'),
(2,  1, '线性代数习题集', '全新未使用，配套教材', 25.00, 1, 'https://picsum.photos/seed/item2/400/300', 1, 1, 1, '2024-01-16 11:20:00', '2024-01-16 11:20:00'),
(3,  1, '大学英语综合教程', '八成新，有划线标记', 30.00, 3, 'https://picsum.photos/seed/item3/400/300', 1, 1, 1, '2024-01-17 09:15:00', '2024-01-17 09:15:00'),
(4,  1, '概率论与数理统计', '七成新，功能完好', 28.00, 3, 'https://picsum.photos/seed/item4/400/300', 1, 1, 1, '2024-01-18 14:00:00', '2024-01-18 14:00:00'),
(5,  1, '数据结构与算法', '九成新，C语言版本', 40.00, 2, 'https://picsum.photos/seed/item5/400/300', 1, 1, 1, '2024-01-19 15:45:00', '2024-01-19 15:45:00'),
(6,  1, '计算机组成原理', '八成新，有笔记', 38.00, 3, 'https://picsum.photos/seed/item6/400/300', 1, 1, 1, '2024-01-20 10:00:00', '2024-01-20 10:00:00'),
(7,  1, '操作系统原理', '全新未拆封', 35.00, 1, 'https://picsum.photos/seed/item7/400/300', 1, 1, 1, '2024-01-21 13:30:00', '2024-01-21 13:30:00'),
(8,  1, '计算机网络第八版', '九成新，最新版本', 42.00, 2, 'https://picsum.photos/seed/item8/400/300', 1, 1, 1, '2024-01-22 16:20:00', '2024-01-22 16:20:00'),
(9,  1, '软件工程导论', '八成新，实用教材', 36.00, 3, 'https://picsum.photos/seed/item9/400/300', 1, 1, 1, '2024-01-23 09:00:00', '2024-01-23 09:00:00'),
(10, 1, '数据库系统概论', '九成新，有配套光盘', 38.00, 2, 'https://picsum.photos/seed/item10/400/300', 1, 1, 1, '2024-01-24 11:00:00', '2024-01-24 11:00:00'),
(11, 1, 'C语言程序设计', '全新，买重复了', 32.00, 1, 'https://picsum.photos/seed/item11/400/300', 1, 1, 1, '2024-01-25 14:30:00', '2024-01-25 14:30:00'),
(12, 1, 'Python编程基础', '九成新，入门首选', 35.00, 2, 'https://picsum.photos/seed/item12/400/300', 1, 1, 1, '2024-01-26 10:15:00', '2024-01-26 10:15:00'),
(13, 1, 'Java核心技术卷I', '八成新，经典教材', 55.00, 3, 'https://picsum.photos/seed/item13/400/300', 1, 1, 1, '2024-01-27 15:00:00', '2024-01-27 15:00:00'),
(14, 1, '离散数学教程', '九成新，有例题解析', 33.00, 2, 'https://picsum.photos/seed/item14/400/300', 1, 1, 1, '2024-01-28 09:45:00', '2024-01-28 09:45:00'),
(15, 1, '大学物理实验', '全新，未使用', 28.00, 1, 'https://picsum.photos/seed/item15/400/300', 1, 1, 1, '2024-01-29 13:20:00', '2024-01-29 13:20:00'),
(16, 1, '电路分析基础', '八成新，电气专业', 40.00, 3, 'https://picsum.photos/seed/item16/400/300', 1, 1, 1, '2024-01-30 16:00:00', '2024-01-30 16:00:00'),
(17, 1, '信号与系统', '九成新，通信必修', 45.00, 2, 'https://picsum.photos/seed/item17/400/300', 1, 1, 1, '2024-01-31 10:30:00', '2024-01-31 10:30:00'),
(18, 1, '数字电路设计', '八成新，有实验报告', 38.00, 3, 'https://picsum.photos/seed/item18/400/300', 1, 1, 1, '2024-02-01 14:15:00', '2024-02-01 14:15:00'),
(19, 1, '微积分学习指导', '全新，配套习题', 26.00, 1, 'https://picsum.photos/seed/item19/400/300', 1, 1, 1, '2024-02-02 11:00:00', '2024-02-02 11:00:00'),
(20, 1, '英语四级真题集', '九成新，含答案解析', 22.00, 2, 'https://picsum.photos/seed/item20/400/300', 1, 1, 1, '2024-02-03 15:30:00', '2024-02-03 15:30:00'),

(21, 2, '小米10充电器20W', '全新未拆封，多余配件', 25.00, 1, 'https://picsum.photos/seed/item21/400/300', 1, 1, 1, '2024-02-04 10:00:00', '2024-02-04 10:00:00'),
(22, 2, '苹果数据线原装', '九成新，Type-C接口', 35.00, 2, 'https://picsum.photos/seed/item22/400/300', 1, 1, 1, '2024-02-05 13:45:00', '2024-02-05 13:45:00'),
(23, 2, '罗技无线鼠标', '八成新，无线蓝牙', 45.00, 3, 'https://picsum.photos/seed/item23/400/300', 1, 1, 1, '2024-02-06 09:30:00', '2024-02-06 09:30:00'),
(24, 2, '机械键盘青轴', '九成新，RGB背光', 120.00, 2, 'https://picsum.photos/seed/item24/400/300', 1, 1, 1, '2024-02-07 14:20:00', '2024-02-07 14:20:00'),
(25, 2, '小米蓝牙耳机', '全新未拆封', 80.00, 1, 'https://picsum.photos/seed/item25/400/300', 1, 1, 1, '2024-02-08 11:15:00', '2024-02-08 11:15:00'),
(26, 2, '苹果AirPods二代', '九成新，含充电盒', 450.00, 2, 'https://picsum.photos/seed/item26/400/300', 1, 1, 1, '2024-02-09 16:00:00', '2024-02-09 16:00:00'),
(27, 2, '华为手环6', '八成新，功能正常', 150.00, 3, 'https://picsum.photos/seed/item27/400/300', 1, 1, 1, '2024-02-10 10:45:00', '2024-02-10 10:45:00'),
(28, 2, 'iPad保护套', '全新，10.2寸通用', 60.00, 1, 'https://picsum.photos/seed/item28/400/300', 1, 1, 1, '2024-02-11 13:30:00', '2024-02-11 13:30:00'),
(29, 2, '手机支架桌面', '九成新，可折叠', 15.00, 2, 'https://picsum.photos/seed/item29/400/300', 1, 1, 1, '2024-02-12 15:15:00', '2024-02-12 15:15:00'),
(30, 2, '笔记本散热器', '八成新，USB供电', 35.00, 3, 'https://picsum.photos/seed/item30/400/300', 1, 1, 1, '2024-02-13 09:00:00', '2024-02-13 09:00:00'),
(31, 2, 'USB集线器4口', '全新，3.0高速', 25.00, 1, 'https://picsum.photos/seed/item31/400/300', 1, 1, 1, '2024-02-14 11:45:00', '2024-02-14 11:45:00'),
(32, 2, '移动硬盘1T', '九成新，西部数据', 280.00, 2, 'https://picsum.photos/seed/item32/400/300', 1, 1, 1, '2024-02-15 14:30:00', '2024-02-15 14:30:00'),
(33, 2, 'U盘64G闪迪', '全新未拆封', 35.00, 1, 'https://picsum.photos/seed/item33/400/300', 1, 1, 1, '2024-02-16 10:15:00', '2024-02-16 10:15:00'),
(34, 2, '读卡器多合一', '九成新，支持TF/SD', 18.00, 2, 'https://picsum.photos/seed/item34/400/300', 1, 1, 1, '2024-02-17 13:00:00', '2024-02-17 13:00:00'),
(35, 2, '笔记本内胆包', '八成新，15.6寸', 40.00, 3, 'https://picsum.photos/seed/item35/400/300', 1, 1, 1, '2024-02-18 15:45:00', '2024-02-18 15:45:00'),
(36, 2, '电脑清洁套装', '全新，含清洁液', 22.00, 1, 'https://picsum.photos/seed/item36/400/300', 1, 1, 1, '2024-02-19 09:30:00', '2024-02-19 09:30:00'),
(37, 2, '显示器支架', '九成新，可升降', 65.00, 2, 'https://picsum.photos/seed/item37/400/300', 1, 1, 1, '2024-02-20 11:15:00', '2024-02-20 11:15:00'),
(38, 2, '网线5米六类', '全新，千兆', 12.00, 1, 'https://picsum.photos/seed/item38/400/300', 1, 1, 1, '2024-02-21 14:00:00', '2024-02-21 14:00:00'),
(39, 2, 'Type-C转接头', '九成新，多功能', 15.00, 2, 'https://picsum.photos/seed/item39/400/300', 1, 1, 1, '2024-02-22 16:30:00', '2024-02-22 16:30:00'),
(40, 2, '小米充电宝10000mAh', '八成新，双向快充', 55.00, 3, 'https://picsum.photos/seed/item40/400/300', 1, 1, 1, '2024-02-23 10:00:00', '2024-02-23 10:00:00');
