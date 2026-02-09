# 资产数据管理平台 - 设计说明

## 1. 数据模型

### 1.1 用户 (users)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| username | TEXT UNIQUE | 登录名 |
| password_hash | TEXT | 密码哈希（如 bcrypt） |
| created_at | DATETIME | 创建时间 |

### 1.2 账户 (accounts)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| user_id | INTEGER FK | 所属用户 |
| name | TEXT | 账户名称（如「海通证券」「老虎证券」） |
| currency | TEXT | 币种（CNY/USD/HKD 等） |
| type | TEXT | 大类型：asset / credit |
| subtype | TEXT | 资产账户子类型：investment / savings / other；信用账户可为 null |
| created_at | DATETIME | 创建时间 |

- **投资账户** (subtype=investment)：市值 = Σ(标的价格 × 持仓数量)，可绑定持仓与标的
- **储蓄/其他**：通过手动余额表更新

### 1.3 投资标的 (investment_targets)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| market | TEXT | 市场：HK / A_SHARE / US 等 |
| code | TEXT | 证券代码（如 00700, SH600519） |
| name | TEXT | 名称（如腾讯、茅台） |
| currency | TEXT | 币种（由市场决定） |
| last_price | REAL | 最近一次拉取价格 |
| price_updated_at | DATETIME | 价格更新时间 |
| created_at | DATETIME | 创建时间 |

唯一约束：(market, code)。

### 1.4 持仓 (positions)

仅对**投资账户**有效。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| account_id | INTEGER FK | 投资账户 |
| target_id | INTEGER FK | 投资标的 |
| quantity | REAL | 持仓数量 |
| updated_at | DATETIME | 最后更新时间 |

唯一约束：(account_id, target_id)。  
账户市值 = Σ(position.quantity * target.last_price)。

### 1.5 手动余额 (manual_balances)

用于**非投资账户**（储蓄、信用等）的金额录入。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| account_id | INTEGER FK | 账户 |
| amount | REAL | 金额 |
| recorded_at | DATETIME | 录入时间 |
| created_at | DATETIME | 创建时间 |

每个账户可保留一条「当前」记录，或按时间保留多条用于历史；快照时按 recorded_at 取当时有效值。

### 1.6 资产快照 (asset_snapshots)

用于历史走势与收益率计算。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| user_id | INTEGER FK | 用户 |
| snapshot_at | DATETIME | 快照时间 |
| trigger_type | TEXT | daily（收盘日快照）/ manual（手动或非投资账户更新时） |
| total_value_cny | REAL | 汇总价值（可选：按统一币种折算，便于画总资产曲线） |
| created_at | DATETIME | 创建时间 |

### 1.7 快照明细 (snapshot_details)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PK | 自增主键 |
| snapshot_id | INTEGER FK | 所属快照 |
| account_id | INTEGER FK | 账户 |
| value | REAL | 该账户在该快照时的价值 |
| currency | TEXT | 币种（冗余便于展示） |

便于按账户画分项走势、桑基图（账户→类型）等。

## 2. 快照策略

- **投资账户**：定时任务每日收盘后（如 18:00）对每个用户汇总其投资账户市值 + 非投资账户当前余额，写入一条 `trigger_type=daily` 的快照及明细。
- **非投资账户**：用户每次更新「手动余额」时，为该用户写一条 `trigger_type=manual` 的快照（可选：合并同一日多条为一条，避免噪音）。

## 3. 收益率与走势

- **资产走势图**：按 `snapshot_at` 排序，取 `total_value_cny`（或各 account 的 value）画折线。
- **收益率走势**：选定基准日 T0，对每个快照日 T 计算 `(V_T - V_T0) / V_T0`，纵轴为累计收益率。
- **区间收益**：选 T1、T2，返回 `(V_T2 - V_T1) / V_T1`；若有入金/出金，在 UI 注明「仅供参考」或后续扩展资金流水表做 TWR。

## 4. 投资标的价格

- 来源：公开 API（如新浪、东方财富、Yahoo Finance 等，需合规使用）。
- 更新方式：定时任务（如每日收盘后） + 前端「刷新价格」按钮触发。
- 表内维护 `last_price`、`price_updated_at`；持仓表只存数量，市值实时用数量 × last_price 计算。

## 5. 安全与多用户

- 所有接口按 `user_id` 过滤，禁止跨用户访问。
- 登录使用 session 或 JWT，密码仅存哈希。
- SQLite 文件建议放在用户可配置的目录，便于备份。
