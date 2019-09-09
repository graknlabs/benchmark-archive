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

gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='if ps -C node; then sudo pkill node; else echo "web-server is not running in the first place. proceeding."; fi'
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='sudo rm -rf ~/service/web-server/'

echo "Preparing web-server content for upload"
# compressin the content of the web-server with the exception of node_modules/ 
zip -r web-server.zip web-server -x web-server/node_modules/\*

echo "Uploading web-server to $INSTANCE_NAME..."

gcloud compute scp web-server.zip ubuntu@$INSTANCE_NAME:~/service/ --zone=$ZONE
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='cd ~/service && unzip web-server.zip'

echo "Post upload clean up"
rm web-server.zip
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='rm ~/service/web-server.zip'

echo "Installing dependencies"
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='cd ~/service/web-server && yarn install'

echo "Starting the web-server"
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='cd ~/service/web-server && nohup sudo NODE_ENV=production yarn start 2>&1 | tee -a ~/logs/node_server.log'
