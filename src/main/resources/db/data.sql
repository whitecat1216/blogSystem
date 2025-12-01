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
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM blog_post WHERE title = 'ブログシステムを構築しました') THEN
        INSERT INTO blog_post (title, content, excerpt, hero_image, tags, author_id, status) 
        VALUES ('ブログシステムを構築しました', 'Spring BootとDomaを使ってブログシステムを作成しました。JSON定義で画面を自動生成できる仕組みです。', 'Spring BootとDomaで作った自作ブログシステムの紹介', '/uploads/sample/sample1.jpg', 'Java,Spring Boot', 1, 'published');
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM blog_post WHERE title = 'Domaの使い方') THEN
        INSERT INTO blog_post (title, content, excerpt, hero_image, tags, author_id, status) 
        VALUES ('Domaの使い方', 'DomaはJavaの型安全なDBアクセスライブラリです。使いやすくて便利です。', 'Domaの基本的な使い方を解説', '/uploads/sample/sample2.jpg', 'Java,Doma', 1, 'draft');
    END IF;
END $$;

-- サンプルコメント（記事が存在する場合のみ挿入）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM blog_post WHERE title = 'ブログシステムを構築しました') THEN
        INSERT INTO blog_comment (post_id, author, comment_text) 
        SELECT id, 'ユーザーA', 'とても参考になりました！'
        FROM blog_post 
        WHERE title = 'ブログシステムを構築しました'
        AND NOT EXISTS (SELECT 1 FROM blog_comment bc 
                       JOIN blog_post bp ON bc.post_id = bp.id 
                       WHERE bp.title = 'ブログシステムを構築しました' AND bc.author = 'ユーザーA');
        
        INSERT INTO blog_comment (post_id, author, comment_text) 
        SELECT id, 'ユーザーB', '素晴らしい記事ですね。'
        FROM blog_post 
        WHERE title = 'ブログシステムを構築しました'
        AND NOT EXISTS (SELECT 1 FROM blog_comment bc 
                       JOIN blog_post bp ON bc.post_id = bp.id 
                       WHERE bp.title = 'ブログシステムを構築しました' AND bc.author = 'ユーザーB');
    END IF;
END $$;
