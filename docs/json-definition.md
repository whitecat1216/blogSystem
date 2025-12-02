# JSON 画面定義マニュアル

本システムは JSON による画面定義を読み取り、一覧・検索・編集フォーム・詳細表示を動的に生成します。ここでは `src/main/resources/screens/blog.json` の定義項目と拡張方法をまとめます。

## 基本構造
- **title**: 画面タイトル（例: "ブログ記事管理"）
- **tableName**: 対象テーブル名（例: `blog_post`）
- **searchFields**: 検索フォーム項目の定義配列
- **listColumns**: 管理用テーブル一覧のカラム定義配列
- **formFields**: 作成/編集フォームの項目定義配列
- **pagination**: ページング設定（例: `{ "pageSize": 10 }`）
- **listLayout**: 一覧の表示レイアウト（カード形式など）
- **detailLayout**: 記事詳細ページの表示レイアウト（タイトル/日付/画像/本文など）
- **sections**: ホーム画面用のセクション定義配列（最新記事表示など）

## searchFields（検索フォーム）
各要素は以下のプロパティを持ちます。
- **key**: 検索対象列名（例: `title`, `status`, `author_id`）
- **label**: ラベル表示（例: "タイトル"）
- **type**: `text` | `number` | `select`
- **placeholder**: プレースホルダ（任意）
- **options**: `type: select` の選択肢配列（例: `["draft","published"]`）

検索時のクエリパラメータは `key=value` として送られ、定義された `key` のみが WHERE 句に反映されます。

## listColumns（管理用一覧テーブル）
各要素は以下のプロパティを持ちます。
- **key**: 表示する列（DBカラム）
- **label**: 表示ラベル
- **sortable**: ソート可否（現状 UI は簡易対応）

## formFields（作成/編集フォーム）
各要素は以下のプロパティを持ちます。
- **key**: フィールド名（DBカラム/仮想フィールド）
- **label**: ラベル
- **type**: `text` | `number` | `textarea` | `select` | `file` | `multiselect` | `richtext`
- **required**: 必須かどうか
- **options**: `type: select` の静的選択肢配列
- **accept**: `type: file` の MIME フィルタ（例: `image/*`）
- **maxSizeMb**: アップロード許容最大サイズ（MB）
- **uploadEndpoint**: アップロード先 API（既定: `/api/upload`）
- **source**: `multiselect` の動的取得 API（例: `/api/tag/list`）
- **allowCreate**: `multiselect` で新規項目追加を許可（true/false）
- **autoFill**: コメントフォームなどで自動入力する値（例: `"username"`）
- **rows**: `textarea` の行数指定（例: `4`）

`type: file` は選択→即アップロード→返却 URL を該当キーへ格納します。`type: multiselect` は ID 配列（例: `[1,5,7]`）を保持し、バックエンドで中間テーブル（例: `post_tag`）へ反映します。`type: richtext` は Quill エディタで太字・見出し・リンク等を扱います。

### 抜粋 (excerpt) フィールドについて
`excerpt` は一覧やカード表示で記事本文すべてを出さず、要約・導入部分のみを表示するための任意フィールドです。SEO/クリック率向上・ページの視認性改善に有効ですが必須ではありません。未定義の場合は本文の先頭数十文字を自動生成するロジックを追加する運用も可能です。

## pagination（ページング）
- **pageSize**: 1ページ当たり表示件数（例: `10`）

## listLayout（一覧のカード表示）
一覧をカード式で表示したい場合に使用します。
- **type**: 現在 `"card"` をサポート
- **titleField**: タイトルに使用するフィールド（例: `"title"`）
- **dateField**: 投稿日に使用するフィールド（例: `"created_at"`）
- **excerptField**: 抜粋表示に使用するフィールド（例: `"excerpt"`）
- **imageField**: サムネイル画像フィールド（例: `"hero_image"`）
- **tagsField**: タグ表示フィールド（例: `"tags"`）

カードのタイトルは詳細ページにリンクします（`#!/post/:id`）。

