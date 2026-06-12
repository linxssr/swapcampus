-- 测试数据 - 用于测试M3功能
USE swapcampus_db;

-- 1. 插入测试用户
INSERT INTO `user` (student_id, username, password, phone, credit_score, status, create_time, update_time) VALUES
('2024001', '张三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138001', 100, 1, NOW(), NOW()),
('2024002', '李四', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138002', 100, 1, NOW(), NOW());

-- 2. 插入测试分类
INSERT INTO `category` (category_name, sort, status) VALUES
('书籍教材', 1, 1),
('电子产品', 2, 1),
('生活用品', 3, 1),
('运动器材', 4, 1),
('服装配饰', 5, 1);

-- 3. 插入测试商品（已审核通过且已上架）
INSERT INTO `item` (user_id, category_id, title, description, price, quality, cover_url, audit_status, audit_admin_id, publish_status, create_time, update_time) VALUES
(1, 1, '高等数学第七版 同济大学', '全新未拆封', 35.00, 1, 'https://example.com/book1.jpg', 1, 1, 1, NOW(), NOW()),
(1, 1, '线性代数及其应用', '轻微使用痕迹', 20.00, 3, 'https://example.com/book2.jpg', 1, 1, 1, NOW(), NOW()),
(2, 2, '小米无线蓝牙耳机', '几乎全新，只用过几次', 120.00, 2, 'https://example.com/earphone.jpg', 1, 1, 1, NOW(), NOW()),
(2, 2, 'iPad 2020款 128GB', '有明显使用痕迹', 2500.00, 4, 'https://example.com/ipad.jpg', 1, 1, 1, NOW(), NOW()),
(1, 3, '台灯 LED护眼灯', '全新', 50.00, 1, 'https://example.com/lamp.jpg', 1, 1, 1, NOW(), NOW()),
(2, 4, '羽毛球拍 尤尼克斯', '几乎全新', 180.00, 2, 'https://example.com/badminton.jpg', 1, 1, 1, NOW(), NOW()),
(1, 5, '牛仔裤 L码', '轻微使用', 80.00, 3, 'https://example.com/jeans.jpg', 1, 1, 1, NOW(), NOW()),
(2, 5, '运动鞋 42码', '明显使用痕迹', 150.00, 4, 'https://example.com/shoes.jpg', 1, 1, 1, NOW(), NOW());

-- 4. 插入测试商品图片
INSERT INTO `item_image` (item_id, image_url, sort) VALUES
(1, 'https://example.com/book1-1.jpg', 1),
(1, 'https://example.com/book1-2.jpg', 2),
(2, 'https://example.com/book2-1.jpg', 1),
(3, 'https://example.com/earphone-1.jpg', 1),
(4, 'https://example.com/ipad-1.jpg', 1),
(5, 'https://example.com/lamp-1.jpg', 1),
(6, 'https://example.com/badminton-1.jpg', 1),
(7, 'https://example.com/jeans-1.jpg', 1),
(8, 'https://example.com/shoes-1.jpg', 1);
