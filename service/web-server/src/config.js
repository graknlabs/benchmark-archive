const config = {};

config.es = {};
config.web = {};
config.auth = {};

config.es.host = '0.0.0.0';
config.es.port = 9200;
config.web.port = {
    http: 80,
    https: 443
}

config.auth.intervalInMinutesToFetchGraknLabsMembers = 10;

module.exports = config;