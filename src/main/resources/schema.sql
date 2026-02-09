-- SQLite 建表脚本，首次启动时执行（spring.sql.init.mode=always 时）
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    currency TEXT NOT NULL,
    type TEXT NOT NULL,
    subtype TEXT,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS investment_targets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    market TEXT NOT NULL,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    currency TEXT NOT NULL,
    last_price REAL,
    price_updated_at TEXT,
    created_at TEXT NOT NULL,
    UNIQUE(market, code)
);

CREATE TABLE IF NOT EXISTS positions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    target_id INTEGER NOT NULL REFERENCES investment_targets(id) ON DELETE CASCADE,
    quantity REAL NOT NULL DEFAULT 0,
    updated_at TEXT NOT NULL,
    UNIQUE(account_id, target_id)
);

CREATE TABLE IF NOT EXISTS manual_balances (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    amount REAL NOT NULL,
    recorded_at TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS asset_snapshots (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    snapshot_at TEXT NOT NULL,
    trigger_type TEXT NOT NULL,
    total_value_cny REAL,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS snapshot_details (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    snapshot_id INTEGER NOT NULL REFERENCES asset_snapshots(id) ON DELETE CASCADE,
    account_id INTEGER NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    value REAL NOT NULL,
    currency TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_positions_account_id ON positions(account_id);
CREATE INDEX IF NOT EXISTS idx_positions_target_id ON positions(target_id);
CREATE INDEX IF NOT EXISTS idx_manual_balances_account_id ON manual_balances(account_id);
CREATE INDEX IF NOT EXISTS idx_asset_snapshots_user_id ON asset_snapshots(user_id);
CREATE INDEX IF NOT EXISTS idx_asset_snapshots_snapshot_at ON asset_snapshots(snapshot_at);
CREATE INDEX IF NOT EXISTS idx_snapshot_details_snapshot_id ON snapshot_details(snapshot_id);
