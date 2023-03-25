FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Dev環境用なのでソースコードをイメージに含める。可搬性は不要なのでボリュームマウントでもいい。
#（本番環境だったらビルドしたファイルだけ含めることも要検討）
COPY src ./src

CMD ["./mvnw", "spring-boot:run"]