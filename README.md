# タスク管理アプリ – LiveTask

JavaとJavaScriptを用いて開発した、シンプルなタスク管理アプリです。  
**フルスタック構成の構築からクラウドへのデプロイまでを一人で完結**させ、Webアプリ開発の流れを実践的に学ぶことができました。

---

## 🌐 公開URL

本アプリは Railway を使用してデプロイしています：  
👉 [https://livetask-production.up.railway.app](https://livetask-production.up.railway.app)

---

## 🔧 使用技術スタック

- **バックエンド**: Java / Spring Boot / Thymeleaf
- **フロントエンド**: JavaScript / HTML / CSS
- **データベース**: PostgreSQL
- **デプロイ**: Railway

---

## ▶️ ローカルでの実行方法

```bash
mvn spring-boot:run -D"spring-boot.run.profiles=dev"
※事前に application-dev.properties を設定する必要があります。
テンプレートファイル（application-dev.properties.sample）を参考に作成してください。

