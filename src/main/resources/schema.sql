-- MySQL 建表脚本
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subtype VARCHAR(50),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS investment_targets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    market VARCHAR(20) NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    last_price DOUBLE,
    price_updated_at DATETIME,
    created_at DATETIME NOT NULL,
    UNIQUE(market, code)
);

CREATE TABLE IF NOT EXISTS positions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    quantity DOUBLE NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL,
    UNIQUE(account_id, target_id),
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (target_id) REFERENCES investment_targets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS manual_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    recorded_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS asset_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    snapshot_at DATETIME NOT NULL,
    trigger_type VARCHAR(20) NOT NULL,
    total_value_cny DOUBLE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS snapshot_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    value DOUBLE NOT NULL,
    currency VARCHAR(10) NOT NULL,
    FOREIGN KEY (snapshot_id) REFERENCES asset_snapshots(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_positions_account_id ON positions(account_id);
CREATE INDEX idx_positions_target_id ON positions(target_id);
CREATE INDEX idx_manual_balances_account_id ON manual_balances(account_id);
CREATE INDEX idx_asset_snapshots_user_id ON asset_snapshots(user_id);
CREATE INDEX idx_asset_snapshots_snapshot_at ON asset_snapshots(snapshot_at);
CREATE INDEX idx_snapshot_details_snapshot_id ON snapshot_details(snapshot_id);
