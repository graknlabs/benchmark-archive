const Octokit = require('@octokit/rest');
const { spawn } = require('child_process');

const checkForEnvVariables = () => {
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
};

const checkESIsRunning = (esClient) => {
    esClient.ping((err) => {
        if (err) {
            console.error("Elastic Search Server is down. Makes use it's up and running before starting the web-server.");
            process.exit(1);
        }
    })
}

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

const getGithubUserAccessToken = async (client_id, client_secret, grabl_token, oauthCode) => {
    const orgOctokit = new Octokit({ auth: grabl_token });

    const accessTokenResp = await orgOctokit.request('POST https://github.com/login/oauth/access_token', {
        headers: { Accept: "application/json" },
        client_id,
        client_secret,
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

const getGraknLabsMembers = async (grabl_token) => {
    const orgOctokit = new Octokit({ auth: grabl_token });
    try {
        const membersResp = await orgOctokit.orgs.listMembers({ org: 'graknlabs' });
        return membersResp.data;
    } catch (err) {
        console.error("There was a problem fetching members of Grakn Labs Github organisation for the purpose of authentication:", err);
        process.exit(1);
    }
}

module.exports = {
    checkForEnvVariables,
    checkESIsRunning,
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
    getGraknLabsMembers,
}
