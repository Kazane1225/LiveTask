FROM eclipse-temurin:17-jdk
WORKDIR /app

# ソースコードをコピーしてビルド
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests && \
    apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

# Railway では DATABASE_URL を環境変数でもらうので、
# DB の待機処理は不要。直接起動でOK。
CMD ["java", "-jar", "target/livetask-0.0.2-SNAPSHOT.jar"]
