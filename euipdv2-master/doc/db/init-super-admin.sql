-- =====================================================================
-- EUIPD 平台超级管理员初始化（在 euipd-schema.sql 之后执行，只执行一次）
--
-- 执行前设置三个会话变量，例如（source 须单独成行）：
--   mysql -uroot -p --default-character-set=utf8mb4 mycourse_bk <<'SQL'
--   SET @phone='登录手机号（11 位数字）';
--   SET @admin_name='平台管理员';
--   SET @plain_password='初始口令（8 至 32 位，同时含字母与数字）';
--   source doc/db/init-super-admin.sql
--   SQL
--
-- 变量未设置或不合法时，第一条插入因 login_name 为空而失败，事务不提交。
-- 平台超级机构的标识固定为 fm4ek0Zy（代码常量 Constants.SUPER_CUSTOMER_ID）。
-- 口令存储与登录协议一致：库内值 = md5(md5(明文 + 用户盐))，首次登录成功后自动升级为 BCrypt。
-- =====================================================================

SET NAMES utf8mb4;
START TRANSACTION;

SET @phone := CONVERT(IFNULL(@phone, '') USING utf8mb4);
SET @plain_password := CONVERT(IFNULL(@plain_password, '') USING utf8mb4);
SET @admin_name := IFNULL(NULLIF(CONVERT(IFNULL(@admin_name, '') USING utf8mb4), ''), '平台管理员');
SET @valid := @phone REGEXP '^[0-9]{11}$'
          AND CHAR_LENGTH(@plain_password) BETWEEN 8 AND 32
          AND @plain_password REGEXP '[A-Za-z]'
          AND @plain_password REGEXP '[0-9]';
SET @salt := SUBSTRING(MD5(RAND()), 1, 8);
SET @pwd := MD5(MD5(CONCAT(@plain_password, @salt)));
SET @customer_id := 'fm4ek0Zy';
SET @user_id := 'SU000001';
SET @customer_user_id := 'CU000001';
SET @dept_id := 'D0000000';
SET @now := NOW();

INSERT INTO rdms_user (
  id, login_name, password, salt, true_name, company_name,
  education_background, degree, description, create_time, deleted
) VALUES (
  @user_id, IF(@valid, @phone, NULL), @pwd, @salt, @admin_name, '平台超级用户',
  '', '', '平台超级管理员', @now, 0
);

INSERT INTO rdms_customer_role (id, name, `desc`)
VALUES ('00000001', '通用客户权限', '创建机构客户时赋予的默认权限');

INSERT INTO rdms_customer (
  id, login_name, password, customer_name, credit_code,
  reg_address, office_address, admin_name, contact_phone,
  license_link, status, login_token, login_ip, deleted,
  create_time, user_num_limit, expiration_time, budget_mode,
  customer_type, storage_space, en_auth_edit
) VALUES (
  @customer_id, '平台超级用户', @pwd, '平台超级用户', '000000000000000000',
  '', '', @admin_name, @phone,
  NULL, 'NORMAL', NULL, NULL, 0,
  @now, 2000, NULL, 'FREE',
  '', 10000, 1
);

INSERT INTO rdms_department (
  id, parent, depart_name, customer_id, manager_id, phone,
  depart_description, create_time, update_time, fixed, deleted
) VALUES (
  @dept_id, NULL, '平台总部', @customer_id, @customer_user_id, @phone,
  '代表平台的顶级部门', @now, @now, 1, 0
);

INSERT INTO rdms_customer_user (
  id, true_name, customer_id, department_id, job_num,
  job_position, title, job_level, join_time, office_tel,
  status, create_time, user_id, login_name, deleted
) VALUES (
  @customer_user_id, @admin_name, @customer_id, @dept_id, 'J0000000',
  '系统管理', '超级管理员', 'P9', @now, @phone,
  'NORMAL', @now, @user_id, @phone, 0
);

INSERT INTO rdms_boss (id, customer_id, boss_id, create_time, deleted)
VALUES ('BS000001', @customer_id, @customer_user_id, @now, 0);

INSERT INTO rdms_customer_role_customer (id, role_id, customer_id)
VALUES ('RC000001', '00000001', @customer_id);

-- 超级用户登录要求资源表非空；业务菜单由前端与机构界面策略决定
INSERT INTO rdms_customer_resource (id, name, page, request, parent) VALUES
('00', '欢迎', 'welcome', NULL, ''),
('51', '平台超级用户', NULL, NULL, ''),
('5101', '客户管理', 'system/customer', NULL, '51');

COMMIT;

SET @plain_password := NULL;
SELECT CONCAT('平台超级管理员已写入：登录手机号 ', @phone) AS result;
