const config = {};

config.es = {};
config.web = {};

config.es.host = '0.0.0.0';
config.es.port = 9200;
config.web.port = {
    http: 80,
    https: 443
}

module.exports = config;