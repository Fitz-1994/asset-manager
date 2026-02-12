FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY . .
RUN ./mvnw -q package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建数据目录
RUN mkdir -p data

# 复制jar文件
COPY --from=builder /app/target/asset-manager-0.1.0.jar app.jar

# 暴露端口
EXPOSE 8000

# 运行
ENTRYPOINT ["java", "-jar", "app.jar"]