## detailLayout（記事詳細の表示）
記事詳細ページでの表示項目と順序を定義します。
- **titleField**: タイトルに使用するフィールド
- **dateField**: 日付に使用するフィールド
- **imageField**: アイキャッチ画像フィールド
- **tagsField**: タグ表示フィールド
- **excerptField**: 抜粋表示フィールド
- **contentField**: 本文表示フィールド
- **order**: 表示順の配列（例: `["title","date","image","tags","excerpt","content"]`）
- **comments**: コメント機能の設定（任意）

`order` に含まれるキーに対応して、ビュー側で該当フィールドを順番にレンダリングします。

### comments（コメント機能）
記事詳細ページにコメント機能を追加する場合の設定です。
- **enabled**: コメント機能を有効にするか（true/false）
- **tableName**: コメントテーブル名（例: `"blog_comment"`）
- **foreignKey**: 外部キーカラム名（例: `"post_id"`）
- **authorField**: 投稿者フィールド名（例: `"author"`）
- **textField**: コメント本文フィールド名（例: `"comment_text"`）
- **dateField**: 投稿日時フィールド名（例: `"created_at"`）
- **allowAnonymous**: 匿名コメントを許可するか（true/false）
- **formFields**: コメント投稿フォームのフィールド定義配列

コメント投稿はログイン必須で、投稿者フィールドには自動的にログインユーザー名が入力されます。

## 例：blog.json の抜粋
```json
{
  "title": "ブログ記事管理",
  "tableName": "blog_post",
  "searchFields": [
    {"key":"title","label":"タイトル","type":"text","placeholder":"タイトルで検索"},
    {"key":"status","label":"公開状態","type":"select","options":["draft","published"]}
  ],
  "listColumns": [
    {"key":"id","label":"ID","sortable":true},
    {"key":"title","label":"タイトル","sortable":true}
  ],
  "formFields": [
    {"key":"title","label":"タイトル","type":"text","required":true},
    {"key":"content","label":"本文","type":"textarea","required":true},
    {"key":"excerpt","label":"抜粋","type":"textarea"},
    {"key":"hero_image","label":"アイキャッチ画像","type":"file","accept":"image/*","maxSizeMb":5,"uploadEndpoint":"/api/upload"},
    {"key":"tagIds","label":"タグ","type":"multiselect","source":"/api/tag/list","allowCreate":true},
    {"key":"status","label":"公開状態","type":"select","options":["draft","published"],"required":true}
  ],
  "pagination": {"pageSize": 10},
  "listLayout": {
    "type": "card",
    "titleField": "title",
    "dateField": "created_at",
    "excerptField": "excerpt",
    "imageField": "hero_image",
    "tagsField": "tags"  // 多対多選択されたタグ名をサーバ側でカンマ結合して保持
  },
  "detailLayout": {
    "titleField": "title",
    "dateField": "created_at",
    "imageField": "hero_image",
    "tagsField": "tags",
    "excerptField": "excerpt",
    "contentField": "content",
    "order": ["title","date","image","tags","excerpt","content"],
    "comments": {
      "enabled": true,
      "tableName": "blog_comment",
      "foreignKey": "post_id",
      "authorField": "author",
      "textField": "comment_text",
      "dateField": "created_at",
      "allowAnonymous": false,
      "formFields": [
        {"key":"author","label":"投稿者","type":"text","required":true,"autoFill":"username"},
        {"key":"comment_text","label":"コメント","type":"textarea","required":true,"rows":4}
      ]
    }
  }
}
```

## API 対応関係
- `GET /api/screen/{screen}/definition`: 画面定義取得
- `GET /api/screen/{screen}/data`: 一覧検索（`page`, `pageSize`, および `searchFields`）
- `GET /api/screen/{screen}/data/{id}`: 詳細取得
- `POST /api/screen/{screen}/data`: 作成（ログイン必須、コメント投稿含む）
- `PUT /api/screen/{screen}/data/{id}`: 更新（管理者）
- `DELETE /api/screen/{screen}/data/{id}`: 削除（管理者）
- `POST /api/upload`: 画像アップロード（返却: `{ "url": "/uploads/yyyyMMdd/uuid.ext" }`）
- `GET /api/auth/status`: 認証状態取得（`authenticated`, `username`, `isAdmin`）
- `POST /api/user/register`: 新規ユーザー登録
- `GET /api/tag/list`: タグ一覧取得（multiselect用）

