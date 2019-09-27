/**
 * /routes/auth
 */

import express from 'express';
import { getAuthController } from './controller';
import { checkVerification } from './middlewares';

export const getAuthRoutes = (): express.Router => {
    const controller = getAuthController();
    const router = express.Router();

    router.get('/callback', controller.oauthCallback.bind(controller));

    router.get('/verify', checkVerification, controller.verify.bind(controller));

    return router;
};
