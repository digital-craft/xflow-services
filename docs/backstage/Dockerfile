FROM node:22-bullseye-slim

# Install dependencies for node-gyp and backstage
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 g++ build-essential libsqlite3-dev && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the entire skeleton
COPY . .

# Install dependencies
RUN yarn install

EXPOSE 3000
EXPOSE 7007

# Run the app in development mode
CMD ["yarn", "start"]
