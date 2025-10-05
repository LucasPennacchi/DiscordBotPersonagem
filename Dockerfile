# Estágio 1: Build (Compilação)
# Usamos uma imagem oficial do Maven com Java 21 para compilar nosso projeto.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Estágio 2: Run (Execução)
# Usamos uma imagem oficial e leve do Java 21 apenas para rodar o bot.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia o .jar compilado do estágio de build para a imagem final.
COPY --from=build /app/target/rpg-discord-bot-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# Expõe a porta 8080 para o nosso servidor WebSocket.
EXPOSE 8080

# O comando para iniciar o bot quando o container ligar.
CMD ["java", "-jar", "app.jar"]