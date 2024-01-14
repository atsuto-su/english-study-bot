### Maven build stage
# Official image (JDK + Ubuntu22)
FROM eclipse-temurin:17-jdk-jammy

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

USER appuser

WORKDIR /home/appuser/app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# パッケージのダウンロードもイメージに含めてキャッシュ化
RUN ./mvnw dependency:go-offline

COPY src ./src

CMD ["./mvnw", "test"]

RUN echo 'test'