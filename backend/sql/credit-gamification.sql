USE swapcampus_db;

-- 签到记录表
CREATE TABLE IF NOT EXISTS `sign_record` (
    record_id       bigint       AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id         bigint       NOT NULL COMMENT '用户ID',
    sign_date       date         NOT NULL COMMENT '签到日期',
    continuous_days int          NOT NULL DEFAULT 1 COMMENT '当日连续签到天数',
    create_time     datetime     NOT NULL COMMENT '签到时间',
    UNIQUE KEY uk_user_date (user_id, sign_date),
    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 每日任务配置表（静态配置）
CREATE TABLE IF NOT EXISTS `task_config` (
    task_code    varchar(50)  NOT NULL PRIMARY KEY COMMENT '任务唯一编码',
    task_name    varchar(100) NOT NULL COMMENT '任务名称',
    description  varchar(200) NOT NULL COMMENT '任务描述',
    reward       int          NOT NULL DEFAULT 1 COMMENT '奖励积分',
    task_type    tinyint      NOT NULL DEFAULT 1 COMMENT '1-每日任务 2-一次性成就',
    sort         int          NOT NULL DEFAULT 0 COMMENT '排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务配置表';

-- 用户任务进度表
CREATE TABLE IF NOT EXISTS `user_task` (
    id           bigint   AUTO_INCREMENT PRIMARY KEY,
    user_id      bigint   NOT NULL COMMENT '用户ID',
    task_code    varchar(50) NOT NULL COMMENT '任务编码',
    task_date    date     NOT NULL COMMENT '任务日期（每日任务当天；成就任务用完成日）',
    status       tinyint  NOT NULL DEFAULT 0 COMMENT '0-未完成 1-已完成待领取 2-已领取',
    complete_time datetime NULL COMMENT '完成时间',
    reward_time  datetime NULL COMMENT '领取时间',
    UNIQUE KEY uk_user_task_date (user_id, task_code, task_date),
    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户任务进度表';

-- 初始化任务配置
INSERT IGNORE INTO task_config (task_code, task_name, description, reward, task_type, sort) VALUES
('DAILY_SIGN',     '每日签到',     '每天登录并签到',                  1, 1, 1),
('DAILY_BROWSE',   '浏览商品',     '今日浏览任意商品',                 1, 1, 2),
('DAILY_COLLECT',  '收藏商品',     '今日收藏任意商品',                 2, 1, 3),
('DAILY_CHAT',     '发起聊天',     '今日与卖家发起一次聊天',            2, 1, 4),
('ACH_FIRST_ITEM', '首次发布商品', '发布第一件闲置商品',                5, 2, 5),
('ACH_FIRST_ORDER','首次完成交易', '完成第一笔交易（买家确认收货）',     10, 2, 6),
('ACH_SIGN_7',     '坚持签到7天',  '连续签到满7天',                    10, 2, 7),
('ACH_SIGN_30',    '签到达人',     '累计签到满30天',                    20, 2, 8);
