/* eslint-disable max-len */
/* eslint-disable @typescript-eslint/camelcase */
/* eslint-disable @typescript-eslint/no-var-requires */
/* tslint:disable max-line-length */
import * as express from 'express';
import * as bodyParser from 'body-parser';
import * as http from 'http';
import * as https from 'https';
import * as history from 'connect-history-api-fallback';
import * as Octokit from '@octokit/rest';
import * as cookieSession from 'cookie-session';
import { config } from './config';

import { getExecutionRoutes } from './routes/execution';
import { getEsClient, getGraknLabsMembers, getGithubUserId, getGithubUserAccessToken, parseMergedPR, startBenchmarking } from './utils';
import { getSpanRoutes } from './routes/span';

const app = express();

const esClient = getEsClient();

// parse application/json
app.use(bodyParser.json());

app.use('/execution', getExecutionRoutes(esClient));
app.use('/span', getSpanRoutes(esClient));

const ExecutionsController = require('./ExecutionsController');

const LAUNCH_EXECUTOR_SCRIPT_PATH = `${__dirname}/../../launch_executor_server.sh`;

// const spans = new SpansController(esClient);

const currentEnv = process.env.NODE_ENV || 'production';

// setup and check for environment variables
if (currentEnv === 'development') {
    require('dotenv').config({ path: `${__dirname}/../.env` });
} else if (currentEnv === 'production') {
    require('dotenv').config({ path: '/home/ubuntu/.env' });
}

const CLIENT_ID = process.env.GITHUB_CLIENT_ID as string;
const CLIENT_SECRET = process.env.GITHUB_CLIENT_SECRET as string;
const GRABL_TOKEN = process.env.GITHUB_GRABL_TOKEN as string;

app.use(cookieSession({
    name: 'session', // set as key on the req object
    keys: [CLIENT_SECRET], // used as a key in signing and verifying cookie values
}));

/**
 * Authentication end-points and middleware
 */

// middleware to determine if the user is authenticated and
// is a member of the graknlabs Github organisation
const verifyIdentity = async (req, res, next) => {
    const { userId } = req.session;
    const isVerified = userId && graknLabsMembers.some(member => member.id === userId);
    if (isVerified) next();
    else res.status(401).json({ authorised: false });
};

app.get('/auth/callback', async (req, res) => {

    try {
        const oauthCode = req.query.code;
        const accessToken = await getGithubUserAccessToken(CLIENT_ID, CLIENT_SECRET, GRABL_TOKEN, oauthCode);
        const userId = await getGithubUserId(accessToken);
        const isAuthorised = graknLabsMembers.some(member => member.id === userId);

        if (isAuthorised) {
            req.session.userId = userId;
            // TODO: send the user back to the original page on which he was asked to authenticate himself
            res.redirect('/');
        } else {
            // the user is not a member of graknlabs Github organisation
            // and therefore his access is revoked
            const oauthOctokit = new Octokit({
                auth: {
                    username: CLIENT_ID, password: CLIENT_SECRET, async on2fa() {
                        // example: ask the user
                        return 'Two-factor authentication Code:';
                    },
                },
            });

            await oauthOctokit.oauthAuthorizations.revokeAuthorizationForApplication({
                client_id: CLIENT_ID,
                access_token: accessToken,
            });
            res.status(401).json({ authorised: false });
        }
    } catch (err) {
        console.log(err);
        const status = err.status || 400;
        res.status(status).json({ authorised: false, error: status });
    }
});

app.get('/auth/verify', verifyIdentity, (req, res) => {
    res.status(200).json({ authorised: true });
});

// // Changes the requested location to the (default) /index.html, whenever there is a request which fulfills the following criteria:
// // 1. The request is a GET request
// // 2. which accepts text/html,
// // 3. is not a direct file request, i.e. the requested path does not contain a . (DOT) character and
// // 4. does not match a pattern provided in options.rewrites (read docs of connect-history-api-fallback)
app.use(history());

