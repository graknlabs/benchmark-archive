import Octokit, { OrgsListMembersResponseItem } from '@octokit/rest';
import { IGlobal } from '../types';
import { config } from '../config';

export interface IGithubClient {
    grablToken: string;
    oauthAppId: string;
    oauthAppSecret: string;
    oauthTempCode: string;
    userAccessToken: undefined |string;

    setUserAccessToken: () => Promise<void>;
    getUserId: () => Promise<number>;
    getGraknLabsMembers: () => Promise<OrgsListMembersResponseItem[]>;
    revokeAccess: () => Promise<void>;
    updateGraknlabsMembers: () => Promise<void>;
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
        revokeAccess,
        updateGraknlabsMembers
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
    const userId: number = userProfile.data.id;
    return userId;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function getGraknLabsMembers() {
    const grablClient = new Octokit({ auth: this.grablToken });
    const membersResp = await grablClient.orgs.listMembers({ org: 'graknlabs' });
    const members: OrgsListMembersResponseItem[] = membersResp.data;
    return members;
}

async function revokeAccess() {
    const oauthClient = new Octokit({ auth: { clientId: this.oauthAppId, clientSecret: this.oauthAppSecret } })
    // eslint-disable-next-line @typescript-eslint/camelcase
    await oauthClient.oauthAuthorizations.revokeAuthorizationForApplication({ client_id: this.oauthAppId, access_token: this.userAccessToken });
}

async function updateGraknlabsMembers() {
    (global as IGlobal).graknlabsMembers = await this.getGraknLabsMembers();
    setInterval(async () => {
        (global as IGlobal).graknlabsMembers = await this.getGraknLabsMembers();
    }, config.auth.intervalInMinutesToFetchGraknLabsMembers * 60 * 1000)
}