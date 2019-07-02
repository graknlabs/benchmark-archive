const express = require('express');
const bodyParser = require('body-parser');
const elasticsearch = require('elasticsearch');
const https = require('https');
const config = require('./config');
const utils = require('./Utils');
const ExecController = require('./ExecutionsController');
const SpansController = require('./SpansController');
const history = require('connect-history-api-fallback');
const Octokit = require('@octokit/rest');
const cookieSession = require('cookie-session');

const ES_URI = config.es.host + ":" + config.es.port;
const LAUNCH_EXECUTOR_SCRIPT_PATH = __dirname + '/../../launch_executor_server.sh';
const DELETE_INSTANCE_SCRIPT_PATH = __dirname + '/../../delete_instance.sh';

const esClient = new elasticsearch.Client({ host: ES_URI });
const executionsController = new ExecController(esClient);
const spans = new SpansController(esClient);

const app = module.exports = express();

// setup and check for environment variables
if (process.env.NODE_ENV === "development")
    require('dotenv').config({ path: '../.env' });
else
    require('dotenv').config({ path: '/home/ubuntu/.env' });

const CLIENT_ID = process.env.GITHUB_CLIENT_ID;
const CLIENT_SECRET = process.env.GITHUB_CLIENT_SECRET;
const GRABL_TOKEN = process.env.GITHUB_GRABL_TOKEN;

// with the following setting, we are allowing the cookie middleware to trust the X-Forwarded-Proto header and
// allow secure cookies being sent over plain HTTP, provided that X-Forwarded-Proto is set to https
// more on this: https://stackoverflow.com/a/23426060/10600803
app.set('trust proxy', true)

app.use(cookieSession({
    name: 'session', // set as key on the req object
    keys: [CLIENT_SECRET], // used as a key in signing and verifying cookie values
}));

// parse application/json
app.use(bodyParser.json())

/**
 * Authentication end-points and middleware
 */
const { getGraknLabsMembers, getGithubUserId, getGithubUserAccessToken } = utils;

// middleware to determine if the user is authenticated and
// is a member of the graknlabs Github organisation
const verifyIdentity = (req, res, next) => {
    const userId = req.session.userId;
    const isVerified = userId && graknLabsMembers.some((member) => member.id === userId);
    if (isVerified) next();
    else res.status(401).json({ authorised: false });
}

