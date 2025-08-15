FROM eclipse-temurin:17-jdk-focal

# Installer wget et curl pour télécharger les binaires
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Télécharger yt-dlp binaire Linux
RUN wget https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -O /usr/local/bin/yt-dlp \
    && chmod +x /usr/local/bin/yt-dlp

# Installer FFmpeg depuis les packages officiels
RUN apt-get update && apt-get install -y \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Créer le dossier downloads
RUN mkdir -p /app/downloads

# Copier le JAR (vous devez d'abord compiler avec Maven localement)
COPY target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"] 