#!/bin/bash

chmod +x ~/service/launch_executor_server.sh
chmod +x ~/service/delete_instance.sh

sudo sysctl -w vm.max_map_count=262144 
ES_VERSION=6.3.2
ES_PATH=~/elastic
mkdir -p $ES_PATH
cd $ES_PATH

if [ ! -d elasticsearch-$ES_VERSION ]
then
    wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$ES_VERSION.zip
    wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$ES_VERSION.zip.sha512 
    shasum -a 512 -c elasticsearch-$ES_VERSION.zip.sha512 
    unzip elasticsearch-$ES_VERSION.zip
fi

# --- start elasticsearch ---
# create data log directories for elastic ahead of time or data won't be persisted
mkdir -p data/elasticsearch/logs
mkdir -p data/elasticsearch/data
es_cmd="elasticsearch-$ES_VERSION/bin/elasticsearch -E path.logs=data/elasticsearch/logs -E path.data=data/elasticsearch/data -E network.host=0.0.0.0"
tmux kill-session -t elastic
tmux new-session -d -s elastic $es_cmd


# --- run server ---
# # --- clone infrastructure ---
# # TODO clone sub-directory of infrastructure that is the service rather than expecting it to be `scp`ed here
# git clone https://github.com/graknlabs/infrastructure.git
# 
# cd ~/infrastructure/benchmark/service/dashboard
# npm install && npm run build
# 
# cd ~/infrastructure/benchmark/service/web-server
# npm install
# tmux new-session -d -s node_server "node ~/infrastructure/benchmark/service/web-server/src/server.js 2>&1 | tee -a ~/logs/node_server.log "

# cd ~/service/dashboard
# npm install && npm run build

cd ~/service/web-server
npm install
tmux new-session -d -s node_server "node ~/service/web-server/src/server.js 2>&1 | tee -a ~/logs/node_server.log "

