package grakn.benchmark.runner.util;

import org.apache.commons.cli.CommandLine;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

import static grakn.benchmark.runner.util.BenchmarkArguments.ELASTIC_URI;

/**
 * Elastic Search manager used to override the default Zipkin index template which excludes the tags object.
 * We need to include the tags object so that we can use it to retrieve spans relative to specific graphs
 * or queries. Also we set the index template order to 10 to make sure it gets higher priority thant the zipkin defaults.
 */

public class ElasticSearchManager {
    private static final String DEFAULT_ES_SERVER_URI = "http://localhost:9200";
    private static final String ES_INDEX_TEMPLATE_NAME = "grakn-benchmark-index-template";
    private static final String INDEX_TEMPLATE =
            "{ \"order\" : 10,\n" +
                    "  \"index_patterns\": [\"benchmark:span-*\"],\n" +
                    "  \"mappings\": {\n" +
                    "    \"span\": { \n" +
                    "      \"_source\": {\"excludes\": [\"_q\"] },\n"+
                    "      \"properties\": {\n" +
                    "        \"tags\": { \"enabled\": true }\n" +
                    "}}}}";

    public static void putIndexTemplate(CommandLine arguments) throws IOException {
        String serverURI =  (arguments.hasOption(ELASTIC_URI)) ? arguments.getOptionValue(ELASTIC_URI) : DEFAULT_ES_SERVER_URI;
        RestClient restClient = RestClient.builder(HttpHost.create(serverURI)).build();

        Request putTemplateRequest = new Request("PUT", "/_template/" + ES_INDEX_TEMPLATE_NAME);
        HttpEntity entity = new StringEntity(INDEX_TEMPLATE, ContentType.APPLICATION_JSON);
        putTemplateRequest.setEntity(entity);

        restClient.performRequest(putTemplateRequest);
        restClient.close();
    }

}
