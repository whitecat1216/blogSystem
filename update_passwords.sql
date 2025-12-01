-- 既存ユーザーのパスワードを平文に更新
UPDATE blog_user SET password = 'admin123' WHERE username = 'admin';
UPDATE blog_user SET password = 'user123' WHERE username = 'user';
