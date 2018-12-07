#!/bin/bash  
set -ev
HUBNAME="migruiz/climacompiler"
docker pull $HUBNAME || true
docker build  --cache-from $HUBNAME -f docker/Dockerfile docker/. -t $HUBNAME  . 
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin 
docker push $HUBNAME  