### お問い合わせAPI
- `POST /api/contact/submit`: お問い合わせ送信（認証不要）
- `GET /api/contact`: お問い合わせ一覧取得（管理者のみ）
- `GET /api/contact/{id}`: お問い合わせ詳細取得（管理者のみ）
- `PUT /api/contact/{id}`: お問い合わせ更新（管理者のみ）
- `POST /api/contact/{id}/reply`: お問い合わせに返信（管理者のみ）
- `DELETE /api/contact/{id}`: お問い合わせ削除（管理者のみ）

## sections（ホーム画面用）
ホーム画面にさまざまなセクションを表示するための設定です。

### サポートされるセクションタイプ

#### 1. hero（ヒーローセクション/スライドショー）
大きな背景画像とテキストを表示するセクション。スライドショー機能も含みます。

```json
{
  "type": "hero",
  "style": "slideshow",
  "slides": [
    {
      "image": "https://example.com/image1.jpg",
      "title": "ようこそ私のブログへ",
      "subtitle": "技術とライフスタイルの記録",
      "buttonText": "記事を読む",
      "buttonLink": "#!/screen/blog",
      "overlay": {
        "enabled": true,
        "color": "#000000",
        "opacity": 0.4
      }
    }
  ],
  "autoplay": true,
  "interval": 5000,
  "height": "500px",
  "animation": "fade"
}
```

プロパティ:
- **style**: スライドショースタイル（`"slideshow"` または `"static"`）
- **slides**: スライドの配列
  - **image**: 背景画像URL
  - **title**: タイトルテキスト
  - **subtitle**: サブタイトルテキスト
  - **buttonText**: ボタンテキスト（任意）
  - **buttonLink**: ボタンリンク先（任意）
  - **overlay**: オーバーレイ設定
    - **enabled**: オーバーレイを有効にするか
    - **color**: オーバーレイの色（HEX）
    - **opacity**: 不透明度（0.0〜1.0）
- **autoplay**: 自動再生するか
- **interval**: スライド切り替え間隔（ミリ秒）
- **height**: セクションの高さ（CSS値）
- **animation**: アニメーション効果（現在は `"fade"` のみ）

#### 2. text-block（テキストブロック）
テキストコンテンツを表示するセクション。

```json
{
  "type": "text-block",
  "heading": "About This Blog",
  "content": "日々の学びや経験を共有しています。",
  "alignment": "center",
  "backgroundColor": "#ffffff",
  "padding": "60px 20px"
}
```

プロパティ:
- **heading**: 見出しテキスト
- **content**: 本文テキスト（HTML可）
- **alignment**: テキスト配置（`"left"`, `"center"`, `"right"`）
- **backgroundColor**: 背景色（HEX）
- **padding**: パディング（CSS値）

#### 3. stats（統計情報）
記事数、コメント数などの統計を表示するセクション。

```json
{
  "type": "stats",
  "backgroundColor": "#f8f9fa",
  "padding": "40px 20px",
  "items": [
    {
      "label": "記事",
      "sourceTable": "blog_post",
      "countWhere": "status = 'published'",
      "icon": "glyphicon-file",
      "color": "#5bc0de"
    }
  ],
  "layout": "horizontal"
}
```

プロパティ:
- **items**: 統計アイテムの配列
  - **label**: ラベル
  - **sourceTable**: カウント対象テーブル
  - **countWhere**: WHERE条件（任意）
  - **icon**: アイコンクラス（Bootstrap Glyphicons）
  - **color**: アイコンの色（HEX）
- **layout**: レイアウト（`"horizontal"` または `"vertical"`）
- **backgroundColor**: 背景色
- **padding**: パディング

#### 4. recent-posts（最新記事）
最新記事を一覧表示するセクション。グリッドまたはリストレイアウトをサポート。

```json
{
  "type": "recent-posts",
  "heading": "最新記事",
  "sourceTable": "blog_post",
  "limit": 6,
  "orderBy": "created_at",
  "orderDirection": "DESC",
  "whereClause": "status = 'published'",
  "displayFields": {
    "titleField": "title",
    "dateField": "created_at",
    "excerptField": "excerpt",
    "imageField": "hero_image",
    "tagsField": "tags"
  },
  "linkPattern": "#!/screen/blog/detail/{{id}}",
  "layout": "grid",
  "columns": 3
}
```

