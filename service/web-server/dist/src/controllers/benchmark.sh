#!/bin/bash

GCLOUD_CREDENTIALS=$1
VM_NAME=$2
ZONE=$3
EXECUTION_ID=$4
ES_URI=$5

gcloud auth activate-service-account --key-file $GCLOUD_CREDENTIALS
gcloud compute ssh ubuntu@$VM_NAME --zone=$ZONE --command="
      sudo /home/ubuntu/grakn/bazel-genfiles/grakn-core-all-linux/grakn server start --benchmark &&
      cd /home/ubuntu/benchmark/bazel-genfiles/profiler &&
      ./benchmark --config ./scenario/road_network/road_config_write.yml               --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI &&
      ./benchmark --config ./scenario/complex/config_write.yml                         --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI &&
      ./benchmark --config ./scenario/road_network/road_config_read.yml                --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace road_network_read &&
      ./benchmark --config ./scenario/biochemical_network/biochemical_config_read.yml  --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI &&
      ./benchmark --config ./scenario/complex/config_read.yml                          --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace generic_uniform_network_read &&
      ./benchmark --config ./scenario/reasoning/config_read.yml                        --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace reasoner --load-schema --static-data-import &&
      ./benchmark --config ./scenario/rule_scaling/config_read.yml                     --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace rule_scaling --load-schema --static-data-import &&
      ./benchmark --config ./scenario/schema/data_definition_config.yml                --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace schema --load-schema --no-data-generation &&
      ./benchmark --config ./scenario/attribute/attribute_read_config.yml              --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace attribute
"