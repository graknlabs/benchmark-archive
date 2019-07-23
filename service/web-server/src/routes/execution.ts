/**
 * /routes/execution
 */

import * as express from 'express';
import { Client as IEsClient } from '@elastic/elasticsearch';
import { ExecutionController } from '../controllers/execution';

export const getExecutionRoutes = (esClient: IEsClient) => {
  const router = express.Router();

  router.post('/new', ExecutionController.create);

  router.post('/delete', ExecutionController.destroy);

  router.post('/start', (req, res) => { ExecutionController.updateStatus(req, res, 'RUNNING'); });

  router.post('/completed', (req, res) => { ExecutionController.updateStatus(req, res, 'COMPLETED'); });

  router.post('/stop', (req, res) => { ExecutionController.updateStatus(req, res, 'CANCELED'); });

  router.post('/failed', (req, res) => { ExecutionController.updateStatus(req, res, 'FAILED'); });

  router.post('/query', ExecutionController.getGraphqlServer());

  return router;
};
