# Stage 1: Dependency resolution and caching with Maven 3.9.9 and Eclipse Temurin JDK 17
FROM maven:3.9.9-eclipse-temurin-17 AS dependencies
WORKDIR /app
COPY pom.xml testng.xml ./
COPY webpage/ ./webpage/
COPY src/ ./src/
RUN mvn test-compile -DskipTests=true

# Stage 2: Runtime environment with Debian native Chromium & ChromeDriver
FROM debian:bookworm-slim AS runner

# Copy Eclipse Temurin JDK 17 and Maven 3.9.9 from stage 1
COPY --from=dependencies /opt/java/openjdk /opt/java/openjdk
COPY --from=dependencies /usr/share/maven /usr/share/maven
COPY --from=dependencies /usr/local/bin/mvn /usr/local/bin/mvn

# Set Java environment variables
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

# Install native Chromium and Chromium Driver from Debian repository
RUN apt-get update && apt-get install -y --no-install-recommends \
    chromium \
    chromium-driver \
    ca-certificates \
    fontconfig \
    fonts-liberation \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Configure environment variables
ENV CHROME_BIN=/usr/bin/chromium
ENV CHROMEDRIVER_PATH=/usr/bin/chromedriver

# Copy cached dependencies from stage 1
COPY --from=dependencies /root/.m2 /root/.m2

# Set working directory
WORKDIR /app

# Copy project files
COPY pom.xml testng.xml ./
COPY webpage/ ./webpage/
COPY src/ ./src/

# Container execution command to run tests
CMD ["mvn", "clean", "test"]
