import express from 'express';
import bodyParser from 'body-parser';
import history from 'connect-history-api-fallback';
import cookieSession from 'cookie-session';
import http from 'http';
import https from 'https';
import { config } from './config';
import { getExecutionRoutes } from './execution/routes';
import { getSpanRoutes } from './span/routes';
import { getAuthRoutes } from './auth/routes';
import { getEsClient } from './utils';
import { Client as IEsClient } from '@elastic/elasticsearch';


const server = Server();

try {
    server.loadEnvVars();
    server.registerRoutes();
    server.start();
} catch (error) {
    console.log(error);
    process.exit(1);
}

interface IServer {
    app: express.Application;
    esClient: IEsClient;
    env: string;

    loadEnvVars: () => void;
    registerRoutes: () => void;
    start: () => void;
}

function Server(): IServer {
    return {
        app: express(),
        esClient: getEsClient(),
        env: process.env.NODE_ENV || 'production',

        loadEnvVars,
        registerRoutes,
        start
    }
}

function loadEnvVars() {
    require('dotenv').config({ path: config.envPath });

    const envVars = {
        development: ['GITHUB_GRABL_TOKEN', 'GITHUB_CLIENT_ID', 'GITHUB_CLIENT_SECRET'],
        production: ['GITHUB_GRABL_TOKEN', 'GITHUB_CLIENT_ID', 'GITHUB_CLIENT_SECRET', 'SERVER_CERTIFICATE', 'SERVER_KEY'],
    };

    const missingEnvVars = envVars[this.env].filter(envVar => process.env[envVar] === undefined);

    if (missingEnvVars.length) {
        throw ` You are running in ${this.env} and the following environment variables are missing from the .env file.
        ${missingEnvVars}
        Get in touch with the team to obtain the missing environment variables.`;
    }

}

function registerRoutes() {
    // parse application/json
    this.app.use(bodyParser.json());

    this.esClient.ping((error) => {
        if (error) { throw "Elastic Search Server is down. Makes sure it's up and running before starting the web-server."; }
    });

    this.app.use('/execution', getExecutionRoutes(this.esClient));
    this.app.use('/span', getSpanRoutes(this.esClient));

    this.app.use(cookieSession({
        name: 'session', // set as key on the req object
        keys: [process.env.GITHUB_CLIENT_SECRET as string], // used as a key in signing and verifying cookie values
    }));

    this.app.use('/auth', getAuthRoutes());

    // Changes the requested location to the (default) /index.html, whenever there is a request which fulfills the following criteria:
    // 1. The request is a GET request
    // 2. which accepts text/html,
    // 3. is not a direct file request, i.e. the requested path does not contain a . (DOT) character and
    // 4. does not match a pattern provided in options.rewrites (read docs of connect-history-api-fallback)
    this.app.use(history());

    this.app.use(express.static(`${config.dashboardPath}`));
}

function start() {
    const invokedByScript = !module.parent;

    if (invokedByScript) {
        let server;
        if (this.env === 'development') {
            server = http.createServer(this.app);
        } else if (this.env === 'production') {
            const { SERVER_KEY, SERVER_CERTIFICATE } = process.env;
            const credentials = { key: SERVER_KEY, cert: SERVER_CERTIFICATE };
            server = https.createServer(credentials, this.app);
        }

        // specifying the hostname (2nd argument), forces the server to accept connections on IPv4 address
        server.listen(
            config.web.port,
            '0.0.0.0',
            () => console.log(`Grakn Benchmark Service listening on port ${config.web.port}!`)
        );
    }

    process.on('exit', () => { this.esClient.close(); });

}