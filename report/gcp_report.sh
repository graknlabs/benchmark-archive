#!/bin/bash

if [ -z "$1" ]
then
    SERVER_INSTANCE=performance-report-server
else
    SERVER_INSTANCE=$1
fi

ZONE="us-east1-b"


echo "Creating report generator server google cloud instance: $SERVER_INSTANCE..."
# TODO set instance size
gcloud compute instances create $SERVER_INSTANCE          \
    --image-family grakn-benchmark-executor             \
    --image-project grakn-dev                           \
    --machine-type n1-standard-16                       \
    --zone $ZONE                                        \
    --tags grakn-grpc-port                              \
    --service-account grakn-benchmark-189@grakn-dev.iam.gserviceaccount.com \
    --scopes https://www.googleapis.com/auth/cloud-platform

CLIENT_INSTANCE=performance-report-client
echo "Creating report generator client-machine google cloud instance: $CLIENT_INSTANCE..."
# TODO set instance size
gcloud compute instances create $CLIENT_INSTANCE          \
    --image-family grakn-benchmark-executor             \
    --image-project grakn-dev                           \
    --machine-type n1-standard-4                        \
    --zone $ZONE                                        \
    --service-account grakn-benchmark-189@grakn-dev.iam.gserviceaccount.com \
    --scopes https://www.googleapis.com/auth/cloud-platform


# Wait until server machine is up and running
echo "Waiting for $SERVER_INSTANCE to be up and running..."
RET=1
while [ $RET -ne 0 ]; do
    sleep 1;
    gcloud compute ssh ubuntu@$SERVER_INSTANCE --zone=$ZONE --command='true'
    RET=$?; # collect return code
done


echo "Setting up Grakn Server on $SERVER_INSTANCE..."

# copy script to clone and build Grakn, then execute
gcloud compute scp report_grakn_server.sh ubuntu@$SERVER_INSTANCE:~ --zone=$ZONE
gcloud compute ssh ubuntu@$SERVER_INSTANCE --zone=$ZONE --command='chmod +x ~/report_grakn_server.sh'
gcloud compute ssh ubuntu@$SERVER_INSTANCE --zone=$ZONE --command='nohup ~/report_grakn_server.sh & '


# Wait until client machine is up and running
echo "Waiting for $CLIENT_INSTANCE to be up and running..."
RET=1
while [ $RET -ne 0 ]; do
    sleep 1;
    gcloud compute ssh ubuntu@$CLIENT_INSTANCE --zone=$ZONE --command='true'
    RET=$?; # collect return code
done

echo "Setting up Report Client on $CLIENT_INSTANCE..."
# copy script to clone and build benchmark, then execute
gcloud compute scp launch_client.sh ubuntu@$CLIENT_INSTANCE:~ --zone=$ZONE
gcloud compute ssh ubuntu@$CLIENT_INSTANCE --zone=$ZONE --command='chmod +x ~/launch_client.sh'
gcloud compute ssh ubuntu@$CLIENT_INSTANCE --zone=$ZONE --command='tmux new -d -s report_client "~/launch_client.sh $SERVER_INSTANCE > log.txt" '


# TODO poll on the client server via SCP to wait on the final JSON blob being produced


# shutdown the instances
# yes | gcloud compute instances delete $SERVER_INSTANCE --zone=$ZONE
# yes | gcloud compute instances delete $CLIENT_INSTANCE --zone=$ZONE