"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
// const Octokit = require('@octokit/rest');
const elasticsearch_1 = require("@elastic/elasticsearch");
const config_1 = require("./config");
exports.getEsClient = () => {
    const esUri = `${config_1.config.es.host}:${config_1.config.es.port}`;
    const esClientOptions = {
        node: esUri,
    };
    return new elasticsearch_1.Client(esClientOptions);
};
// const getGithubUserAccessToken = async (client_id, client_secret, grabl_token, oauthCode) => {
//     const orgOctokit = new Octokit({ auth: grabl_token });
//     const accessTokenResp = await orgOctokit.request('POST https://github.com/login/oauth/access_token', {
//         headers: { Accept: "application/json" },
//         client_id,
//         client_secret,
//         code: oauthCode
//     });
//     return accessTokenResp.data.access_token;
// }
// const getGithubUserId = async (accessToken) => {
//     const userOctokit = new Octokit({ auth: accessToken });
//     const userResp = await userOctokit.request('Get https://api.github.com/user', {
//         headers: { Accept: "application/json" },
//         access_token: accessToken,
//     });
//     return userResp.data.id
// }
// const getGraknLabsMembers = async (grabl_token) => {
//     const orgOctokit = new Octokit({ auth: grabl_token });
//     const membersResp = await orgOctokit.orgs.listMembers({ org: 'graknlabs' });
//     return membersResp.data;
// }
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
