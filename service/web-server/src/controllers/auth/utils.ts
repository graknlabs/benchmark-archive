import Octokit from '@octokit/rest';

export interface IGithubClient {
    grablToken: string;
    oauthAppId: string;
    oauthAppSecret: string;
    oauthTempCode: string;
    userAccessToken: undefined |string;

    setUserAccessToken: () => Promise<void>;
    getUserId: () => Promise<string>;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    getGraknLabsMembers: () => Promise<any>;
    revokeAccess: () => Promise<void>;
}

export function getGithubClient(oauthCode: string = ''): IGithubClient {
    return {
        grablToken: process.env.GITHUB_GRABL_TOKEN as string,
        oauthAppId: process.env.GITHUB_CLIENT_ID as string,
        oauthAppSecret: process.env.GITHUB_CLIENT_SECRET as string,
        oauthTempCode: oauthCode,
        userAccessToken: undefined,

        setUserAccessToken,
        getUserId,
        getGraknLabsMembers,
        revokeAccess
    }
}

async function setUserAccessToken() {
    const grablClient = new Octokit({ auth: this.grablToken });
    const accessTokenResp = await grablClient.request('POST https://github.com/login/oauth/access_token', {
        // eslint-disable-next-line @typescript-eslint/camelcase
        headers: { Accept: 'application/json' }, client_id: this.oauthAppId, client_secret: this.oauthAppSecret, code: this.oauthTempCode,
    });
    this.userAccessToken = accessTokenResp.data.access_token;
}

async function getUserId() {
    await this.setUserAccessToken();
    const userClient = new Octokit({ auth: this.userAccessToken });
    const userProfile = await userClient.users.getAuthenticated();
    const userId = userProfile.data.id;
    return userId;
}

async function getGraknLabsMembers() {
    const grablClient = new Octokit({ auth: this.grablToken });
    const membersResp = await grablClient.orgs.listMembers({ org: 'graknlabs' });
    const members = membersResp.data;
    return members;
}

async function revokeAccess() {
    const oauthClient = new Octokit({
        auth: { username: this.oauthAppId, password: this.oauthAppSecret, async on2fa() { return 'Two-factor authentication Code:'; } },
    });

    // eslint-disable-next-line @typescript-eslint/camelcase
    await oauthClient.oauthAuthorizations.revokeAuthorizationForApplication({ client_id: this.oauthAppId, access_token: this.userAccessToken });
}