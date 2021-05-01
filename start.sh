#!/bin/sh
set -e # Stop script from running if there are any errors

IMAGE="atomicboo/rulebot"                             # Docker image
GIT_VERSION=$(git describe --always --abbrev --tags --long) # Git hash and tags

# Build and tag image
docker build -t ${IMAGE}:${GIT_VERSION} .
docker tag ${IMAGE}:${GIT_VERSION} ${IMAGE}:latest

# Log in to Docker Hub and push
echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
docker push ${IMAGE}:${GIT_VERSION}

doctl compute ssh -t ${DIGITAL_TOKEN} ubuntu-honkbot --ssh-command "docker stop rulebot ; docker rm rulebot ; docker run -p 6969:6969 -v /root:/mnt/kyledahlin --env HONKBOT_CREDENTIALS=/mnt/kyledahlin/honkbot-5c9e4-firebase-adminsdk-fe2wk-a12b80cdc2.json --env RULEBOT_HOST=0.0.0.0 -d --env HONKBOT_TOKEN=/mnt/kyledahlin/honkbottoken.txt --name=rulebot --restart unless-stopped ${IMAGE}:${GIT_VERSION} && docker system prune -a -f"
