# SwapCampus Backend

Spring Boot 3 Maven 聚合多模块后端工程初始结构。

## 模块

- `swapcampus-common`：公共配置、工具、异常、统一响应、鉴权占位
- `swapcampus-model`：实体、DTO、VO
- `swapcampus-mapper`：MyBatis Mapper 接口与 XML
- `module-m1-user`：用户体系模块
- `module-m2-item`：商品发布模块
- `module-m3-search`：商品检索与收藏模块
- `module-m4-order`：订单交易与评价模块
- `module-m5-chat`：站内通讯模块
- `module-m6-admin`：管理员后台模块
- `swapcampus-app`：启动模块

## 运行前配置

1. 创建 MySQL 数据库并执行 `backend/sql/schema.sql`。
2. 按需修改 `backend/swapcampus-app/src/main/resources/application.yml` 中的 MySQL、MinIO 配置。
3. JWT 不需要提前申请或生成账号密钥，`swapcampus.jwt.secret` 是后端用于签发和校验登录 token 的本地密钥。开发环境已提供默认值，生产环境再替换为更长、更随机的字符串即可。
4. 在项目根目录或 `backend` 目录均可执行 Maven 构建：

```bash
mvn clean install
```

5. 启动 `swapcampus-app` 模块中的 `SwapCampusApplication`。

当前工程只包含初始项目结构、依赖、配置和占位类，业务代码待后续按模块开发。
