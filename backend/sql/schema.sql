CREATE DATABASE IF NOT EXISTS swapcampus_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE swapcampus_db;

DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `item_collect`;
DROP TABLE IF EXISTS `item_image`;
DROP TABLE IF EXISTS `item`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `admin`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    user_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    student_id varchar(20) NOT NULL UNIQUE COMMENT '学号（实名唯一）',
    username varchar(50) NOT NULL COMMENT '昵称',
    password varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
    phone varchar(11) NULL COMMENT '手机号',
    avatar varchar(500) NULL COMMENT '头像（MinIO URL）',
    credit_score int NOT NULL DEFAULT 100 COMMENT '信用积分（初始100）',
    status tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-正常0-封禁',
    create_time datetime NOT NULL COMMENT '创建时间',
    update_time datetime NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='普通用户表';

CREATE TABLE `admin` (
    admin_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '管理员ID',
    admin_name varchar(50) NOT NULL COMMENT '管理员账号',
    password varchar(100) NOT NULL COMMENT '登录密码',
    real_name varchar(20) NULL COMMENT '真实姓名',
    phone varchar(11) NULL COMMENT '联系电话',
    status tinyint NOT NULL DEFAULT 1 COMMENT '1-正常0-禁用',
    create_time datetime NOT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

CREATE TABLE `category` (
    category_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    category_name varchar(30) NOT NULL COMMENT '分类名',
    sort int DEFAULT 0 COMMENT '排序权重',
    status tinyint DEFAULT 1 COMMENT '1-启用0-禁用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE `item` (
    item_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    user_id bigint NOT NULL COMMENT '发布者ID',
    category_id bigint NOT NULL COMMENT '分类ID',
    title varchar(100) NOT NULL COMMENT '商品标题',
    description text NULL COMMENT '商品描述',
    price decimal(10,2) NOT NULL COMMENT '价格',
    quality tinyint NOT NULL COMMENT '成色：1-全新2-几乎全新3-轻微使用痕迹4-明显使用痕迹',
    cover_url varchar(500) NOT NULL COMMENT '封面图（MinIO URL）',
    audit_status tinyint NOT NULL DEFAULT 0 COMMENT '0-待审核1-通过2-驳回',
    audit_admin_id bigint NULL COMMENT '审核管理员ID',
    publish_status tinyint NOT NULL DEFAULT 1 COMMENT '1-上架0-下架',
    create_time datetime NOT NULL COMMENT '发布时间',
    update_time datetime NOT NULL COMMENT '更新时间',
    CONSTRAINT fk_item_user FOREIGN KEY (user_id) REFERENCES `user`(user_id),
    CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES `category`(category_id),
    CONSTRAINT fk_item_audit_admin FOREIGN KEY (audit_admin_id) REFERENCES `admin`(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE `item_image` (
    image_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    item_id bigint NOT NULL COMMENT '商品ID',
    image_url varchar(500) NOT NULL COMMENT '图片URL（MinIO）',
    sort int DEFAULT 0 COMMENT '排序',
    CONSTRAINT fk_item_image_item FOREIGN KEY (item_id) REFERENCES `item`(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

CREATE TABLE `item_collect` (
    collect_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    user_id bigint NOT NULL COMMENT '用户ID',
    item_id bigint NOT NULL COMMENT '商品ID',
    create_time datetime NOT NULL COMMENT '收藏时间',
    CONSTRAINT fk_collect_user FOREIGN KEY (user_id) REFERENCES `user`(user_id),
    CONSTRAINT fk_collect_item FOREIGN KEY (item_id) REFERENCES `item`(item_id),
    CONSTRAINT uk_collect_user_item UNIQUE (user_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

CREATE TABLE `orders` (
    order_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no varchar(32) UNIQUE NOT NULL COMMENT '订单编号',
    buyer_id bigint NOT NULL COMMENT '买家ID',
    seller_id bigint NOT NULL COMMENT '卖家ID',
    item_id bigint NOT NULL COMMENT '商品ID',
    price decimal(10,2) NOT NULL COMMENT '成交价格',
    trade_type tinyint NOT NULL COMMENT '1-面交2-快递柜中转',
    order_status tinyint NOT NULL COMMENT '1-待确认2-待取件3-已完成4-已取消',
    create_time datetime NOT NULL COMMENT '创建时间',
    finish_time datetime NULL COMMENT '完成时间',
    CONSTRAINT fk_order_buyer FOREIGN KEY (buyer_id) REFERENCES `user`(user_id),
    CONSTRAINT fk_order_seller FOREIGN KEY (seller_id) REFERENCES `user`(user_id),
    CONSTRAINT fk_order_item FOREIGN KEY (item_id) REFERENCES `item`(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE `chat_message` (
    msg_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    from_uid bigint NOT NULL COMMENT '发送方ID',
    to_uid bigint NOT NULL COMMENT '接收方ID',
    content text NOT NULL COMMENT '消息内容',
    msg_type tinyint NOT NULL COMMENT '1-文字2-图片3-表情',
    image_url varchar(500) NULL COMMENT '图片URL（MinIO）',
    send_time datetime NOT NULL COMMENT '发送时间',
    is_read tinyint DEFAULT 0 COMMENT '0-未读1-已读',
    CONSTRAINT fk_chat_from_user FOREIGN KEY (from_uid) REFERENCES `user`(user_id),
    CONSTRAINT fk_chat_to_user FOREIGN KEY (to_uid) REFERENCES `user`(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

CREATE TABLE `comment` (
    comment_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    order_id bigint NOT NULL COMMENT '订单ID',
    buyer_id bigint NOT NULL COMMENT '买家ID',
    seller_id bigint NOT NULL COMMENT '卖家ID',
    item_id bigint NOT NULL COMMENT '商品ID',
    content varchar(500) NULL COMMENT '评价内容',
    score tinyint NOT NULL COMMENT '评分：1-5星',
    create_time datetime NOT NULL COMMENT '评价时间',
    CONSTRAINT fk_comment_order FOREIGN KEY (order_id) REFERENCES `orders`(order_id),
    CONSTRAINT fk_comment_buyer FOREIGN KEY (buyer_id) REFERENCES `user`(user_id),
    CONSTRAINT fk_comment_seller FOREIGN KEY (seller_id) REFERENCES `user`(user_id),
    CONSTRAINT fk_comment_item FOREIGN KEY (item_id) REFERENCES `item`(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

CREATE TABLE `report` (
    report_id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '举报ID',
    report_user bigint NOT NULL COMMENT '举报用户ID',
    item_id bigint NOT NULL COMMENT '被举报商品ID',
    reason varchar(200) NOT NULL COMMENT '举报原因',
    handle_status tinyint DEFAULT 0 COMMENT '0-待处理1-已处理',
    handle_admin_id bigint NULL COMMENT '处理管理员ID',
    handle_result varchar(200) NULL COMMENT '处理结果',
    create_time datetime NOT NULL COMMENT '举报时间',
    update_time datetime NULL COMMENT '更新时间/处理时间',
    CONSTRAINT fk_report_user FOREIGN KEY (report_user) REFERENCES `user`(user_id),
    CONSTRAINT fk_report_item FOREIGN KEY (item_id) REFERENCES `item`(item_id),
    CONSTRAINT fk_report_admin FOREIGN KEY (handle_admin_id) REFERENCES `admin`(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

CREATE INDEX idx_item_user_id ON `item`(user_id);
CREATE INDEX idx_item_category_id ON `item`(category_id);
CREATE INDEX idx_item_audit_status ON `item`(audit_status);
CREATE INDEX idx_order_buyer_id ON `orders`(buyer_id);
CREATE INDEX idx_order_seller_id ON `orders`(seller_id);
CREATE INDEX idx_order_status ON `orders`(order_status);
CREATE INDEX idx_chat_from_to ON `chat_message`(from_uid, to_uid);
CREATE INDEX idx_report_handle_admin ON `report`(handle_admin_id);
