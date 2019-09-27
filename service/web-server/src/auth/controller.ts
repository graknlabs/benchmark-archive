import { getGithubClient, IGithubClient } from './githubClient';
import { IGlobal } from '../types';

export interface IAuthController {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    oauthCallback: (req, res) => Promise<void>;
    verify: (req, res) => void;
}

export function getAuthController(): IAuthController {

    // NOTE: as a temporary solution:
    //       we fetch and update graknlabsMembers
    //       in regular intervals in order to
    //       verify the users against a recent
    //       list of graknlabs github members.
    const ghClient: IGithubClient = getGithubClient();
    ghClient.updateGraknlabsMembers();

    return {
        oauthCallback,
        verify,
    };
}

async function oauthCallback(req, res) {
    try {
        const oauthCode: string = req.query.code;
        const ghClient: IGithubClient = getGithubClient(oauthCode);

        const userId = await ghClient.getUserId();
        const isAuthorised = (global as IGlobal).graknlabsMembers.some(member => member.id === userId);

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