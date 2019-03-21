const express = require('express');
const bodyParser = require('body-parser');
const elasticsearch = require('elasticsearch');

const config = require('./config');
const utils = require('./Utils');
const ExecController = require('./ExecutionsController');
const SpansController = require('./SpansController');

const ES_URI = config.es.host + ":" + config.es.port;
const LAUNCH_EXECUTOR_SCRIPT_PATH = __dirname + '/../../launch_executor_server.sh';
const DELETE_INSTANCE_SCRIPT_PATH = __dirname + '/../../delete_instance.sh';

const esClient = new elasticsearch.Client({ host: ES_URI });
const executionsController = new ExecController(esClient);
const spans = new SpansController(esClient);

const app = module.exports = express();

// Serve static files for web dashboard
app.use(express.static(__dirname + '/../../dashboard/dist'));

// parse application/json
app.use(bodyParser.json())

/**
 * End-point used to trigger new execution when PR is merged
 */
app.post('/pull_request', checkPullRequestIsMerged, (req, res) => {
    const execution = utils.parseMergedPR(req);
    executionsController.addExecution(execution)
        .then(()=> {
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
        .then(()=> {
            utils.startBenchmarking(LAUNCH_EXECUTOR_SCRIPT_PATH, execution);
            console.log("New execution added to ES.");
            res.status(200).json({ triggered: true });
        }).catch((err) => { 
            res.status(500).json({ triggered: false, error: true });
            console.error(err); 
        });
});

app.post('/execution/start', (req, res) => {
    executionsController.updateExecutionStatus(req.body, { status : 'RUNNING', executionStartedAt: new Date().toISOString()})
        .then(()=> { console.log("Execution marked as RUNNING."); })
        .catch((err) => { console.error(err); });
    res.status(200).json({});
});

app.post('/execution/completed', (req, res) => {
    executionsController.updateExecutionStatus(req.body, { status : 'RUNNING', executionCompletedAt: new Date().toISOString()})
        .then(()=> { console.log("Execution marked as COMPLETED."); })
        .catch((err) => { console.error(err); })
    utils.deleteInstance(DELETE_INSTANCE_SCRIPT_PATH, req.body.vmName);
    res.status(200).json({});
});

app.post('/execution/stop', (req, res) => {
    executionsController.updateExecutionStatus(req.body, { status : 'STOPPED', executionCompletedAt: new Date().toISOString()})
        .then(()=> { console.log("Execution marked as STOPPED."); })
        .catch((err) => { console.error(err); })
    utils.deleteInstance(DELETE_INSTANCE_SCRIPT_PATH, req.body.vmName);
    res.status(200).json({});
});

app.post('/execution/delete', (req, res) => {
    executionsController.deleteExecution(req.body)
        .then(()=> { console.log("Execution deleted."); })
        .catch((err) => { console.error(err); })
    utils.deleteInstance(DELETE_INSTANCE_SCRIPT_PATH, req.body.vmName);
    res.status(200).json({});
});

app.post('/execution/failed', (req, res) => {
    executionsController.updateExecutionStatus(req.body, { status : 'FAILED', executionCompletedAt: new Date().toISOString()})
        .then(()=> { console.log("Execution marked as FAILED."); })
        .catch((err) => { console.error(err); });
    res.status(200).json({});
});

// Register GraphQL middleware for execution queries
app.use('/execution/query', executionsController.queryExecutions());

// Register GraphQL middleware for span queries
app.use('/span/query', spans.query());

// Middleware used by /pull_request end-point
function checkPullRequestIsMerged(req, res, next){
    if(req.body.action === "closed" && req.body.pull_request.merged){
        next();
    } else {
        res.status(200).json({ triggered: false });
    }
}

// Start http server only when invoked by script
if (!module.parent) {
    app.listen(config.web.port, () => console.log(`Grakn Benchmark Service listening on port ${config.web.port}!`));
}
// Register shutdown hook to properly terminate connection to ES
process.on('exit', () => { esClient.close(); });

