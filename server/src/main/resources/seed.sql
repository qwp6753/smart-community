INSERT INTO `user` (username, password, real_name, mobile, status) VALUES
('admin', '$2a$10$uIhq8JhLT.PgQ57/bV5AIuzG6pBAVD7Vj2Wyl.DymUB3QhxUKLkxm', '系统管理员', '13800000000', 1);

INSERT INTO role (role_name, role_code, description, status) VALUES
('超级管理员', 'ADMIN', '超级管理员角色', 1);

INSERT INTO user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO menu (menu_id, menu_name, path, component, icon, parent_id, sort, type, permission, status) VALUES
(1,  '仪表盘',  '/dashboard',        'dashboard/index',    'Odometer',     0, 1, 1, 'dashboard:view',          1),
(2,  '系统管理','/system',           'Layout',             'Setting',      0, 2, 0, 'system:user:view',        1),
(3,  '用户管理','/system/users',     'system/user/index',  'User',         2, 1, 1, 'system:user:view',        1),
(4,  '角色管理','/system/roles',     'system/role/index',  'UserFilled',   2, 2, 1, 'system:role:view',        1),
(5,  '菜单管理','/system/menus',     'system/menu/index',  'Menu',         2, 3, 1, 'system:menu:view',        1),
(20, '数据字典','/system/dict',      'system/dict/index',  'Document',     2, 4, 1, 'system:dict:view',        1),
(6,  '物业管理','/property',         'Layout',             'OfficeBuilding',0, 3, 0, 'property:community:view', 1),
(7,  '小区管理','/property/communities','property/community/index','House',6, 1, 1, 'property:community:view', 1),
(8,  '摄像头管理','/property/cameras','property/camera/index','VideoCamera',6, 2, 1, 'property:camera:view',    1),
(9,  '居民管理','/property/persons', 'property/person/index','Avatar',     6, 3, 1, 'property:person:view',     1),
(10, '门禁管理','/access',           'Layout',             'Lock',         0, 4, 0, 'access:record:view',      1),
(11, '出入记录','/access/records',   'access/record/index','Tickets',      10,1, 1, 'access:record:view',      1),
(12, '访客登记','/access/visitors',  'access/visitor/index','Notebook',    10,2, 1, 'access:visitor:view',     1),
-- 按钮级权限（type=2）
(13, '用户编辑', NULL, NULL, NULL, 3, 1, 2, 'system:user:edit',        1),
(14, '角色编辑', NULL, NULL, NULL, 4, 1, 2, 'system:role:edit',        1),
(15, '菜单编辑', NULL, NULL, NULL, 5, 1, 2, 'system:menu:edit',        1),
(16, '小区编辑', NULL, NULL, NULL, 7, 1, 2, 'property:community:edit', 1),
(17, '摄像头编辑', NULL, NULL, NULL, 8, 1, 2, 'property:camera:edit',  1),
(18, '居民编辑', NULL, NULL, NULL, 9, 1, 2, 'property:person:edit',    1),
(19, '访客编辑', NULL, NULL, NULL, 12, 1, 2, 'access:visitor:edit',    1),
(21, '字典编辑', NULL, NULL, NULL, 20, 1, 2, 'system:dict:edit',       1);

INSERT INTO role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 6), (1, 7), (1, 8), (1, 9),
(1, 10), (1, 11), (1, 12),
(1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20), (1, 21);

INSERT INTO community (name, address, map_lng, map_lat, total_building, total_house, status) VALUES
('智慧社区一号', '北京市朝阳区望京街道100号',  116.480, 39.996, 8,  320, 1),
('智慧社区二号', '上海市浦东新区张江高科技园区', 121.590, 31.208, 12, 480, 1);

INSERT INTO camera (name, ip_address, community_id, location, status) VALUES
('北门主摄像头', '192.168.1.101', 1, '北门入口', 1),
('南门主摄像头', '192.168.1.102', 1, '南门入口', 1),
('东门主摄像头', '192.168.1.201', 2, '东门入口', 1),
('西门主摄像头', '192.168.1.202', 2, '西门入口', 0);

INSERT INTO person (community_id, user_name, mobile, sex, house_no, person_type, state, remark) VALUES
(1, '张三', '13800000001', '男', '1-101',  'owner',  1, ''),
(1, '李四', '13800000002', '女', '1-102',  'owner',  1, ''),
(1, '王五', '13800000003', '男', '2-201',  'tenant', 1, '租户'),
(2, '赵六', '13800000004', '女', 'A-301',  'owner',  1, ''),
(2, '孙七', '13800000005', '男', 'A-302',  'owner',  1, '');

INSERT INTO in_out_record (person_id, person_name, community_id, type, time, location, verified) VALUES
(1, '张三', 1, 'in',  NOW(), '北门入口', 1),
(2, '李四', 1, 'out', NOW(), '南门入口', 1),
(3, '王五', 1, 'in',  NOW(), '北门入口', 1),
(4, '赵六', 2, 'in',  NOW(), '东门入口', 1),
(5, '孙七', 2, 'out', NOW(), '东门入口', 1),
(1, '张三', 1, 'out', NOW(), '南门入口', 1);

INSERT INTO visitor (person_id, name, mobile, id_card, community_id, visit_time, reason, status) VALUES
(2, '钱八', '13900000001', '110101199001011234', 1, NOW(), '探亲', 1),
(4, '周九', '13900000002', '310101199002022345', 2, NOW(), '维修', 1);
