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

# build dashboard
echo "Building dashboard..."
cd dashboard
yarn install && yarn run build
cd ..

echo "Uploading dashboard files to $INSTANCE_NAME..."

#Copy web dashboard files
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='rm -rf ~/service/dashboard/'
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='mkdir -p ~/service/dashboard/'
gcloud compute scp --recurse dashboard/dist ubuntu@$INSTANCE_NAME:~/service/dashboard/ --zone=$ZONE