プロパティ:
- **heading**: セクション見出し
- **sourceTable**: データソーステーブル
- **limit**: 表示件数
- **orderBy**: ソート基準カラム
- **orderDirection**: ソート方向（`"DESC"` または `"ASC"`）
- **whereClause**: WHERE条件
- **displayFields**: 表示フィールド設定
  - **titleField**: タイトルフィールド
  - **dateField**: 日付フィールド
  - **excerptField**: 抜粋フィールド
  - **imageField**: 画像フィールド
  - **tagsField**: タグフィールド
- **linkPattern**: 詳細ページへのリンクパターン（`{{id}}` を含む）
- **layout**: レイアウト（`"grid"` または `"list"`）
- **columns**: グリッドのカラム数（1〜4）

#### 5. category-grid（カテゴリグリッド）
カテゴリをカード形式で表示するセクション。

```json
{
  "type": "category-grid",
  "heading": "カテゴリから探す",
  "sourceTable": "blog_category",
  "displayFields": {
    "nameField": "name",
    "descriptionField": "description"
  },
  "columns": 3,
  "linkPattern": "#!/screen/blog?category={{id}}",
  "backgroundColor": "#ffffff",
  "padding": "40px 20px"
}
```

プロパティ:
- **heading**: セクション見出し
- **sourceTable**: データソーステーブル
- **displayFields**: 表示フィールド設定
  - **nameField**: 名前フィールド
  - **descriptionField**: 説明フィールド
- **columns**: カラム数
- **linkPattern**: リンクパターン
- **backgroundColor**: 背景色
- **padding**: パディング

#### 6. custom-html（カスタムHTML）
任意のHTMLコンテンツを挿入するセクション。

```json
{
  "type": "custom-html",
  "content": "<div class='custom-widget'>任意のHTML</div>"
}
```

プロパティ:
- **content**: HTML文字列

### 例：home.json（完全版）
### 例：home.json（完全版）
```json
{
  "title": "ホーム",
  "sections": [
    {
      "type": "hero",
      "style": "slideshow",
      "slides": [
        {
          "image": "https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1920&h=600&fit=crop",
          "title": "ようこそ私のブログへ",
          "subtitle": "技術とライフスタイルの記録",
          "buttonText": "記事を読む",
          "buttonLink": "#!/screen/blog",
          "overlay": {
            "enabled": true,
            "color": "#000000",
            "opacity": 0.4
          }
        },
        {
          "image": "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1920&h=600&fit=crop",
          "title": "最新技術を学ぶ",
          "subtitle": "プログラミングの世界へ",
          "buttonText": "詳しく見る",
          "buttonLink": "#!/screen/blog",
          "overlay": {
            "enabled": true,
            "color": "#1e3c72",
            "opacity": 0.5
          }
        }
      ],
      "autoplay": true,
      "interval": 5000,
      "height": "500px",
      "animation": "fade"
    },
    {
      "type": "text-block",
      "heading": "About This Blog",
      "content": "日々の学びや経験を共有しています。プログラミング、デザイン、ライフハックなど幅広いトピックをカバーしています。",
      "alignment": "center",
      "backgroundColor": "#ffffff",
      "padding": "60px 20px"
    },
    {
      "type": "stats",
      "backgroundColor": "#f8f9fa",
      "padding": "40px 20px",
      "items": [
        {
          "label": "記事",
          "sourceTable": "blog_post",
          "countWhere": "status = 'published'",
          "icon": "glyphicon-file",
          "color": "#5bc0de"
        },
        {
          "label": "カテゴリ",
          "sourceTable": "blog_category",
          "icon": "glyphicon-folder-open",
          "color": "#5cb85c"
        },
        {
          "label": "コメント",
          "sourceTable": "blog_comment",
          "icon": "glyphicon-comment",
          "color": "#f0ad4e"
        }
      ],
      "layout": "horizontal"
    },
    {
      "type": "recent-posts",
      "heading": "最新記事",
      "sourceTable": "blog_post",
      "limit": 6,
      "orderBy": "created_at",
      "orderDirection": "DESC",
      "whereClause": "status = 'published'",
      "displayFields": {
        "titleField": "title",
        "dateField": "created_at",
        "excerptField": "excerpt",
        "imageField": "hero_image",
        "tagsField": "tags"
      },
      "linkPattern": "#!/screen/blog/detail/{{id}}",
      "layout": "grid",
      "columns": 3
    },
    {
      "type": "category-grid",
      "heading": "カテゴリから探す",
      "sourceTable": "blog_category",
      "displayFields": {
        "nameField": "name",
        "descriptionField": "description"
      },
      "columns": 3,
      "linkPattern": "#!/screen/blog?category={{id}}",
      "backgroundColor": "#ffffff",
      "padding": "40px 20px"
    }
  ]
}
```

