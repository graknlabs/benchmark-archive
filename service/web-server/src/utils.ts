/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-var-requires */
import * as Octokit from '@octokit/rest';
import { Client as EsClient, ClientOptions } from '@elastic/elasticsearch';
import { config } from './config';
const { spawn } = require('child_process');

export const getEsClient = (): EsClient => {
    const esUri = `${config.es.host}:${config.es.port}`;
    const esClientOptions: ClientOptions = {
        node: esUri,
    };

    const esClient: EsClient = new EsClient(esClientOptions);

    // esClient.ping((error) => {
    //     if (error) {
    //         console.error("Elastic Search Server is down. Makes sure it's up and running before starting the web-server.");
    //         process.exit(1);
    //     }
    // });

    return esClient;
};

export const getGithubUserAccessToken = async (clientId, clientSecret, grablToken, oauthCode) => {
    const orgOctokit = new Octokit({ auth: grablToken });

    const accessTokenResp = await orgOctokit.request('POST https://github.com/login/oauth/access_token', {
        headers: { Accept: 'application/json' },
        // eslint-disable-next-line @typescript-eslint/camelcase
        client_id: clientId,
        // eslint-disable-next-line @typescript-eslint/camelcase
        client_secret: clientSecret,
        code: oauthCode,
    });
    return accessTokenResp.data.access_token;
};

export const getGithubUserId = async (accessToken) => {
    const userOctokit = new Octokit({ auth: accessToken });
    const userResp = await userOctokit.request('Get https://api.github.com/user', {
        headers: { Accept: 'application/json' },
        // eslint-disable-next-line @typescript-eslint/camelcase
        access_token: accessToken,
    });
    return userResp.data.id;
};

export const getGraknLabsMembers = async (grablToken): Promise<any[]> => {
    const orgOctokit = new Octokit({ auth: grablToken });
    const membersResp = await orgOctokit.orgs.listMembers({ org: 'graknlabs' });
    const members = membersResp.data;
    return members;
};

export const parseMergedPR = (req) => {
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

export const startBenchmarking = (scriptPath, execution) => {
    const ls = spawn('bash', [scriptPath, execution.repoUrl, execution.id, execution.commit, execution.vmName]);
    displayStream(ls);
};

function displayStream(stream) {
    return new Promise(() => {
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
