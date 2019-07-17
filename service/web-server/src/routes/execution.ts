/**
 * /routes/execution
 */

import * as express from 'express';
import { Client as IEsClient } from '@elastic/elasticsearch';
import { ExecutionController } from '../controllers/execution';

export const getExecutionRoutes = (esClient: IEsClient) => {
  const router = express.Router();

  const controller = new ExecutionController(esClient);

  router.post('/new', controller.create);

  router.post('/delete', controller.destroy);

  router.post('/start',
              (req, res, next) => { res.locals.newStatus = 'START'; next(); },
              controller.updateStatus);

  router.post('/completed',
              (req, res, next) => { res.locals.newStatus = 'COMPLETED'; next(); },
              controller.updateStatus);

  router.post('/stop',
              (req, res, next) => { res.locals.newStatus = 'STOPPED'; next(); },
              controller.updateStatus);

  router.post('/failed',
              (req, res, next) => { res.locals.newStatus = 'FAILED'; next(); },
              controller.updateStatus);

  router.post('/query', controller.getGraphqlServer());

  return router;
};
