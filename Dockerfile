# Stage 1: Dependency resolution and caching
FROM maven:3.9.9-eclipse-temurin-17 AS dependencies
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Stage 2: Runtime environment with Chromium and ChromeDriver
FROM maven:3.9.9-eclipse-temurin-17 AS runner

# Install Chromium and Chromium Driver from Debian repository
RUN apt-get update && apt-get install -y --no-install-recommends \
    chromium \
    chromium-driver \
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