## ホーム画面編集システム

### 概要
ホーム画面（`home.json`）をGUI経由で編集・プレビュー・保存できる機能を提供します。

### 編集方法

#### 方法1: 直接ファイル編集
`src/main/resources/screens/home.json` を直接編集してサーバーを再起動します。

#### 方法2: 管理画面での編集
1. 管理者としてログイン
2. ナビゲーションバーの「設定」→「ホーム画面編集」をクリック
3. ビジュアルエディタまたはJSON直接編集モードで編集
4. プレビューで確認
5. 保存（自動でバージョン管理）

### ホーム画面編集API

#### GET /api/home/config
現在のホーム画面設定を取得します。

レスポンス:
```json
{
  "id": 1,
  "config_json": "{...}",
  "version": 3,
  "updated_at": "2025-12-02T12:34:56",
  "updated_by": "admin"
}
```

#### PUT /api/home/config
ホーム画面設定を更新します（管理者のみ）。

リクエスト:
```json
{
  "config_json": "{\"title\":\"ホーム\",\"sections\":[...]}"
}
```

レスポンス:
```json
{
  "success": true,
  "version": 4,
  "message": "ホーム画面設定を更新しました"
}
```

#### GET /api/home/config/history
設定変更履歴を取得します（管理者のみ）。

レスポンス:
```json
{
  "records": [
    {
      "id": 3,
      "version": 3,
      "config_json": "{...}",
      "updated_at": "2025-12-02T12:34:56",
      "updated_by": "admin"
    }
  ]
}
```

#### POST /api/home/config/restore/{version}
指定バージョンの設定に復元します（管理者のみ）。

### ビジュアルエディタ機能

#### セクションの追加
1. 「セクションを追加」ボタンをクリック
2. セクションタイプを選択（hero, text-block, stats, recent-posts, category-grid, custom-html）
3. 各フィールドを入力
4. 「追加」をクリック

#### セクションの並び替え
- セクションをドラッグ&ドロップで移動
- 上下矢印ボタンで順序を変更

#### セクションの編集
- 各セクションの「編集」ボタンをクリック
- フィールドを変更
- 「保存」をクリック

#### セクションの削除
- 各セクションの「削除」ボタンをクリック
- 確認ダイアログで「OK」

### プレビュー機能
- 「プレビュー」ボタンをクリック
- モーダルウィンドウで実際のレンダリング結果を確認
- プレビュー中も編集可能（リアルタイム反映）

### バージョン管理
- すべての変更は自動的にバージョン管理される
- 変更履歴から過去のバージョンを確認可能
- 任意のバージョンに復元可能
- 変更者とタイムスタンプが記録される

### データベーススキーマ

```sql
CREATE TABLE IF NOT EXISTS home_config (
    id SERIAL PRIMARY KEY,
    config_json TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS home_config_history (
    id SERIAL PRIMARY KEY,
    config_id INTEGER NOT NULL,
    version INTEGER NOT NULL,
    config_json TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    FOREIGN KEY (config_id) REFERENCES home_config(id) ON DELETE CASCADE
);
```

### セキュリティ
- ホーム画面設定の編集は管理者（ADMIN）のみ可能
- すべての操作はログに記録
- JSON妥当性チェックを実行
- XSS対策としてHTMLコンテンツをサニタイズ

## 権限
- **一般ユーザー（USER）**: 
  - 記事の閲覧（GET）
  - コメントの投稿（POST /api/screen/comment/data）
  - 記事管理メニューのみ表示
