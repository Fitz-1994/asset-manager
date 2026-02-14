FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY . .
RUN ./mvnw -q package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

# 创建数据目录
RUN mkdir -p data

# 复制jar文件
COPY --from=builder /app/target/asset-manager-0.1.0.jar app.jar

# 复制前端静态文件
COPY --from=builder /app/frontend/dist ./frontend/dist

# 暴露端口
EXPOSE 8000

# 运行
ENTRYPOINT ["java", "-jar", "app.jar"]
