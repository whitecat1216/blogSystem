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

`type: file` は選択→即アップロード→返却 URL を該当キーへ格納します。`type: multiselect` は ID 配列（例: `[1,5,7]`）を保持し、バックエンドで中間テーブル（例: `post_tag`）へ反映します。`type: richtext` は太字・リンク等を扱う拡張用で、現状はプレーン textarea 実装（将来 Quill 等へ差し替え）。

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

`order` に含まれるキーに対応して、ビュー側で該当フィールドを順番にレンダリングします。

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
    "order": ["title","date","image","tags","excerpt","content"]
  }
}
```

## API 対応関係
- `GET /api/screen/{screen}/definition`: 画面定義取得
- `GET /api/screen/{screen}/data`: 一覧検索（`page`, `pageSize`, および `searchFields`）
- `GET /api/screen/{screen}/data/{id}`: 詳細取得
- `POST /api/screen/{screen}/data`: 作成（管理者）
- `PUT /api/screen/{screen}/data/{id}`: 更新（管理者）
- `DELETE /api/screen/{screen}/data/{id}`: 削除（管理者）
- `POST /api/upload`: 画像アップロード（返却: `{ "url": "/uploads/yyyyMMdd/uuid.ext" }`）

## 権限
- 一般ユーザ/未ログイン: GET 可、管理ボタンは非表示
- 管理者: 作成/編集/削除が可能

## 運用メモ
- レイアウトや表示順の変更は `listLayout`/`detailLayout` を編集すれば反映されます。
- 新しい DB カラムを追加する場合は、`schema.sql` と `formFields` の整合を取ってください。
- 画像は `static/uploads` 配下に保存され、`/uploads/...` で配信されます。
