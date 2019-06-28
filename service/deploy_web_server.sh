#!/bin/bash

# exit when any command fails
set -e

ZONE="us-east1-b"

if [ -z "$1" ]
then
    INSTANCE_NAME=benchmark-service
else
    INSTANCE_NAME=$1
fi

echo "Getting to a clean web-server state on $INSTANCE_NAME..."

# gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='pkill node'
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='rm -rf ~/service/web-server/'

echo "Uploading web-server files to $INSTANCE_NAME..."

gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='mkdir -p ~/service/web-server/'
gcloud compute scp --recurse web-server/src ubuntu@$INSTANCE_NAME:~/service/web-server/ --zone=$ZONE
gcloud compute scp web-server/package.json ubuntu@$INSTANCE_NAME:~/service/web-server/ --zone=$ZONE

echo "Start the updated web-server"
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='cd ~/service/web-server && npm install && node ~/service/web-server/src/server.js 2>&1 | tee -a ~/logs/node_server.log'