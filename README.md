# ブログシステム

Spring Boot + Doma + AngularJS で構築したJSON定義による画面自動生成ブログシステムです。

## 📋 機能

- **記事管理**: ブログ記事の作成・編集・削除・検索
- **コメント管理**: 記事へのコメント管理
- **カテゴリ管理**: 記事のカテゴリ分類
- **JSON定義による画面自動生成**: JSON定義ファイルを追加するだけで新しい管理画面を作成可能

## 🏗 技術スタック

### バックエンド
- **Spring Boot 3.2.5** - Javaアプリケーションフレームワーク
- **Doma 2.54.0** - 型安全なDBアクセスライブラリ（設定済み・将来的な活用予定）
- **JdbcTemplate** - 動的SQL実行（現在のデータアクセス層）
- **PostgreSQL** - 本番データベース
- **Spring Security** - 認証・権限管理

### フロントエンド
- **AngularJS 1.8.2** - JavaScriptフレームワーク
- **Bootstrap 3.4.1** - UIフレームワーク

## 🔍 データアクセス層の設計

現在のシステムは**JdbcTemplate**を使用した動的SQLで実装されています：

### 現在の実装（JdbcTemplate）
- JSON定義ファイルでテーブル名・カラム名を動的に指定
- コード変更なしで新しい画面を追加可能
- 柔軟性が高く、画面自動生成に最適

### 将来的なDoma活用
Domaは設定済みで、以下のような用途で活用予定：
- **特定テーブルの高度な検索機能**（全文検索、複雑な結合など）
- **バッチ処理**（大量データの一括処理）
- **パフォーマンス重視の処理**（型安全性とSQL最適化の両立）

例：ブログ記事の高度な検索機能を追加する場合
```java
@Dao
public interface BlogPostDao {
    @Select
    List<BlogPost> searchByKeyword(String keyword);
    
    @Select
    List<BlogPost> findPopularPosts(int limit);
}
```

**現在の動的実装（JdbcTemplate）と併用可能**なため、用途に応じて使い分けることができます。

## 📂 プロジェクト構造

```
ブログシステム/
├── src/
│   ├── main/
│   │   ├── java/com/example/app/
│   │   │   ├── Application.java                    # メインアプリケーション
│   │   │   ├── config/
│   │   │   │   ├── DomaConfig.java                 # Doma設定
│   │   │   │   ├── SecurityConfig.java             # セキュリティ設定
│   │   │   │   └── WebConfig.java                  # Web設定
│   │   │   ├── controller/
│   │   │   │   └── DynamicScreenController.java    # REST APIコントローラー
│   │   │   ├── service/
│   │   │   │   └── DynamicScreenService.java       # ビジネスロジック
│   │   │   ├── entity/
│   │   │   │   ├── BlogPost.java                   # 記事エンティティ
│   │   │   │   ├── BlogComment.java                # コメントエンティティ
│   │   │   │   └── BlogCategory.java               # カテゴリエンティティ
│   │   │   └── dto/
│   │   │       └── ScreenDefinition.java           # 画面定義DTO
│   │   └── resources/
│   │       ├── application.yml                      # アプリケーション設定
│   │       ├── db/
│   │       │   ├── schema.sql                       # テーブル定義
│   │       │   └── data.sql                         # 初期データ
│   │       ├── screens/                             # JSON画面定義
│   │       │   ├── blog.json                        # 記事管理画面定義
│   │       │   ├── comment.json                     # コメント管理画面定義
│   │       │   └── category.json                    # カテゴリ管理画面定義
│   │       └── static/
│   │           ├── app/
│   │           │   └── index.html                   # メインHTML
│   │           ├── scripts/
│   │           │   ├── app.js                       # AngularJSアプリ設定
│   │           │   ├── controllers/                 # コントローラー
│   │           │   │   ├── dynamicScreenController.js
│   │           │   │   ├── homeController.js
│   │           │   │   └── navigationController.js
│   │           │   └── filters/
│   │           │       └── range.js                 # カスタムフィルター
│   │           └── views/
│   │               ├── home.html                    # ホーム画面
│   │               └── dynamicScreen.html           # 動的画面テンプレート
├── build.gradle                                     # Gradleビルド設定
├── settings.gradle                                  # Gradleプロジェクト設定
└── README.md                                        # このファイル
```

## 🚀 セットアップと実行

### 前提条件
- **Java 17以上** がインストールされていること
- **Gradle** がインストールされていること（または同梱のGradle Wrapperを使用）

### 実行手順

#### 1. プロジェクトのビルド

```powershell
# Windowsの場合
.\gradlew build

# Linux/Macの場合
./gradlew build
```

#### 2. アプリケーションの起動

```powershell
# Windowsの場合
.\gradlew bootRun

# Linux/Macの場合
./gradlew bootRun
```

