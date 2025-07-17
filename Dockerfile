# Temel imaj olarak OpenJDK'nın son LTS sürümünü kullanın
FROM openjdk:17-jdk-slim

# Uygulamanın JAR dosyasını kopyalayacağımız çalışma dizinini belirleyin
WORKDIR /app

# Maven wrapper dosyalarını kopyala
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Bağımlılıkları önceden indirmek için build et (sadece bağımlılıklar değiştiğinde tekrar çalışır)
RUN ./mvnw dependency:go-offline -B

# Proje kaynak kodunu kopyala
COPY src ./src

# Uygulamayı derle ve JAR dosyasını oluştur
RUN ./mvnw clean install -DskipTests

# Oluşturulan JAR dosyasını kopyala (hedef dizininizdeki adla eşleşmeli)
# Genellikle 'target' altında projectname-version.jar şeklinde olur
# Bu örnekte, 'app.jar' olarak yeniden adlandırarak basitleştirdim.
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Uygulama çalıştığında varsayılan olarak çalışacak komut
ENTRYPOINT ["java", "-jar", "app.jar"]

# Spring Boot uygulamasının çalıştığı varsayılan port
EXPOSE 8080