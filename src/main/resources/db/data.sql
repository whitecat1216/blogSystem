-- デフォルトユーザー(既存データがある場合はスキップ)
-- パスワードは平文保存: admin123 / user123
INSERT INTO blog_user (username, password, role, enabled)
SELECT 'admin', 'admin123', 'ADMIN', true
WHERE NOT EXISTS (SELECT 1 FROM blog_user WHERE username = 'admin');

INSERT INTO blog_user (username, password, role, enabled)
SELECT 'user', 'user123', 'USER', true
WHERE NOT EXISTS (SELECT 1 FROM blog_user WHERE username = 'user');

-- サンプルカテゴリ(既存データがある場合はスキップ)
INSERT INTO blog_category (name, description) 
SELECT '技術', 'プログラミングや技術関連の記事'
WHERE NOT EXISTS (SELECT 1 FROM blog_category WHERE name = '技術');

INSERT INTO blog_category (name, description) 
SELECT '日記', '日常の出来事や雑記'
WHERE NOT EXISTS (SELECT 1 FROM blog_category WHERE name = '日記');

INSERT INTO blog_category (name, description) 
SELECT 'レビュー', '製品やサービスのレビュー'
WHERE NOT EXISTS (SELECT 1 FROM blog_category WHERE name = 'レビュー');

-- サンプルタグ（既存データがある場合はスキップ）
INSERT INTO blog_tag (name) 
SELECT 'Java'
WHERE NOT EXISTS (SELECT 1 FROM blog_tag WHERE name = 'Java');

INSERT INTO blog_tag (name) 
SELECT 'Spring Boot'
WHERE NOT EXISTS (SELECT 1 FROM blog_tag WHERE name = 'Spring Boot');

INSERT INTO blog_tag (name) 
SELECT 'AngularJS'
WHERE NOT EXISTS (SELECT 1 FROM blog_tag WHERE name = 'AngularJS');

INSERT INTO blog_tag (name) 
SELECT 'データベース'
WHERE NOT EXISTS (SELECT 1 FROM blog_tag WHERE name = 'データベース');

-- シンプルな条件付きINSERT（PL/pgSQLブロックを避け、文字化け/終端エラー回避）
INSERT INTO blog_post (title, content, excerpt, hero_image, tags, author_id, status)
SELECT 'ブログシステムを構築しました',
       'Spring BootとDomaを使ってブログシステムを作成しました。JSON定義で画面を自動生成できる仕組みです。',
       'Spring BootとDomaで作った自作ブログシステムの紹介',
       '/uploads/sample/sample1.jpg',
       'Java,Spring Boot',
       1,
       'published'
WHERE NOT EXISTS (SELECT 1 FROM blog_post WHERE title = 'ブログシステムを構築しました');

INSERT INTO blog_post (title, content, excerpt, hero_image, tags, author_id, status)
SELECT 'Domaの使い方',
       'DomaはJavaの型安全なDBアクセスライブラリです。使いやすくて便利です。',
       'Domaの基本的な使い方を解説',
       '/uploads/sample/sample2.jpg',
       'Java,Doma',
       1,
       'draft'
WHERE NOT EXISTS (SELECT 1 FROM blog_post WHERE title = 'Domaの使い方');

INSERT INTO blog_comment (post_id, author, comment_text)
SELECT bp.id, 'ユーザーA', 'とても参考になりました！'
FROM blog_post bp
WHERE bp.title = 'ブログシステムを構築しました'
    AND NOT EXISTS (
        SELECT 1 FROM blog_comment bc JOIN blog_post bpp ON bc.post_id = bpp.id
        WHERE bpp.title = 'ブログシステムを構築しました' AND bc.author = 'ユーザーA'
    );

INSERT INTO blog_comment (post_id, author, comment_text)
SELECT bp.id, 'ユーザーB', '素晴らしい記事ですね。'
FROM blog_post bp
WHERE bp.title = 'ブログシステムを構築しました'
    AND NOT EXISTS (
        SELECT 1 FROM blog_comment bc JOIN blog_post bpp ON bc.post_id = bpp.id
        WHERE bpp.title = 'ブログシステムを構築しました' AND bc.author = 'ユーザーB'
    );
