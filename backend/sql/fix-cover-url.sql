-- 修复数据库中错误的 cover_url 值
-- 执行前请先备份数据
USE swapcampus_db;

-- 1. 修复大量 demo.jpg 占位数据（210条），用 picsum 对应商品ID的图片替代
UPDATE item
SET cover_url = CONCAT('https://picsum.photos/seed/item', item_id, '/400/300')
WHERE cover_url = 'http://localhost:9000/swapcampus/item/demo.jpg';

-- 2. 修复 example.com 假数据
UPDATE item
SET cover_url = CONCAT('https://picsum.photos/seed/item', item_id, '/400/300')
WHERE cover_url LIKE 'https://example.com/%';

-- 3. 修复 picsum 200/150 格式（尺寸太小）
UPDATE item
SET cover_url = CONCAT('https://picsum.photos/seed/item', item_id, '/400/300')
WHERE cover_url LIKE 'https://picsum.photos/200/150%';

-- 验证：查看修复后剩余的非标准 cover_url
SELECT item_id, title, cover_url
FROM item
WHERE cover_url NOT LIKE 'https://picsum.photos/seed/%'
  AND cover_url NOT LIKE '/minio/%'
LIMIT 20;
