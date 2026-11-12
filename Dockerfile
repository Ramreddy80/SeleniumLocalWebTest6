# Stage 1: Dependency resolution and caching
FROM maven:3.9.9-eclipse-temurin-17 AS dependencies
WORKDIR /app
COPY pom.xml testng.xml ./
COPY webpage/ ./webpage/
COPY src/ ./src/
RUN mvn test-compile -DskipTests=true

# Stage 2: Runtime environment with Chromium and ChromeDriver
FROM maven:3.9.9-eclipse-temurin-17 AS runner

# Install Chromium, Chromium Driver, and required dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    chromium \
    chromium-driver \
    ca-certificates \
    fontconfig \
    fonts-liberation \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Pre-generate font cache and pre-warm Chromium headless
RUN fc-cache -f && chromium --headless=new --no-sandbox --disable-gpu --dump-dom about:blank > /dev/null 2>&1 || true

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