app.use(express.static(`${__dirname}/../../dashboard/dist`));

// /**
//  * End-point used to trigger new execution when PR is merged
//  */
app.post('/pull_request', checkPullRequestIsMerged, (req, res) => {
    const execution = parseMergedPR(req);
    ExecutionsController.addExecution(execution)
        .then(() => {
            startBenchmarking(LAUNCH_EXECUTOR_SCRIPT_PATH, execution);
            console.log('New execution added to ES.');
            res.status(200).json({ triggered: true });
        }).catch((err) => {
            res.status(500).json({ triggered: false, error: true });
            console.error(err);
        });
});

// /**
//  * End-point used to manually trigger a new execution providing repoUrl and commit
//  */

// Register GraphQL middleware for span queries
// app.use('/span/query', verifyIdentity, spans.query());

// Middleware used by /pull_request end-point
function checkPullRequestIsMerged(req, res, next) {
    if (req.body.action === 'closed' && req.body.pull_request.merged) {
        next();
    } else {
        res.status(200).json({ triggered: false });
    }
}

/**
 * Checking if all is well before starting the server
 *  */

// Are the environment variables all set up?
const envVariables = {
    development: ['GITHUB_GRABL_TOKEN', 'GITHUB_CLIENT_ID', 'GITHUB_CLIENT_SECRET'],
    production: ['GITHUB_GRABL_TOKEN', 'GITHUB_CLIENT_ID', 'GITHUB_CLIENT_SECRET', 'SERVER_CERTIFICATE', 'SERVER_KEY'],
};
const undefinedEnvVariables = envVariables[currentEnv].filter(envVar => process.env[envVar] === undefined);

if (undefinedEnvVariables.length) {
    console.error(`
    You are running in ${currentEnv} and the following environment variables are missing from the .env file.
    ${undefinedEnvVariables}
    Get in touch with the team to obtain the missing environment variables.
    `);
    process.exit(1);
}

// Is the ElasticSearch server running?
esClient.ping((err) => {
    if (err) {
        console.error("Elastic Search Server is down. Makes sure it's up and running before starting the web-server.");
        process.exit(1);
    }
});

// Are the graknlabs members fetched successfully?
let graknLabsMembers;
getGraknLabsMembers(GRABL_TOKEN).then(members => graknLabsMembers = members).catch((err) => { printMembersFetchError(err); });
setInterval(async () => {
    getGraknLabsMembers(GRABL_TOKEN).then(members => graknLabsMembers = members).catch((err) => { printMembersFetchError(err); });
}, config.auth.intervalInMinutesToFetchGraknLabsMembers * 60 * 1000);

const printMembersFetchError = (err) => {
    console.error('There was a problem fetching members of Grakn Labs Github organisation for the purpose of authentication:', err);
    process.exit(1);
};

// Start http server only when invoked by script
if (!module.parent) {
    if (currentEnv === 'development') {
        const httpServer = http.createServer(app);
        httpServer.listen(config.web.port, '0.0.0.0', () => console.log(`Grakn Benchmark Service listening on port ${config.web.port}!`));
    } else if (currentEnv === 'production') {
        const KEY = process.env.SERVER_KEY;
        const CERTIFICATE = process.env.SERVER_CERTIFICATE;
        const credentials = { key: KEY, cert: CERTIFICATE };
        const httpsServer = https.createServer(credentials, app);

        // specifying the hostname, 2nd argument, forces the server to accept connections on IPv4 address
        httpsServer.listen(config.web.port, '0.0.0.0', () => console.log(`Grakn Benchmark Service listening on port ${config.web.port}!`)); httpsServer.listen(config.web.port, '0.0.0.0', () => console.log(`Grakn Benchmark Service listening on port ${config.web.port}!`));
    }
}
// Register shutdown hook to properly terminate connection to ES
process.on('exit', () => { esClient.close(); });
