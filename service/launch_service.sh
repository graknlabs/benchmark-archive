#!/bin/bash

if [ -z "$1" ]
then
    INSTANCE_NAME=benchmark-service
else
    INSTANCE_NAME=$1
fi
echo "Creating benchmark service google cloud instance: $INSTANCE_NAME..."

ZONE="us-east1-b"

gcloud compute instances create $INSTANCE_NAME          \
    --image-family grakn-benchmark-service              \
    --image-project grakn-dev                           \
    --machine-type n1-standard-1                        \
    --zone $ZONE                                        \
    --tags elastic-tcp-9200,benchmark-service-tcp-4567  \
    --service-account grakn-benchmark-189@grakn-dev.iam.gserviceaccount.com \
    --scopes https://www.googleapis.com/auth/cloud-platform

# build dashboard
echo "Building dashboard..."
cd dashboard
npm install && npm run build
cd ..

# Wait until machine is up and running
echo "Waiting for $INSTANCE_NAME to be up and running..."
RET=1
while [ $RET -ne 0 ]; do
    sleep 1;
    gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='true'
    RET=$?; # collect return code
done

echo "Uploading service files to $INSTANCE_NAME..."

#Copy web dashboard files
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='mkdir -p ~/service/dashboard/'
gcloud compute scp --recurse dashboard/dist ubuntu@$INSTANCE_NAME:~/service/dashboard/ --zone=$ZONE

#Copy web server files
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='mkdir -p ~/service/web-server/'
gcloud compute scp --recurse web-server/src ubuntu@$INSTANCE_NAME:~/service/web-server/ --zone=$ZONE
gcloud compute scp web-server/package.json ubuntu@$INSTANCE_NAME:~/service/web-server/ --zone=$ZONE

#Copy executor scripts
gcloud compute scp --recurse executor ubuntu@$INSTANCE_NAME:~/executor --zone=$ZONE

#Copy service scripts
gcloud compute scp delete_instance.sh ubuntu@$INSTANCE_NAME:~/service --zone=$ZONE
gcloud compute scp launch_executor_server.sh ubuntu@$INSTANCE_NAME:~/service --zone=$ZONE
gcloud compute scp run_service.sh ubuntu@$INSTANCE_NAME:~/service --zone=$ZONE


echo "Initializing instance..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='chmod +x ~/service/run_service.sh && mkdir -p ~/logs'
echo "Launching service..."
gcloud compute ssh ubuntu@$INSTANCE_NAME --zone=$ZONE --command='tmux new -d -s run_service "~/service/run_service.sh 2>&1 | tee -a ~/logs/run_service.log" '
