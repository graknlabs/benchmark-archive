/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
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

package grakn.benchmark.runner;

import grakn.benchmark.runner.executor.QueryExecutor;
import grakn.core.client.Grakn;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import grakn.benchmark.runner.executionconfig.BenchmarkConfiguration;
import grakn.benchmark.runner.executionconfig.BenchmarkConfigurationFile;
import grakn.benchmark.runner.generator.DataGenerator;
import grakn.core.util.SimpleURI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import grakn.benchmark.runner.sharedconfig.Configs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 *
 */
public class BenchmarkRunner {
    private DataGenerator dataGenerator;
    private QueryExecutor queryExecutor;
    private int numQueryRepetitions;
    private BenchmarkConfiguration configuration;

    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRunner.class);

    public BenchmarkRunner(BenchmarkConfiguration configuration, DataGenerator dataGenerator, QueryExecutor queryExecutor) {
        this.dataGenerator = dataGenerator;
        this.queryExecutor = queryExecutor;
        this.numQueryRepetitions = configuration.numQueryRepetitions();
        this.configuration = configuration;
    }

    public void run() {


        // initialize data generation if not disabled
        if (!this.configuration.noDataGeneration()) {
            this.dataGenerator.initializeGeneration();
        }

        // run a variable dataset size or the pre-initialized one
        if (this.configuration.noDataGeneration()) {
            // count the current size of the DB
            int numConcepts = this.queryExecutor.aggregateCount();
            // only 1 point to profile at
            this.queryExecutor.processStaticQueries(numQueryRepetitions, numConcepts, "Preconfigured DB - no data gen");
        } else {
            this.runAtConcepts(this.configuration.scalesToProfile());
        }

    }

    /**
     * Given a list of database sizes to perform profiling at,
     * Populate the DB to a given size, then run the benchmark
     * @param scalesToProfile
     */
    private void runAtConcepts(List<Integer> scalesToProfile) {
        for (int scale : scalesToProfile) {
            LOG.info("Running queries with " + Integer.toString(scale) + " vertices");
            this.dataGenerator.generate(scale);
            this.queryExecutor.processStaticQueries(numQueryRepetitions, scale);
        }
    }


    public static boolean indexTemplateExists(RestClient esClient, String indexTemplateName) throws IOException {
        try {
            Request templateExistsRequest = new Request(
                    "GET",
                    "/_template/" + indexTemplateName
            );
            Response response = esClient.performRequest(templateExistsRequest);
            LOG.info("Index template `" + indexTemplateName + "` already exists");
            return true;
        } catch (ResponseException err) {
            // 404 => template does not exist yet
            LOG.error("Index template `" + indexTemplateName + "` does not exist", err);
            return false;
        }
    }

    public static void putIndexTemplate(RestClient esClient, String indexTemplateName, String indexTemplate) throws IOException {
        Request putTemplateRequest = new Request(
                "PUT",
                "/_template/" + indexTemplateName
        );
        HttpEntity entity = new StringEntity(indexTemplate, ContentType.APPLICATION_JSON);
        putTemplateRequest.setEntity(entity);
        esClient.performRequest(putTemplateRequest);
        LOG.info("Created index template `" + indexTemplateName + "`");
    }

    public static void initElasticSearch() throws IOException {
        String esServerHost = "localhost";
        int esServerPort = 9200;
        String esServerProtocol = "http";
        RestClientBuilder esRestClientBuilder = RestClient.builder(new HttpHost(esServerHost, esServerPort, esServerProtocol));
        esRestClientBuilder.setDefaultHeaders(new Header[]{new BasicHeader("header", "value")});
        RestClient restClient = esRestClientBuilder.build();

        String indexTemplateName = Configs.ElasticSearchConfig.INDEX_TEMPLATE_NAME;
        if (!indexTemplateExists(restClient, indexTemplateName)) {
            String indexTemplate = Configs.ElasticSearchConfig.INDEX_TEMPLATE;
            putIndexTemplate(restClient, indexTemplateName, indexTemplate);
        }
        restClient.close();
    }

    public static Ignite initIgniteServer() throws IgniteException {
        return Ignition.start();
    }

    public static void main(String[] args) throws IOException {

        Ignite ignite = initIgniteServer();
        initElasticSearch();

        Option configFileOption = Option.builder("c")
                .longOpt("config")
                .hasArg(true)
                .desc("Benchmarking YAML file (required)")
                .required(true)
                .type(String.class)
                .build();

        Option graknAddressOption = Option.builder("u")
                .longOpt("uri")
                .hasArg(true)
                .desc("Address of the grakn cluster (default: localhost:48555)")
                .required(false)
                .type(String.class)
                .build();

        Option keyspaceOption = Option.builder("k")
                .longOpt("keyspace")
                .required(false)
                .hasArg(true)
                .desc("Specific keyspace to utilize (default: `name` in config yaml")
                .type(String.class)
                .build();
        Option noDataGenerationOption = Option.builder("ng")
                .longOpt("no-data-generation")
                .required(false)
                .desc("Disable data generation")
                .type(Boolean.class)
                .build();
        Option noSchemaLoadOption = Option.builder("ns")
                .longOpt("no-schema-load")
                .required(false)
                .desc("Disable loading a schema")
                .type(Boolean.class)
                .build();
        Option executionNameOption = Option.builder("n")
                .longOpt("execution-name")
                .hasArg(true)
                .required(false)
                .desc("Name for specific execution of the config file")
                .type(String.class)
                .build();
        Options options = new Options();
        options.addOption(configFileOption);
        options.addOption(graknAddressOption);
        options.addOption(keyspaceOption);
        options.addOption(noDataGenerationOption);
        options.addOption(noSchemaLoadOption);
        options.addOption(executionNameOption);
        CommandLineParser parser = new DefaultParser();
        CommandLine arguments;
        try {
            arguments = parser.parse(options, args);
        } catch (ParseException e) {
            (new HelpFormatter()).printHelp("Benchmarking options", options);
            throw new RuntimeException(e.getMessage());
        }

        String configFileName = arguments.getOptionValue("config");
        Path configFilePath = Paths.get(configFileName);

        // parse config yaml file into object
        ObjectMapper benchmarkConfigMapper = new ObjectMapper(new YAMLFactory());
        BenchmarkConfigurationFile configFile = benchmarkConfigMapper.readValue(
                configFilePath.toFile(),
                BenchmarkConfigurationFile.class);
        BenchmarkConfiguration benchmarkConfiguration = new BenchmarkConfiguration(configFilePath, configFile);

        // override the URI of grakn, if one has been set
        if (arguments.hasOption("uri")) {
            String grakn_uri = arguments.getOptionValue("uri");
            Configs.GRAKN_URI = grakn_uri;
        }

        // use given keyspace string if exists, otherwise use yaml file `name` tag
        if (arguments.hasOption("keyspace")) {
            benchmarkConfiguration.setKeyspace(arguments.getOptionValue("keyspace"));
        }

        // loading a schema file, enabled by default
        boolean noSchemaLoad = arguments.hasOption("no-schema-load") ? true : false;
        benchmarkConfiguration.setNoSchemaLoad(noSchemaLoad);

        // generate data true/false, else default to do generate data
        boolean noDataGeneration = arguments.hasOption("no-data-generation") ? true : false;
        benchmarkConfiguration.setNoDataGeneration(noDataGeneration);

        // generate a name for this specific execution of the benchmarking
        String executionName = arguments.getOptionValue("execution-name", "");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = dateFormat.format(new Date());
        executionName = String.join(" ", Arrays.asList(dateString, benchmarkConfiguration.getName(), executionName)).trim();

        Grakn client = new Grakn(new SimpleURI(Configs.GRAKN_URI));

        // TODO fix sometime
        // workaround to make deletions work...
        if (!benchmarkConfiguration.noSchemaLoad()) {
            System.out.println("Deleting keyspace `" + benchmarkConfiguration.getKeyspace() + "`");
            client.keyspaces().delete(benchmarkConfiguration.getKeyspace());
        }

        Grakn.Session session = client.session(benchmarkConfiguration.getKeyspace());
        int randomSeed = 0;



        // no data generation means NEITHER schema load NOR data generate
        DataGenerator dataGenerator = benchmarkConfiguration.noDataGeneration() ?
                null :
                new DataGenerator(session, benchmarkConfiguration.getName(), benchmarkConfiguration.getSchemaGraql(), randomSeed);

        // load schema if not disabled
        if (!benchmarkConfiguration.noSchemaLoad()) {
            dataGenerator.loadSchema();
        }

        QueryExecutor queryExecutor = new QueryExecutor(benchmarkConfiguration.getKeyspace(),
                                            Configs.GRAKN_URI,
                                            executionName,
                                            benchmarkConfiguration.getQueries());
        BenchmarkRunner runner = new BenchmarkRunner(benchmarkConfiguration, dataGenerator, queryExecutor);
        runner.run();

        ignite.close();
    }
}
