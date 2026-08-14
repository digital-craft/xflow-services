# XFlow Backstage portal — prebuilt Backstage image.
# The app config is NOT baked in: it is mounted at runtime in the dev overlay
# (docker-compose.dev.yml), so the same image runs in any environment without
# leaking the dev config (localhost, guest auth) into production.
# mkdocs-techdocs-core is installed so Backstage can generate TechDocs locally
# (techdocs.generator.runIn: local) without relying on a Docker socket.
FROM ghcr.io/backstage/backstage:latest

USER root
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 python3-pip && \
    rm -rf /var/lib/apt/lists/* && \
    pip3 install --break-system-packages --no-cache-dir mkdocs-techdocs-core==1.7.0

# Patch the baked frontend bundle so the Notifications (and Signals) plugins are
# registered as app features. The generic image ships an app with an explicit
# minimal feature list (`features:[s.A,M]` = catalog plugin + app module); without
# this the baked NotificationsSidebarItem crashes with:
#   NotImplementedError: No implementation available for apiRef{plugin.notifications.service}
# The notifications plugin lives in module-backstage chunk as module 90295 (export A),
# the signals plugin as module 86944 (export A). Fails the build if the pattern moves.
USER node
RUN node -e 'const fs=require("fs");const dir="/app/packages/app/dist/static";const files=fs.readdirSync(dir).filter(f=>/^main\..*\.js$/.test(f));if(files.length!==1)throw new Error("expected exactly one main chunk, got "+files.join(","));const file=dir+"/"+files[0];let s=fs.readFileSync(file,"utf8");const from="features:[s.A,M]";const to="features:[s.A,M,e(90295).A,e(86944).A]";const n=s.split(from).length-1;if(n!==1)throw new Error("frontend patch pattern not found exactly once ("+n+") in "+files[0]);fs.writeFileSync(file,s.replace(from,to),"utf8");'
