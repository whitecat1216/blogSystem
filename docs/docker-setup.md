# Docker セットアップと起動手順

本ドキュメントでは、Dockerを使用したブログシステムのセットアップと起動手順を説明します。

## 📋 前提条件

以下のソフトウェアがインストールされていることを確認してください：

- **Docker Desktop**: 20.10以上
  - Windows: [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
  - Mac: [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop)
  - Linux: Docker Engine + Docker Compose

### Dockerのインストール確認

```powershell
# Dockerバージョン確認
docker --version

# Docker Composeバージョン確認
docker compose version
```

## 🚀 クイックスタート

### 1. リポジトリのクローン

```powershell
git clone https://github.com/whitecat1216/blogSystem.git
cd blogSystem/app
```

### 2. 環境変数の設定（オプション）

デフォルト設定で問題ない場合はスキップ可能です。カスタマイズする場合：

```powershell
# .env.exampleをコピーして.envファイルを作成
Copy-Item .env.example .env

# .envファイルを編集（テキストエディタで開く）
notepad .env
```

### 3. Dockerコンテナの起動

```powershell
# コンテナをビルドして起動
docker compose up -d

# ログを確認（オプション）
docker compose logs -f
```

### 4. アプリケーションへのアクセス

ブラウザで以下のURLにアクセス：

```
http://localhost:8080
```

デフォルトのログイン情報：
- **ユーザー名**: `admin`
- **パスワード**: `admin123`

## 📝 詳細な起動手順

### ステップ1: プロジェクトディレクトリへ移動

```powershell
cd c:\Users\Yuuki\Documents\3.個人用\ブログシステム\app
```

### ステップ2: Docker Composeで起動

```powershell
# バックグラウンドで起動
docker compose up -d

# または、フォアグラウンドで起動（ログをリアルタイム表示）
docker compose up
```

起動完了までの目安時間：
- 初回起動（ビルド含む）: 3-5分
- 2回目以降: 30秒-1分

### ステップ3: 起動状態の確認

```powershell
# コンテナの状態を確認
docker compose ps

# 期待される出力例:
# NAME            IMAGE              STATUS         PORTS
# blog-app        app:latest         Up 2 minutes   0.0.0.0:8080->8080/tcp
# blog-postgres   postgres:16-alpine Up 2 minutes   0.0.0.0:5432->5432/tcp
```

### ステップ4: ヘルスチェック

```powershell
# アプリケーションのヘルスチェック（Windows）
Invoke-WebRequest -Uri http://localhost:8080 -UseBasicParsing

# または、ブラウザでアクセス
Start-Process http://localhost:8080
```

## 🛠 Docker コマンド一覧

### コンテナ管理

```powershell
# 起動
docker compose up -d

# 停止
docker compose stop

# 再起動
docker compose restart

# 停止してコンテナを削除
docker compose down

# コンテナ、ボリューム、イメージをすべて削除
docker compose down -v --rmi all

# 特定のサービスのみ再起動
docker compose restart app
```

### ログ確認

```powershell
# すべてのサービスのログを表示
docker compose logs

# リアルタイムでログを表示
docker compose logs -f

# 特定のサービスのログのみ表示
docker compose logs app
docker compose logs postgres

# 最新100行のみ表示
docker compose logs --tail=100 app
```

### コンテナ内での作業

```powershell
# アプリケーションコンテナに接続
docker compose exec app sh

# PostgreSQLコンテナに接続
docker compose exec postgres psql -U postgres -d blogdb

# データベースバックアップ
docker compose exec postgres pg_dump -U postgres blogdb > backup.sql
```

### ビルド・再ビルド

```powershell
# イメージを再ビルド
docker compose build

# キャッシュを使わずに再ビルド
docker compose build --no-cache

# 再ビルドして起動
docker compose up -d --build
```

## 🔧 環境設定

### データベース設定

`docker-compose.yml` でPostgreSQLの設定を変更できます：

```yaml
postgres:
  environment:
    POSTGRES_DB: blogdb          # データベース名
    POSTGRES_USER: postgres      # ユーザー名
    POSTGRES_PASSWORD: postgres  # パスワード
```

### アプリケーション設定

`docker-compose.yml` のアプリケーション環境変数：

```yaml
app:
  environment:
    # データベース接続
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/blogdb
    SPRING_DATASOURCE_USERNAME: postgres
    SPRING_DATASOURCE_PASSWORD: postgres
    
    # 管理者アカウント（本番環境では必ず変更）
    SPRING_SECURITY_USER_NAME: admin
    SPRING_SECURITY_USER_PASSWORD: admin123
    
    # メール設定
    SPRING_MAIL_ENABLED: "true"
    SPRING_MAIL_HOST: smtp.gmail.com
    SPRING_MAIL_PORT: 587
    SPRING_MAIL_USERNAME: your-email@gmail.com
    SPRING_MAIL_PASSWORD: your-app-password
```

### ポート変更

デフォルトのポート8080を変更する場合：

```yaml
app:
  ports:
    - "9090:8080"  # ホスト側のポート:コンテナ側のポート
```

この場合、`http://localhost:9090` でアクセスします。

## 💾 データ永続化

### ボリュームの確認

```powershell
# ボリューム一覧を表示
docker volume ls

# ボリュームの詳細情報
docker volume inspect app_postgres_data
docker volume inspect app_uploads_data
```

### データのバックアップ

```powershell
# データベースのバックアップ
docker compose exec postgres pg_dump -U postgres blogdb > backup_$(Get-Date -Format "yyyyMMdd_HHmmss").sql

# アップロードファイルのバックアップ（Windows）
docker compose cp app:/app/uploads ./uploads_backup
```

### データの復元

```powershell
# データベースの復元
Get-Content backup.sql | docker compose exec -T postgres psql -U postgres -d blogdb

# アップロードファイルの復元
docker compose cp ./uploads_backup/. app:/app/uploads
```

## 🐛 トラブルシューティング

### ポートが既に使用されている

```powershell
# ポート8080を使用しているプロセスを確認
netstat -ano | findstr :8080

# プロセスを終了（PIDを指定）
Stop-Process -Id <PID> -Force
```

### コンテナが起動しない

```powershell
# ログを確認
docker compose logs app

# コンテナを削除して再作成
docker compose down
docker compose up -d --force-recreate
```

### データベース接続エラー

```powershell
# PostgreSQLコンテナの状態確認
docker compose ps postgres

# PostgreSQLのログ確認
docker compose logs postgres

# データベースコンテナを再起動
docker compose restart postgres

# 接続テスト
docker compose exec postgres psql -U postgres -d blogdb -c "SELECT version();"
```

### ディスク容量不足

```powershell
# 未使用のDockerリソースをクリーンアップ
docker system prune -a

# ボリュームも含めてクリーンアップ（注意：データが削除されます）
docker system prune -a --volumes
```

### アプリケーションが応答しない

```powershell
# コンテナのリソース使用状況を確認
docker stats

# アプリケーションコンテナを再起動
docker compose restart app

# メモリ不足の場合、JVMヒープサイズを調整
# docker-compose.ymlのJAVA_OPTSを編集：
# JAVA_OPTS: "-Xmx1024m -Xms512m"
```

## 🔒 セキュリティ

### 本番環境での推奨設定

1. **管理者パスワードの変更**

```yaml
SPRING_SECURITY_USER_NAME: your_admin_username
SPRING_SECURITY_USER_PASSWORD: strong_password_here
```

2. **データベースパスワードの変更**

```yaml
POSTGRES_PASSWORD: strong_db_password_here
```

3. **環境変数ファイルの使用**

`.env` ファイルを使用して機密情報を管理：

```powershell
# .envファイルを作成
Copy-Item .env.example .env

# .envファイルを編集
notepad .env

# .envファイルをGit管理から除外（既に.gitignoreに含まれています）
```

4. **HTTPSの有効化**

リバースプロキシ（nginx、Caddy等）を使用してHTTPSを設定してください。

## 📊 モニタリング

### リソース使用状況の確認

```powershell
# リアルタイムでリソース使用状況を表示
docker stats

# 特定のコンテナのみ表示
docker stats blog-app blog-postgres
```

### ログの監視

```powershell
# アプリケーションログをリアルタイム監視
docker compose logs -f app

# エラーログのみフィルタリング
docker compose logs app | Select-String "ERROR"
```

## 🚢 本番環境へのデプロイ

### Docker Swarmを使用する場合

```powershell
# Swarmモードを初期化
docker swarm init

# スタックをデプロイ
docker stack deploy -c docker-compose.yml blog-system

# スタックの状態確認
docker stack ps blog-system

# スタックを削除
docker stack rm blog-system
```

### Kubernetesを使用する場合

Kompose を使用してKubernetesマニフェストを生成：

```powershell
# Komposeをインストール（chocolateyを使用）
choco install kubernetes-kompose

# Kubernetesマニフェストを生成
kompose convert -f docker-compose.yml
```

## 📚 参考リンク

- [Docker公式ドキュメント](https://docs.docker.com/)
- [Docker Compose公式ドキュメント](https://docs.docker.com/compose/)
- [PostgreSQL Dockerイメージ](https://hub.docker.com/_/postgres)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)

## 🆘 サポート

問題が解決しない場合：

1. [GitHubリポジトリのIssues](https://github.com/whitecat1216/blogSystem/issues)で報告
2. ログファイルを添付して詳細を共有
3. 実行環境（OS、Dockerバージョン等）を明記

---

最終更新日: 2025年12月2日
