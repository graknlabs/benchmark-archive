if [ $# -ne 4 ]
then
    echo "No arguments supplied"
    echo "Usage: ./launch_executor_server.sh <repo https url> <repo id> <commit id> <new vm name>"
    exit 1;
fi

REPO_URL=$1
REPO_ID=$2
COMMIT=$3
INSTANCE_NAME=$4

ZONE="us-east1-b"

echo "Script invoked with the following arguments: ${REPO_URL} ${REPO_ID} ${COMMIT} ${INSTANCE_NAME}"