/**
 * /routes/auth
 */

import express from 'express';
import { AuthController } from './controller';

export const getAuthRoutes = () => {
    const controller = AuthController();
    const router = express.Router();

    // NOTE: as a temporary solution:
    //       we fetch and update graknlabsMembers
    //       in regular intervals in order to
    //       verify the users against a recent
    //       list of graknlabs github members.
    controller.updateGraknlabsMembers();

    router.get('/callback', controller.oauthCallback.bind(controller));

    router.get('/verify', controller.checkVerification.bind(controller), controller.verify.bind(controller));

    return router;
};
