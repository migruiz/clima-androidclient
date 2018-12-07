#!/bin/bash  
set -ev
HUBNAME="migruiz/climacompiler"
docker pull $HUBNAME || true
docker build -t $HUBNAME  -f docker/Dockerfile docker/. --cache-from $HUBNAME 
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin 
docker push $HUBNAME  