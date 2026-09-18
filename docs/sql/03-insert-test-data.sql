-- =============================================
-- MelodyTicket 测试数据
-- =============================================

-- ==================== 用户 ====================
USE melody_user;

-- 演示账号（test01 / admin），密码均为 Demo@2026!（BCrypt 加密）
-- 仅供本地体验，部署前请删除或重置密码
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `role`, `status`, `create_time`, `update_time`)
VALUES
('test01', '$2a$10$eOwCuginn/3vt6z6Dt9Gae.lOKWZ/fObSPO0szTMD7IJSPetrmF3q', '测试用户', '13800138001', 'USER', 1, NOW(), NOW()),
('admin', '$2a$10$eOwCuginn/3vt6z6Dt9Gae.lOKWZ/fObSPO0szTMD7IJSPetrmF3q', '管理员', '13800138000', 'ADMIN', 1, NOW(), NOW());

-- ==================== 演唱会 ====================
USE melody_concert;

INSERT INTO `concert` (`name`, `singer`, `venue`, `city`, `show_time`, `total_stock`, `remaining_stock`, `price`, `cover_url`, `status`, `create_time`, `update_time`)
VALUES
('周杰伦「嘉年华」世界巡回演唱会', '周杰伦', '国家体育场（鸟巢）', '北京', '2026-08-15 19:30:00', 50000, 43210, 1280.00, 'https://picsum.photos/400/200?music=1', 1, NOW(), NOW()),
('陈奕迅「FEAR AND DREAMS」演唱会', '陈奕迅', '上海梅赛德斯奔驰文化中心', '上海', '2026-09-20 20:00:00', 18000, 15200, 980.00, 'https://picsum.photos/400/200?music=2', 1, NOW(), NOW()),
('Taylor Swift「The Eras Tour」', 'Taylor Swift', '东京巨蛋', '东京', '2026-10-05 18:00:00', 55000, 32000, 2280.00, 'https://picsum.photos/400/200?music=3', 1, NOW(), NOW()),
('林俊杰「JJ20」世界巡回演唱会', '林俊杰', '广州天河体育中心', '广州', '2026-07-28 19:30:00', 30000, 28500, 880.00, 'https://picsum.photos/400/200?music=4', 1, NOW(), NOW()),
('五月天「好好好想见到你」演唱会', '五月天', '深圳大运中心体育场', '深圳', '2026-11-11 19:00:00', 40000, 39800, 680.00, 'https://picsum.photos/400/200?music=5', 1, NOW(), NOW()),
('邓紫棋「G.E.M.」世界巡回演唱会', '邓紫棋', '成都东安湖体育公园', '成都', '2026-12-24 20:00:00', 25000, 24800, 780.00, 'https://picsum.photos/400/200?music=6', 1, NOW(), NOW()),
('薛之谦「天外来物」巡回演唱会', '薛之谦', '武汉体育中心', '武汉', '2026-08-08 19:30:00', 22000, 18500, 580.00, 'https://picsum.photos/400/200?music=7', 1, NOW(), NOW()),
('张学友「60+」巡回演唱会', '张学友', '香港红磡体育馆', '香港', '2026-06-30 20:15:00', 12000, 8900, 1680.00, 'https://picsum.photos/400/200?music=8', 1, NOW(), NOW()),
('王力宏「最好的地方」演唱会', '王力宏', '杭州奥体中心', '杭州', '2026-10-20 19:30:00', 28000, 26000, 980.00, 'https://picsum.photos/400/200?music=9', 1, NOW(), NOW()),
('蔡依林「Ugly Beauty」世界巡演', '蔡依林', '长沙贺龙体育场', '长沙', '2026-11-15 20:00:00', 32000, 30500, 780.00, 'https://picsum.photos/400/200?music=10', 1, NOW(), NOW()),
('Beyond「海阔天空」纪念演唱会', 'Beyond', '南京奥林匹克体育中心', '南京', '2026-12-01 19:00:00', 35000, 34000, 880.00, 'https://picsum.photos/400/200?music=11', 1, NOW(), NOW()),
('Ed Sheeran「+-=÷×」Tour', 'Ed Sheeran', '首尔世界杯体育场', '首尔', '2026-09-10 18:30:00', 45000, 38000, 1980.00, 'https://picsum.photos/400/200?music=12', 1, NOW(), NOW()),
('李荣浩「纵横四海」巡回演唱会', '李荣浩', '重庆奥林匹克体育中心', '重庆', '2026-08-25 19:30:00', 20000, 18000, 680.00, 'https://picsum.photos/400/200?music=13', 1, NOW(), NOW()),
('Coldplay「Music of the Spheres」', 'Coldplay', '台北小巨蛋', '台北', '2026-11-28 19:00:00', 15000, 12000, 2580.00, 'https://picsum.photos/400/200?music=14', 1, NOW(), NOW()),
('毛不易「幼鸟指南」巡回演唱会', '毛不易', '天津奥林匹克中心', '天津', '2026-07-15 19:30:00', 18000, 16000, 580.00, 'https://picsum.photos/400/200?music=15', 1, NOW(), NOW());
