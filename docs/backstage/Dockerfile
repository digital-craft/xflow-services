# XFlow Backstage portal — prebuilt Backstage image with the app config baked in.
# mkdocs-techdocs-core is installed so Backstage can generate TechDocs locally
# (techdocs.generator.runIn: local) without relying on a Docker socket.
FROM ghcr.io/backstage/backstage:latest

USER root
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 python3-pip && \
    rm -rf /var/lib/apt/lists/* && \
    pip3 install --break-system-packages --no-cache-dir mkdocs-techdocs-core==1.7.0

COPY app-config.yaml /app/app-config.yaml

USER node
