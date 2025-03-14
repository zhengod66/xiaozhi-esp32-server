-- 设备表
CREATE TABLE t_device (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    mac_address VARCHAR(50) NOT NULL COMMENT '设备MAC地址',
    client_id VARCHAR(50) NOT NULL COMMENT '设备UUID',
    name VARCHAR(100) COMMENT '设备名称',
    type VARCHAR(50) COMMENT '设备类型',
    status TINYINT DEFAULT 0 COMMENT '设备状态：0-未激活 1-等待激活 2-已激活',
    user_id BIGINT COMMENT '关联用户ID',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mac_address (mac_address),
    UNIQUE KEY uk_client_id (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 激活码表
CREATE TABLE t_activation_code (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    code VARCHAR(6) NOT NULL COMMENT '6位数字激活码',
    device_id BIGINT COMMENT '关联设备ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-有效 1-已使用 2-已过期',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='激活码表';

-- 访问令牌表
CREATE TABLE t_access_token (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    device_id BIGINT NOT NULL COMMENT '关联设备ID',
    token TEXT NOT NULL COMMENT 'JWT令牌',
    is_revoked TINYINT DEFAULT 0 COMMENT '是否已撤销：0-否 1-是',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问令牌表';

-- 添加外键约束
ALTER TABLE t_activation_code ADD CONSTRAINT fk_activation_device FOREIGN KEY (device_id) REFERENCES t_device (id);
ALTER TABLE t_access_token ADD CONSTRAINT fk_token_device FOREIGN KEY (device_id) REFERENCES t_device (id); 