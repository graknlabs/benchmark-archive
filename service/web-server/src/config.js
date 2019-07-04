const config = {};

config.es = {};
config.web = {};
config.auth = {};

config.es.host= process.env.NODE_ENV === "production" ? "localhost" : "35.237.252.3";
config.es.port = 9200;
config.web.port = {
    http: 80,
    https: 443
}

config.auth.intervalInMinutesToFetchGraknLabsMembers = 10;

module.exports = config;