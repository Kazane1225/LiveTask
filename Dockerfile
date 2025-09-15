FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests && \
    apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*
CMD sh -c 'until nc -z livetask-db 5432; do echo "waiting for db..."; sleep 1; done; \
           java -jar target/livetask-0.0.2-SNAPSHOT.jar'
