CREATE DATABASE IF NOT EXISTS cms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'cms_user'@'localhost' IDENTIFIED BY 'cms_password';
GRANT ALL PRIVILEGES ON cms.* TO 'cms_user'@'localhost';
FLUSH PRIVILEGES;