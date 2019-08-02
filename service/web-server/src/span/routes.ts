/**
 * /routes/span
 */

import express from 'express';
import { Client as IEsClient } from '@elastic/elasticsearch';
import { SpanController, ISpanController } from './controller';

export const getSpanRoutes = (esClient: IEsClient) => {
    const controller: ISpanController = new SpanController(esClient);
    const router = express.Router();

    router.post('/query', controller.getGraphqlServer());

    return router;
};
