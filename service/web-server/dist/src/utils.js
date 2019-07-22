"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const Octokit = require("@octokit/rest");
const elasticsearch_1 = require("@elastic/elasticsearch");
const config_1 = require("./config");
const { spawn } = require('child_process');
exports.getEsClient = () => {
    const esUri = `${config_1.config.es.host}:${config_1.config.es.port}`;
    const esClientOptions = {
        node: esUri,
    };
    const esClient = new elasticsearch_1.Client(esClientOptions);
    esClient.ping((error) => {
        if (error) {
            console.error("Elastic Search Server is down. Makes sure it's up and running before starting the web-server.");
            process.exit(1);
        }
    });
    return esClient;
};
exports.getGithubUserAccessToken = (clientId, clientSecret, grablToken, oauthCode) => __awaiter(this, void 0, void 0, function* () {
    const orgOctokit = new Octokit({ auth: grablToken });
    const accessTokenResp = yield orgOctokit.request('POST https://github.com/login/oauth/access_token', {
        headers: { Accept: 'application/json' },
        client_id: clientId,
        client_secret: clientSecret,
        code: oauthCode,
    });
    return accessTokenResp.data.access_token;
});
exports.getGithubUserId = (accessToken) => __awaiter(this, void 0, void 0, function* () {
    const userOctokit = new Octokit({ auth: accessToken });
    const userResp = yield userOctokit.request('Get https://api.github.com/user', {
        headers: { Accept: 'application/json' },
        access_token: accessToken,
    });
    return userResp.data.id;
});
exports.getGraknLabsMembers = (grablToken) => __awaiter(this, void 0, void 0, function* () {
    const orgOctokit = new Octokit({ auth: grablToken });
    const membersResp = yield orgOctokit.orgs.listMembers({ org: 'graknlabs' });
    const members = membersResp.data;
    return members;
});
exports.parseMergedPR = (req) => {
    return {
        id: req.body.pull_request.merge_commit_sha + Date.now(),
        commit: req.body.pull_request.merge_commit_sha,
        repoUrl: req.body.repository.html_url,
        prMergedAt: req.body.pull_request.merged_at,
        prUrl: req.body.pull_request.html_url,
        prNumber: req.body.pull_request.number,
        executionInitialisedAt: new Date().toISOString(),
        status: 'INITIALISING',
        vmName: `benchmark-executor-${req.body.pull_request.merge_commit_sha.trim()}`,
    };
};
exports.startBenchmarking = (scriptPath, execution) => {
    const ls = spawn('bash', [scriptPath, execution.repoUrl, execution.id, execution.commit, execution.vmName]);
    displayStream(ls);
};
function displayStream(stream) {
    return new Promise((resolve, reject) => {
        stream.stdout.on('data', (data) => {
            console.log(`${data}`);
        });
        stream.stderr.on('data', (data) => {
            process.stdout.write(`${data}`);
        });
        stream.on('close', (code) => {
            if (code !== 0) {
                console.error(`Script terminated with code ${code}`);
            }
        });
    });
}
// module.exports = {
//     parseMergedPR(req) {
//         return {
//             id: req.body.pull_request.merge_commit_sha + Date.now(),
//             commit: req.body.pull_request.merge_commit_sha,
//             repoUrl: req.body.repository.html_url,
//             prMergedAt: req.body.pull_request.merged_at,
//             prUrl: req.body.pull_request.html_url,
//             prNumber: req.body.pull_request.number,
//             executionInitialisedAt: new Date().toISOString(),
//             status: 'INITIALISING',
//             vmName: 'benchmark-executor-' + req.body.pull_request.merge_commit_sha.trim()
//         }
//     },
//     getGithubUserAccessToken,
//     getGithubUserId,
//     getGraknLabsMembers,
//     getEsClient,
// }
