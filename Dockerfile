# Java 17 & Maven 3.8 ベースの公式イメージ
FROM eclipse-temurin:17-jdk

# 作業ディレクトリを設定
WORKDIR /app

# プロジェクトの全ファイルをコピー
COPY . .

# 権限がない場合でも Maven Wrapper を実行可能に
RUN chmod +x mvnw

# jarファイルをビルド
RUN ./mvnw clean package -DskipTests

# アプリを起動する
CMD ["java", "-jar", "target/livetask-0.0.2-SNAPSHOT.jar"]

