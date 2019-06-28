const Octokit = require('@octokit/rest');
const { spawn } = require('child_process');

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
    })

}
const GRABL_TOKEN = process.env.GITHUB_GRABL_TOKEN;
const orgOctokit = new Octokit({ auth: GRABL_TOKEN });

const getGithubUserAccessToken = async (oauthCode) => {
    const CLIENT_ID = process.env.GITHUB_CLIENT_ID;
    const CLIENT_SECRET = process.env.GITHUB_CLIENT_SECRET;

    const accessTokenResp = await orgOctokit.request('POST https://github.com/login/oauth/access_token', {
        headers: { Accept: "application/json" },
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
        code: oauthCode
    });
    return accessTokenResp.data.access_token;
}

const getGithubUserId = async (accessToken) => {
    const userOctokit = new Octokit({ auth: accessToken });
    const userResp = await userOctokit.request('Get https://api.github.com/user', {
        headers: { Accept: "application/json" },
        access_token: accessToken,
    });
    return userResp.data.id
}

const isUserGraknLabsMember = async (userId) => {
    const membersResp = await orgOctokit.orgs.listMembers({ org: 'graknlabs' });
    const members = membersResp.data;
    return members.some((member) => member.id === userId);
}

module.exports = {
    parseMergedPR(req) {
        return {
            id: req.body.pull_request.merge_commit_sha + Date.now(),
            commit: req.body.pull_request.merge_commit_sha,
            repoUrl: req.body.repository.html_url,
            prMergedAt: req.body.pull_request.merged_at,
            prUrl: req.body.pull_request.html_url,
            prNumber: req.body.pull_request.number,
            executionInitialisedAt: new Date().toISOString(),
            status: 'INITIALISING',
            vmName: 'benchmark-executor-' + req.body.pull_request.merge_commit_sha.trim()
        }
    },
    createExecutionObject(req) {
        return {
            id: req.body.commit + Date.now(),
            commit: req.body.commit,
            repoUrl: req.body.repoUrl,
            executionInitialisedAt: new Date().toISOString(),
            status: 'INITIALISING',
            vmName: 'benchmark-executor-' + req.body.commit.trim()
        }
    },
    startBenchmarking(scriptPath, execution) {
        const ls = spawn('bash', [scriptPath, execution.repoUrl, execution.id, execution.commit, execution.vmName])
        displayStream(ls);
    },
    deleteInstance(scriptPath, vmName) {
        const ls = spawn('bash', [scriptPath, vmName])
        displayStream(ls);
    },
    getGithubUserAccessToken,
    getGithubUserId,
    isUserGraknLabsMember
}
