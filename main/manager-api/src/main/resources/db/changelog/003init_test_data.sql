-- 初始化测试数据
-- 添加管理员用户（如果不存在）
INSERT INTO sys_user (id, username, password, status, super_admin, create_date, update_date)
SELECT '1', 'admin', '$2a$10$012Kx2ba5jzqr9gLlG4MX.bnQJTD9UWqF57XDo2N3.fPtLne02u/m', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

-- 添加测试部门（如果不存在）
INSERT INTO sys_params (id, param_code, param_value, param_type, remark, create_date, update_date)
SELECT '1', 'DEPT_NAME', '小智科技', 1, '部门名称', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_params WHERE param_code = 'DEPT_NAME');

-- 添加测试角色（如果不存在）
INSERT INTO sys_params (id, param_code, param_value, param_type, remark, create_date, update_date)
SELECT '2', 'ADMIN_ROLE', '超级管理员', 1, '管理员角色', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_params WHERE param_code = 'ADMIN_ROLE');

-- 添加测试设备（如果不存在）
INSERT INTO t_device (id, mac_address, client_id, name, type, status, user_id, creator, create_date, updater, update_date)
SELECT '1', 'AA:BB:CC:DD:EE:FF', 'test-device-001', '测试设备1', 'ESP32', 0, NULL, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM t_device WHERE id = 1);

INSERT INTO t_device (id, mac_address, client_id, name, type, status, user_id, creator, create_date, updater, update_date)
SELECT '2', 'AA:BB:CC:DD:EE:00', 'test-device-002', '测试设备2', 'ESP32', 1, NULL, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM t_device WHERE id = 2);

INSERT INTO t_device (id, mac_address, client_id, name, type, status, user_id, creator, create_date, updater, update_date)
SELECT '3', 'AA:BB:CC:DD:EE:11', 'test-device-003', '测试设备3', 'ESP32', 2, 1, 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM t_device WHERE id = 3); 