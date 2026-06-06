# SwapCampus Frontend

Vue3 + Vite + TypeScript 前端初始工程。

## 技术栈

- Vue3
- Vite
- TypeScript
- Vue Router
- Pinia
- Axios
- Element Plus
- WebSocket
- Vitest

## 页面结构

普通用户端：

- 首页
- 商品列表
- 商品详情
- 发布闲置
- 个人中心
- 我的订单
- 聊天页面
- 登录/注册

管理员端：

- 管理员登录
- 数据看板
- 商品审核
- 用户管理
- 举报处理
- 分类管理

## 接口约定

后端接口统一前缀为 `/api/v1`，WebSocket 地址为 `/ws/chat?token=xxx`。

开发环境默认代理到 `http://localhost:8080`，可在 `.env.development` 中调整。

## 运行

```bash
npm install
npm run dev
```

## 构建

```bash
npm run build
```

当前工程只包含前端结构、依赖、配置、路由、状态、请求封装和页面占位，不包含具体业务实现。
