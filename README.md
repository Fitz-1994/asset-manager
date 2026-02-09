# 私人资产数据管理平台

本地化、多用户的资产与投资数据管理，数据全部存储在本地 SQLite，保护隐私。

## 功能概览

- **多用户**：用户数据隔离，每人只看到自己的账户与资产
- **账户分层**：资金账户（股票/证券/储蓄等），支持币种、类型（资产/信用；投资/储蓄）
- **投资账户**：通过「标的 × 数量」自动汇总市值，并可用公开 API 更新标的价格
- **非投资账户**：支持手动录入与更新
- **资产快照**：投资账户按日收盘快照；非投资账户在每次更新时快照，用于历史走势与收益率分析
- **可视化**：饼图、桑基图、资产走势图、收益率走势图

## 技术方案

- **存储**：SQLite，单文件数据库（`./data/asset_manager.db`），便于备份与迁移
- **后端**：**Java 17 + Spring Boot 3.x**，JPA/Hibernate，Spring Security + JWT，SQLite
- **前端**：Web 单页，ECharts 做饼图、走势图、收益率图
- **运行**：本地运行后端，浏览器访问；数据不离开本机

## 快速开始（Java 后端）

```bash
# 需 Java 17+
cd asset-manager

# 编译并启动
./mvnw spring-boot:run

# 或先打包再运行
./mvnw -q package
java -jar target/asset-manager-0.1.0.jar
```

浏览器打开：**http://127.0.0.1:8000**

- 首次运行会自动创建 `data` 目录和 SQLite 数据库（JPA `ddl-auto: update`）
- 在登录页先**注册**用户，再**登录**即可使用

## 项目结构

```
asset-manager/
├── src/main/java/com/assetmanager/
│   ├── AssetManagerApplication.java
│   ├── config/          # 安全、JWT、SQLite 方言、数据目录初始化
│   ├── entity/          # JPA 实体
│   ├── repository/       # Spring Data JPA
│   ├── dto/              # 请求/响应 DTO（与前端 API 一致）
│   ├── service/          # 业务逻辑（账户市值、快照、价格拉取）
│   ├── controller/       # REST 接口（/api/auth, /api/accounts, ...）
│   └── security/         # JWT 过滤器
├── src/main/resources/
│   └── application.yml   # 端口 8000、SQLite 路径、JWT 配置
├── frontend/dist/        # 前端静态资源（由 Spring 从 file:./frontend/dist/ 提供）
├── data/                 # SQLite 数据库（自动创建）
├── pom.xml
└── README.md
```

## 配置

- **数据库路径**：`application.yml` 中 `spring.datasource.url: jdbc:sqlite:./data/asset_manager.db`
- **JWT**：`app.jwt.secret`、`app.jwt.expiration-ms`，生产环境请修改 `secret`
- **前端静态**：默认从 `file:./frontend/dist/` 提供，可与后端同端口访问

## 许可证

MIT