- **管理者（ADMIN）**: 
  - 記事の作成/編集/削除（POST/PUT/DELETE）
  - コメント管理
  - カテゴリ管理
  - タグ管理
  - すべてのメニュー表示

メニュー表示制御は `index.html` で `ng-if="isAdmin"` により実装されています。

## お問い合わせ機能

### 概要
ユーザーがWebフォームから問い合わせを送信し、管理者が返信できる機能です。管理者の返信はメールで送信されます。

### データベーススキーマ
```sql
CREATE TABLE IF NOT EXISTS contact_message (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'new',
    reply TEXT,
    replied_at TIMESTAMP,
    replied_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### ステータス
- **new**: 新規（未対応）
- **replied**: 返信済み
- **archived**: アーカイブ済み

### 公開フォーム
`/contact.html` で誰でもアクセス可能な問い合わせフォームを提供します。

フォーム項目:
- お名前（必須）
- メールアドレス（必須）
- 件名（必須）
- お問い合わせ内容（必須）

### 管理画面
管理者は `#!/screen/contact` でお問い合わせを管理できます。

機能:
- お問い合わせ一覧表示
- ステータス・メールアドレスでの検索
- 詳細表示
- 返信内容の入力とメール送信
- ステータス更新
- 削除

### メール送信設定

#### application.yml設定例
```yaml
spring:
  mail:
    enabled: true  # メール送信を有効化
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    from: noreply@blog.com
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

#### 開発環境での動作
`spring.mail.enabled` が `false` の場合、実際のメール送信は行わず、ログ出力のみ行います。

### contact.json定義例
```json
{
  "title": "お問い合わせ管理",
  "tableName": "contact_message",
  "searchFields": [
    {
      "key": "status",
      "label": "ステータス",
      "type": "select",
      "options": ["new", "replied", "archived"]
    },
    {
      "key": "email",
      "label": "メールアドレス",
      "type": "text"
    }
  ],
  "listColumns": [
    {"key": "id", "label": "ID", "sortable": true},
    {"key": "name", "label": "名前", "sortable": true},
    {"key": "email", "label": "メールアドレス"},
    {"key": "subject", "label": "件名", "sortable": true},
    {"key": "status", "label": "ステータス", "sortable": true},
    {"key": "created_at", "label": "受信日時", "sortable": true}
  ],
  "formFields": [
    {"key": "name", "label": "名前", "type": "text", "required": true},
    {"key": "email", "label": "メールアドレス", "type": "text", "required": true},
    {"key": "subject", "label": "件名", "type": "text", "required": true},
    {"key": "message", "label": "お問い合わせ内容", "type": "textarea", "rows": 8, "required": true},
    {"key": "status", "label": "ステータス", "type": "select", "options": ["new", "replied", "archived"], "required": true},
    {"key": "reply", "label": "返信内容", "type": "textarea", "rows": 8}
  ],
  "pagination": {"pageSize": 20}
}
```

### セキュリティ
- 公開フォーム（`/contact.html`, `/api/contact/submit`）は認証不要
- 管理機能（一覧・返信・削除）は管理者（ADMIN）のみアクセス可能
- メール送信時は送信者情報とタイムスタンプを記録

### 実装ファイル
- `ContactController.java`: REST APIエンドポイント
- `ContactService.java`: ビジネスロジック（CRUD、返信処理）
- `ContactMessage.java`: エンティティ
- `EmailService.java`: メール送信サービス
- `contact.html`: 公開フォームページ
- `contact.json`: 管理画面定義

## 運用メモ
- レイアウトや表示順の変更は `listLayout`/`detailLayout` を編集すれば反映されます。
- 新しい DB カラムを追加する場合は、`schema.sql` と `formFields` の整合を取ってください。
- 画像は `static/uploads` 配下に保存され、`/uploads/...` で配信されます。
- コメント機能を追加するには `detailLayout.comments` を設定します。
- ホーム画面の表示内容は `home.json` の `sections` で制御します。
- リッチテキストエディタ（Quill）は太字、見出し（h1/h2/h3）、リスト、リンク等をサポートします。
- HTML出力はjsoupでサニタイズされ、安全なタグのみが保存されます。
- お問い合わせ機能のメール送信は `application.yml` で設定が必要です（開発時は無効化可能）。
