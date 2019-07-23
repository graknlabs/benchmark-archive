/**
 * /routes/execution
 */

import * as express from 'express';
import { Client as IEsClient } from '@elastic/elasticsearch';
import { ExecutionController, IExecutionController } from '../controllers/execution';

export const getExecutionRoutes = (esClient: IEsClient) => {
  const controller: IExecutionController = new ExecutionController(esClient);
  const router = express.Router();

  router.post('/new', controller.create);

  router.post('/delete', controller.destroy);

  router.post('/start', (req, res) => { controller.updateStatus(req, res, 'RUNNING'); });

  router.post('/completed', (req, res) => { controller.updateStatus(req, res, 'COMPLETED'); });

  router.post('/stop', (req, res) => { controller.updateStatus(req, res, 'CANCELED'); });

  router.post('/failed', (req, res) => { controller.updateStatus(req, res, 'FAILED'); });

  router.post('/query', controller.getGraphqlServer());

  return router;
};
