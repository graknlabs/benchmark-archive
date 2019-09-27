/**
 * /routes/execution
 */

import express from 'express';
import { Client as IEsClient } from '@elastic/elasticsearch';
import { getExecutionController, IExecutionController } from './controller';
import { checkPRMerged } from './middlewares';
import { TStatus } from './types';

export const getExecutionRoutes = (esClient: IEsClient) => {
    const controller: IExecutionController = getExecutionController(esClient);
    const router = express.Router();

    router.post('/pull_request', checkPRMerged, controller.create.bind(controller));

    router.post('/new', controller.create.bind(controller));

    router.post('/delete', controller.destroy.bind(controller));

    router.post('/start', (req, res) => { controller.updateStatus.bind(controller)(req, res, 'RUNNING' as TStatus); });

    router.post('/completed', (req, res) => { controller.updateStatus.bind(controller)(req, res, 'COMPLETED' as TStatus); });

    router.post('/stop', (req, res) => { controller.updateStatus.bind(controller)(req, res, 'CANCELED' as TStatus); });

    router.post('/failed', (req, res) => { controller.updateStatus.bind(controller)(req, res, 'FAILED' as TStatus); });

    router.post('/query', controller.getGraphqlServer.bind(controller)());

    return router;
};
