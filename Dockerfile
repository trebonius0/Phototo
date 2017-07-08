FROM openjdk:8
MAINTAINER trebonius0

WORKDIR /app

RUN \
    apt update && apt upgrade -y && apt install -y \
    libimage-exiftool-perl \
    unzip \
    ffmpeg

RUN \
    wget $(curl -s https://api.github.com/repos/trebonius0/photato/releases/latest | grep browser_download_url | cut -d '"' -f 4) \
    && unzip *.zip \
    && rm *.zip

# ports and volumes
EXPOSE 8186
VOLUME /pictures /cache /config

# start
ENTRYPOINT ["java", "-Dfile.encoding=UTF8", "-Xmx1g", "-jar", "Photato-Release.jar", "/pictures", "/cache", "/config"]