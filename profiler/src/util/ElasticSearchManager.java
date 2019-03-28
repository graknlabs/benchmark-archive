/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.profiler.util;

import org.apache.commons.cli.CommandLine;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.HashMap;

import static grakn.benchmark.common.configuration.parse.BenchmarkArguments.ELASTIC_URI;

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

        HttpEntity entity = new StringEntity(INDEX_TEMPLATE, ContentType.APPLICATION_JSON);

        restClient.performRequest("PUT", "/_template/" + ES_INDEX_TEMPLATE_NAME, new HashMap<>(), entity );
        restClient.close();
    }

}
