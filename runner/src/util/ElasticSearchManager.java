package grakn.benchmark.runner.util;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticSearchManager {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchManager.class);
    private static final String ES_SERVER_PROTOCOL = "http";
    private static final String ES_INDEX_TEMPLATE_NAME = "grakn-benchmark-index-template";
    private static final String INDEX_TEMPLATE =
            "{"+
                    "\"index_patterns\": [\"benchmarking:span-*\"],"+
                    "\"settings\": {"+
                    "    \"mapper\": {"+
                    "        \"dynamic\": true"+
                    "    }"+
                    "},"+
                    "\"mappings\": {"+
                    "    \"_default_\": {"+
                    "        \"dynamic_templates\": ["+
                    "            {"+
                    "              \"integers\": {"+
                    "                \"match_mapping_type\": \"long\","+
                    "                \"mapping\": {"+
                    "                  \"type\": \"long\""+
                    "                }"+
                    "              }"+
                    "            }"+
                    "        ]"+
                    "    },"+
                    "    \"span\": {"+
                    "        \"properties\": {"+
                    "            \"tags.concepts\": {"+
                    "                \"type\": \"long\""+
                    "            },"+
                    "            \"traceId\": {"+
                    "                \"type\": \"keyword\", "+
                    "                \"norms\": \"false\""+
                    "            },"+
                    "            \"name\": {"+
                    "                \"type\": \"keyword\","+
                    "                \"norms\": \"false\""+
                    "            },"+
                    "            \"annotations\": {"+
                    "                \"type\": \"object\","+
                    "                \"enabled\": \"true\""+
                    "            },"+
                    "            \"tags\": {"+
                    "                \"enabled\": true,"+
                    "                \"dynamic\": true,"+
                    "                \"type\": \"object\""+
                    "            }"+
                    "        }"+
                    "    }"+
                    "}" +
                    "}";

    private static void putIndexTemplate(RestClient esClient, String indexTemplateName, String indexTemplate) throws IOException {
        Request putTemplateRequest = new Request("PUT", "/_template/" + indexTemplateName);
        HttpEntity entity = new StringEntity(indexTemplate, ContentType.APPLICATION_JSON);
        putTemplateRequest.setEntity(entity);
        esClient.performRequest(putTemplateRequest);
        LOG.debug("Created index template `" + indexTemplateName + "`");
    }

    private static boolean indexTemplateExists(RestClient esClient, String indexTemplateName) throws IOException {
        try {
            Request templateExistsRequest = new Request("GET", "/_template/" + indexTemplateName);
            esClient.performRequest(templateExistsRequest);
            LOG.debug("Index template `" + indexTemplateName + "` already exists");
            return true;
        } catch (ResponseException err) {
            // 404 => template does not exist yet
            LOG.debug("Index template `" + indexTemplateName + "` does not exist.");
            return false;
        }
    }

    public static void init(String elasticUri) throws IOException {
        int splitIndex = elasticUri.lastIndexOf(":");
        String esServerHost = elasticUri.substring(0, splitIndex);
        Integer esServerPort = Integer.parseInt(elasticUri.substring(splitIndex+1));
        RestClientBuilder esRestClientBuilder = RestClient.builder(new HttpHost(esServerHost, esServerPort, ES_SERVER_PROTOCOL));
        esRestClientBuilder.setDefaultHeaders(new Header[]{new BasicHeader("header", "value")});
        RestClient restClient = esRestClientBuilder.build();

        if (!indexTemplateExists(restClient, ES_INDEX_TEMPLATE_NAME)) {
            putIndexTemplate(restClient, ES_INDEX_TEMPLATE_NAME, INDEX_TEMPLATE);
        }
        restClient.close();
    }
}
