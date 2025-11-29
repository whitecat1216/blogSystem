-- サンプルカテゴリ（既存データがある場合はスキップ）
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

-- サンプル記事（既存データがある場合はスキップ）
INSERT INTO blog_post (title, content, author_id, status) 
SELECT 'ブログシステムを構築しました', 'Spring BootとDomaを使ってブログシステムを作成しました。JSON定義で画面を自動生成できる仕組みです。', 1, 'published'
WHERE NOT EXISTS (SELECT 1 FROM blog_post WHERE title = 'ブログシステムを構築しました');

INSERT INTO blog_post (title, content, author_id, status) 
SELECT 'Domaの使い方', 'DomaはJavaの型安全なDBアクセスライブラリです。使いやすくて便利です。', 1, 'draft'
WHERE NOT EXISTS (SELECT 1 FROM blog_post WHERE title = 'Domaの使い方');

-- サンプルコメント（既存データがある場合はスキップ）
INSERT INTO blog_comment (post_id, author, comment_text) 
SELECT 1, 'ユーザーA', 'とても参考になりました！'
WHERE NOT EXISTS (SELECT 1 FROM blog_comment WHERE post_id = 1 AND author = 'ユーザーA');

INSERT INTO blog_comment (post_id, author, comment_text) 
SELECT 1, 'ユーザーB', '素晴らしい記事ですね。'
WHERE NOT EXISTS (SELECT 1 FROM blog_comment WHERE post_id = 1 AND author = 'ユーザーB');
