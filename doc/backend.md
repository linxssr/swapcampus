# 后端工程结构文档（backend.md）
## 一、概述
后端采用 **Maven聚合多模块 + 按业务M1~M6模块化拆分**，SpringBoot3开发，分层：公共依赖层→数据模型层→DAO层→业务模块层→启动聚合层，适配6大业务模块，便于课设分工开发、后期文档撰写与答辩。
**父工程：backend，所有子模块统一继承父pom版本管理**

## 二、整体目录树
```
backend/
├── pom.xml                               # 父工程POM：统一管理依赖版本
├── swapcampus-common/                     # 公共通用模块（全模块依赖）
│   ├── pom.xml
│   └── src/main/java/com/campus/common/
│       ├── config/                       # 全局配置类
│       │   ├── JwtConfig.java
│       │   ├── MinIOConfig.java
│       │   └── WebSocketConfig.java
│       ├── utils/                        # 工具类
│       │   ├── JwtUtil.java
│       │   ├── BCryptUtil.java
│       │   └── MinIOUtil.java
│       ├── exception/                    # 全局统一异常处理
│       │   ├── GlobalExceptionHandler.java
│       │   └── BusinessException.java
│       └── interceptor/                  # JWT拦截器、权限控制
│           ├── JwtInterceptor.java
│           └── AuthAnnotation.java
│
├── swapcampus-model/                     # 实体、入参DTO、出参VO
│   ├── pom.xml
│   └── src/main/java/com/campus/model/
│       ├── entity/                       # 数据库10张表实体类
│       ├── dto/                          # 接口请求入参
│       └── vo/                           # 接口返回封装对象
│
├── swapcampus-mapper/                    # Mybatis数据访问层
│   ├── pom.xml
│   ├── src/main/java/com/campus/mapper/  # 所有Mapper接口
│   └── src/main/resources/mybatis/       # Mapper.xml映射文件
│
├── module-m1-user/                       # M1 用户体系模块
│   ├── pom.xml
│   └── src/main/java/com/campus/user/
│       ├── controller/
│       │   └── UserController.java
│       └── service/
│           ├── UserService.java
│           └── impl/UserServiceImpl.java
│
├── module-m2-item/                       # M2 商品发布模块
│   ├── pom.xml
│   └── src/main/java/com/campus/item/
│       ├── controller/ItemController.java
│       ├── service/impl/ItemServiceImpl.java
│       └── task/                         # 预留定时任务目录
│
├── module-m3-search/                     # M3 商品检索+收藏
│   ├── pom.xml
│   └── src/main/java/com/campus/search/
│       ├── controller/
│       │   ├── CategoryController.java
│       │   └── CollectController.java
│       └── service/impl/
│
├── module-m4-order/                      # M4 订单+评价
│   ├── pom.xml
│   └── src/main/java/com/campus/order/
│       ├── controller/OrderController.java
│       ├── service/impl/
│       └── config/                       # 订单状态配置
│
├── module-m5-chat/                       # M5 WebSocket聊天
│   ├── pom.xml
│   └── src/main/java/com/campus/chat/
│       ├── controller/ChatController.java
│       ├── service/impl/
│       ├── websocket/ChatServer.java
│       └── config/WebSocketConfig.java
│
├── module-m6-admin/                      # M6 管理员后台
│   ├── pom.xml
│   └── src/main/java/com/campus/admin/
│       ├── controller/AdminController.java
│       └── service/impl/
│
└── swapcampus-app/                       # 项目启动主模块
    ├── pom.xml
    └── src/main/
        ├── java/com/campus/SwapCampusApplication.java
        └── resources/application.yml      # 全局配置文件
```

## 三、模块依赖关系
1. `swapcampus-app`：依赖全部6个业务模块(m1~m6) + common + model + mapper
2. `module-m1~m6`：统一依赖 `swapcampus-common`、`swapcampus-model`、`swapcampus-mapper`
3. `common/model/mapper`：基础公共模块，无互相依赖

## 四、各模块功能说明
### 1. swapcampus-common（公共模块）
1. config：MinIO连接配置、JWT拦截配置、WebSocket全局配置
2. utils：JWT生成解析、密码加密、图片上传工具类
3. exception：全局异常捕获、自定义业务异常
4. interceptor：请求token校验、接口权限拦截

### 2. swapcampus-model
- entity：与数据库10张表一一对应实体
- dto：前端提交请求参数封装（登录、发布商品、下单等入参）
- vo：接口返回给前端的封装数据（脱敏、组装数据）

### 3. swapcampus-mapper
- Mapper接口：Mybatis数据库操作接口
- XML文件：SQL语句，统一放在resources/mybatis下

### 4. module-m1-user【M1用户体系】
用户注册、学号登录(JWT)、修改资料、修改密码、个人信用、我的发布/订单；普通用户接口。

### 5. module-m2-item【M2商品发布】
商品新增、编辑、删除、图片上传MinIO，商品提交进入待审核状态。

### 6. module-m3-search【M3检索+收藏】
分类查询、关键词搜索、高级筛选、商品收藏/取消收藏、我的收藏。

### 7. module-m4-order【M4订单交易+评价】
创建订单、确认收货、取消订单、商品评价、评价后信用分变更逻辑。

### 8. module-m5-chat【M5即时通讯】
WebSocket点对点聊天、文字/图片消息收发、历史聊天记录查询、聊天图片MinIO存储。

### 9. module-m6-admin【M6管理员后台】
管理员独立登录、商品审核、用户封禁、举报处理、商品分类管理。

### 10. swapcampus-app
唯一启动入口，yml配置数据库、MinIO、JWT参数，打包运行主程序。

## 五、开发规范
1. 接口统一返回：`Result<T>`（common中封装统一返回格式：code、msg、data）
2. 所有请求鉴权：除登录接口外，其余接口通过JwtInterceptor自动校验token
3. 图片全部走MinIO上传，数据库只存URL地址

## 六、启动步骤
1. Maven依次install：common → model → mapper → m1~m6 → app
2. 修改swapcampus-app/application.yml的数据库、MinIO连接信息
3. 运行`SwapCampusApplication.java`启动项目，默认8080端口