app.get('/auth/callback', async (req, res) => {

    try {
        const oauthCode = req.query.code;
        const accessToken = await getGithubUserAccessToken(CLIENT_ID, CLIENT_SECRET, GRABL_TOKEN, oauthCode);
        const userId = await getGithubUserId(accessToken);
        const isAuthorised = graknLabsMembers.some((member) => member.id === userId);

        if (isAuthorised) {
            req.session.userId = userId;
            // TODO: send the user back to the original page on which he was asked to authenticate himself
            res.redirect('/');
        } else {
            // the user is not a member of graknlabs Github organisation
            // and therefore his access is revoked
            const oauthOctokit = new Octokit({ auth: { username: CLIENT_ID, password: CLIENT_SECRET } });
            await oauthOctokit.oauthAuthorizations.revokeAuthorizationForApplication({
                client_id: CLIENT_ID,
                access_token: accessToken
            });
            res.status(401).json({ authorised: false })
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

// Changes the requested location to the (default) /index.html, whenever there is a request which fulfills the following criteria:
// 1. The request is a GET request
// 2. which accepts text/html,
// 3. is not a direct file request, i.e. the requested path does not contain a . (DOT) character and
// 4. does not match a pattern provided in options.rewrites (read docs of connect-history-api-fallback)
app.use(history());

app.use(express.static(__dirname + '/../../dashboard/dist'));

/**
 * End-point used to trigger new execution when PR is merged
 */
app.post('/pull_request', checkPullRequestIsMerged, (req, res) => {
    const execution = utils.parseMergedPR(req);
    executionsController.addExecution(execution)
        .then(() => {
            utils.startBenchmarking(LAUNCH_EXECUTOR_SCRIPT_PATH, execution);
            console.log("New execution added to ES.");
            res.status(200).json({ triggered: true });
        }).catch((err) => {
            res.status(500).json({ triggered: false, error: true });
            console.error(err);
        });
});

/**
 * End-point used to manually trigger a new execution providing repoUrl and commit
 */
app.post('/execution/new', (req, res) => {
    const execution = utils.createExecutionObject(req);
    executionsController.addExecution(execution)
        .then(() => {
            utils.startBenchmarking(LAUNCH_EXECUTOR_SCRIPT_PATH, execution);
            console.log("New execution added to ES.");
            res.status(200).json({ triggered: true });
        }).catch((err) => {
            res.status(500).json({ triggered: false, error: true });
            console.error(err);
        });
});

app.post('/execution/start', (req, res) => {
    executionsController.updateExecutionStatus(req.body, { status: 'RUNNING', executionStartedAt: new Date().toISOString() })
        .then(() => {
            res.status(200).json({});
            console.log("Execution marked as RUNNING.");
        }).catch((err) => { console.error(err); });
});

app.post('/execution/completed', (req, res) => {
    executionsController.updateExecutionStatus(req.body, { status: 'COMPLETED', executionCompletedAt: new Date().toISOString() })
        .then(() => {
            res.status(200).json({});
            console.log("Execution marked as COMPLETED.");
        }).catch((err) => { console.error(err); })
    utils.deleteInstance(DELETE_INSTANCE_SCRIPT_PATH, req.body.vmName);

});

app.post('/execution/stop', (req, res) => {
    executionsController.updateExecutionStatus(req.body, { status: 'STOPPED', executionCompletedAt: new Date().toISOString() })
        .then(() => {
            res.status(200).json({});
            console.log("Execution marked as STOPPED.");
        }).catch((err) => { console.error(err); })
    utils.deleteInstance(DELETE_INSTANCE_SCRIPT_PATH, req.body.vmName);

});

app.post('/execution/delete', (req, res) => {
    executionsController.deleteExecution(req.body)
        .then(() => {
            res.status(200).json({});
            console.log("Execution deleted.");
        }).catch((err) => { console.error(err); })
    utils.deleteInstance(DELETE_INSTANCE_SCRIPT_PATH, req.body.vmName);

});

app.post('/execution/failed', (req, res) => {
    executionsController.updateExecutionStatus(req.body, { status: 'FAILED', executionCompletedAt: new Date().toISOString() })
        .then(() => { console.log("Execution marked as FAILED."); })
        .catch((err) => { console.error(err); });
    res.status(200).json({});
});

// Register GraphQL middleware for execution queries
app.use('/execution/query', verifyIdentity, executionsController.queryExecutions());

// Register GraphQL middleware for span queries
app.use('/span/query', verifyIdentity, spans.query());

// Middleware used by /pull_request end-point
function checkPullRequestIsMerged(req, res, next) {
    if (req.body.action === "closed" && req.body.pull_request.merged) {
        next();
    } else {
        res.status(200).json({ triggered: false });
    }
}

/**
 * Checking if all is well before starting the server
 *  */

// Are the environment variables all set up?
const { GITHUB_GRABL_TOKEN, GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET, SERVER_CERTIFICATE, SERVER_KEY } = process.env;
if (!GITHUB_GRABL_TOKEN || !GITHUB_CLIENT_ID || !GITHUB_CLIENT_SECRET || !SERVER_CERTIFICATE || !SERVER_KEY) {
    console.error(`
    At least one of the required environmental variables is missing.
    To troubleshoot this:
        1. check the implementation of the function that is the source of this message, to find out what environmental variables are required.
        2. ensure that all required environmental variables are defined within /etc/environment on the machine that runs the web-server.
        3. get in touch with the team to obtain the required values to update /etc/environment
    `)
    process.exit(1);
}

// Is the ElasticSearch server running?
esClient.ping((err) => {
    if (err) {
        console.error("Elastic Search Server is down. Makes sure it's up and running before starting the web-server.");
        process.exit(1);
    }
})

// Are the graknlabs members fetched successfully?
let graknLabsMembers;
getGraknLabsMembers(GRABL_TOKEN).then((members) => graknLabsMembers = members).catch((err) => { printMembersFetchError(err) });
setInterval(async () => {
    getGraknLabsMembers(GRABL_TOKEN).then((members) => graknLabsMembers = members).catch((err) => { printMembersFetchError(err) });
}, config.auth.intervalInMinutesToFetchGraknLabsMembers * 60 * 1000);

const printMembersFetchError = (err) => {
    console.error("There was a problem fetching members of Grakn Labs Github organisation for the purpose of authentication:", err);
    process.exit(1);
}

const KEY = process.env.SERVER_KEY;
const CERTIFICATE = process.env.SERVER_CERTIFICATE;
const credentials = { key: KEY, cert: CERTIFICATE };
const httpsServer = https.createServer(credentials, app);

// Start http server only when invoked by script
if (!module.parent) {
    // specifying the hostname, 2nd argument, forces the server to accept connections on IPv4 address
    httpsServer.listen(config.web.port.https, "0.0.0.0", () => console.log(`Grakn Benchmark Service listening on port ${config.web.port.https}!`));
}
// Register shutdown hook to properly terminate connection to ES
process.on('exit', () => { esClient.close(); });

