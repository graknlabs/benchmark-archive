const config = {};

config.es = {};
config.web = {};

config.es.host = '0.0.0.0';
config.es.port = 9200;
<<<<<<< Updated upstream
config.web.port = process.env.WEB_PORT || 4567;
=======
config.web.port = {
    http: 80,
    https: 443
}
>>>>>>> Stashed changes

module.exports = config;