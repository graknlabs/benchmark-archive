import { getGithubClient, IGithubClient } from './githubClient';
import { config } from '../config';

export interface IAuthController {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    graknLabsMembers: undefined | any[];
    updateGraknlabsMembers: () => void;
    oauthCallback: (req, res) => void;
    verify: (req, res, status) => void;
    checkVerification: (req, res, next) => void;
}

export function AuthController(): IAuthController {
    return {
        graknLabsMembers: undefined,
        updateGraknlabsMembers,
        oauthCallback,
        verify,
        checkVerification
    };
}

async function oauthCallback(req, res) {
    try {
        const oauthCode = req.query.code;
        const ghClient: IGithubClient = getGithubClient(oauthCode);

        const userId = await ghClient.getUserId();
        const isAuthorised = this.graknlabsMembers.some(member => member.id === userId);

        if (isAuthorised) {
            req.session.userId = userId;
            // TODO: send the user back to the original page on which the user was required to authenticate
            res.redirect('/');
        } else {
            await ghClient.revokeAccess();
            res.status(401).json({ authorised: false });
        }
    } catch (error) {
        console.log(error);
        const status = error.status || 400;
        res.status(status).json({ authorised: false, error: status });
    }
}

function verify (req, res) {
    res.status(200).json({ authorised: true });
}

function checkVerification (req, res, next) {
    const { userId } = req.session;
    const isVerified = userId && this.graknlabsMembers.some(member => member.id === userId);
    if (isVerified) next();
    else res.status(401).json({ authorised: false });
};


async function updateGraknlabsMembers() {
    const ghClient: IGithubClient = getGithubClient();

    this.graknlabsMembers = await ghClient.getGraknLabsMembers();
    setInterval(async () => {
        this.graknlabsMembers = await ghClient.getGraknLabsMembers();
    }, config.auth.intervalInMinutesToFetchGraknLabsMembers * 60 * 1000)
}