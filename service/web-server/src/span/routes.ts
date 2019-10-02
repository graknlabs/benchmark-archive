/**
 * /routes/span
 */

import express from 'express';
import { Client as IEsClient } from '@elastic/elasticsearch';
import { getSpanController, ISpanController } from './controller';

export const getSpanRoutes = (esClient: IEsClient) => {
    const controller: ISpanController = getSpanController(esClient);
    const router = express.Router();

    router.post('/query', controller.getGraphqlServer.bind(controller)());

    return router;
};