#### 3. ブラウザでアクセス

アプリケーション起動後、以下のURLにアクセスします：

```
http://localhost:8080
```

### デフォルト認証情報

API呼び出しには Basic認証が必要です（フロントエンドで自動設定済み）：

- **ユーザー名**: `admin`
- **パスワード**: `admin123`

## 📝 使い方

### 1. ホーム画面

起動直後に表示される画面です。各機能へのリンクが表示されます。

### 2. 記事管理

- **新規作成**: 「新規作成」ボタンをクリック
- **編集**: 一覧の「編集」ボタンをクリック
- **削除**: 一覧の「削除」ボタンをクリック
- **検索**: 検索フォームに条件を入力して「検索」ボタンをクリック

### 3. コメント管理

記事に対するコメントを管理できます。記事IDを指定してコメントを追加・編集・削除できます。

### 4. カテゴリ管理

記事のカテゴリを管理できます。

## 🔧 カスタマイズ

### 新しい画面を追加する方法

1. **データベーステーブルを作成**
   
   `src/main/resources/db/schema.sql` に新しいテーブルを追加

2. **JSON定義ファイルを作成**
   
   `src/main/resources/screens/` に新しいJSONファイルを作成
   
   例: `tag.json`
   ```json
   {
     "title": "タグ管理",
     "tableName": "blog_tag",
     "searchFields": [...],
     "listColumns": [...],
     "formFields": [...],
     "pagination": { "pageSize": 10 }
   }
   ```

3. **ナビゲーションに追加**
   
   `src/main/resources/static/app/index.html` のナビゲーションバーに新しいメニューを追加
   
   ```html
   <li><a href="#!/screen/tag">タグ管理</a></li>
   ```

4. **アプリケーション再起動**

これだけで新しい管理画面が自動生成されます！

## 🗄 データベース

### 現在の設定
本番環境では**PostgreSQL**を使用しています：
- **データベース名**: `blogdb`
- **ユーザー名**: `postgres`
- **ポート**: `5432`

### データベース設定の変更

`src/main/resources/application.yml` で設定を変更できます：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blogdb
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
```

### テーブル自動作成

アプリケーション起動時に以下が自動実行されます：
1. `db/schema.sql` - テーブル定義
2. `db/data.sql` - 初期データ投入

## 🔧 将来の拡張予定

### Domaを活用した機能追加例

現在はJdbcTemplateで動的にデータアクセスしていますが、以下のような機能を追加する際にDomaを活用できます：

#### 1. ブログ記事の全文検索
```java
@Dao
public interface BlogPostDao {
    @Select
    @Sql("SELECT * FROM blog_post WHERE to_tsvector('japanese', title || ' ' || content) @@ plainto_tsquery('japanese', /*keyword*/'') LIMIT /*limit*/10")
    List<BlogPost> fullTextSearch(String keyword, int limit);
}
```

#### 2. 人気記事ランキング
```java
@Dao
public interface BlogPostDao {
    @Select
    @Sql("""
        SELECT p.*, COUNT(c.id) as comment_count 
        FROM blog_post p 
        LEFT JOIN blog_comment c ON p.id = c.post_id 
        GROUP BY p.id 
        ORDER BY comment_count DESC 
        LIMIT /*limit*/10
        """)
    List<BlogPostWithStats> findPopularPosts(int limit);
}
```

#### 3. バッチ処理（記事の一括更新）
```java
@Dao
public interface BlogPostDao {
    @BatchUpdate
    int[] batchUpdate(List<BlogPost> posts);
}
```

### Domaのメリット
- **型安全性**: コンパイル時にSQLエラーを検出
- **パフォーマンス**: 最適化されたSQL実行
- **保守性**: SQLとJavaコードの明確な分離

これらの機能は現在のJdbcTemplate実装と**併用可能**で、必要に応じて段階的に追加できます。

## 🔐 セキュリティ

- Spring Securityによる認証機能を実装
- `/api/**` エンドポイントはBasic認証が必要
- 本番環境では `application.yml` のパスワードを変更してください

## 📦 ビルド成果物

```powershell
# JARファイルの作成
.\gradlew bootJar

# 成果物の場所
build/libs/blog-system-0.0.1-SNAPSHOT.jar
```

## 🐛 トラブルシューティング

### ポート8080が使用中の場合

`application.yml` の `server.port` を変更してください。

### データベースエラーが発生する場合

1. `blog.db` ファイルを削除
2. アプリケーションを再起動（自動的に再作成されます）

## 📄 ライセンス

このプロジェクトはサンプルコードです。自由に使用・改変してください。

## 🙏 謝辞

- Spring Boot
- Doma
- AngularJS
- Bootstrap

---

作成日: 2025年11月29日
