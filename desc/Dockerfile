# ------------------------------------------------------------------------
# STAGE 1: Builder (For compiling the Java code)
# ------------------------------------------------------------------------
FROM maven:3.9.6-amazoncorretto-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files (pom.xml, etc.) first
COPY pom.xml .

# Download dependencies (this step is cached if pom.xml doesn't change)
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Compile and package the application into a Fat Jar (App.jar)
# Replace 'package' with your actual build command if different.
# Assuming you configured the Maven Shade Plugin to create App.jar
RUN mvn clean package

# ------------------------------------------------------------------------
# STAGE 2: Runner (For running the executable Fat Jar)
# ------------------------------------------------------------------------
# Use a lightweight JRE/JDK image for minimal footprint
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the Fat Jar from the builder stage's target directory
COPY --from=builder /app/target/App.jar App.jar

# Define the port your HTTP server listens on (e.g., 8080)
EXPOSE 8080

# The command to execute the application when the container starts
# Assumes your main class is correctly set in the App.jar manifest
CMD ["java", "-jar", "App.jar"]