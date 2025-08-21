# Use an official OpenJDK 11 image
FROM openjdk:11-jdk-slim

# Install dependencies for X11 forwarding
RUN apt-get update && \
    apt-get install -y libxext6 libxrender1 libxtst6 libxi6 libgl1-mesa-glx && \
    rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the project files
COPY . /app

# Make gradlew executable
RUN chmod +x gradlew

# Expose X11 socket
ENV DISPLAY=${DISPLAY}

# Build the project
RUN ./gradlew desktop:build

# Set the working directory for running the game
WORKDIR /app/assets

# Default command to run the game (Desktop launcher)
CMD ["../gradlew", "desktop:run"] 