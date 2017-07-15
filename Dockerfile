FROM alpine:latest
MAINTAINER trebonius0

WORKDIR /app
VOLUME /pictures /cache /config
EXPOSE 8186

# User creation
RUN addgroup -S abc && adduser -S -G abc abc
RUN chown -R abc:abc /app /pictures /cache /config

# Dependencies install
RUN apk update && apk upgrade && apk add exiftool openjdk8-jre-base unzip ffmpeg curl wget

USER abc

# Software install
RUN \
    wget $(curl -s https://api.github.com/repos/trebonius0/photato/releases/latest | grep browser_download_url | cut -d '"' -f 4) \
    && unzip *.zip \
    && rm *.zip

# start
ENTRYPOINT ["java", "-Dfile.encoding=UTF8", "-Xmx1g", "-jar", "Photato-Release.jar", "/pictures", "/cache", "/config"]