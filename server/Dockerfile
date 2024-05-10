FROM node:lts as builder

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
COPY package.json yarn.lock ./

RUN yarn install --frozen-lockfile

COPY . .

RUN yarn build

FROM node:lts-slim

USER root
RUN apt update && apt install -y openssl

ENV NODE_ENV production
USER node

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
COPY package.json yarn.lock ./
COPY database ./database

RUN yarn install --production --frozen-lockfile

COPY --from=builder /usr/src/app/dist ./dist

CMD ["/bin/sh", "-c", "echo 'Waiting 20 seconds to start...';sleep 20;yarn prisma db push;node dist/main.js"]

EXPOSE 3000