/**
 * /routes/execution
 */

import * as express from 'express';
import { Client as IEsClient } from '@elastic/elasticsearch';
import { ExecutionController } from '../controllers/execution';

export const getExecutionRoutes = (esClient: IEsClient) => {
  const router = express.Router();

  router.post('/new', (req, res) => { ExecutionController.create(req, res, esClient); });

  router.post('/delete', (req, res) => { ExecutionController.destroy(req, res, esClient); });

  router.post('/start', (req, res) => { ExecutionController.updateStatus(req, res, esClient, 'RUNNING'); });

  router.post('/completed', (req, res) => { ExecutionController.updateStatus(req, res, esClient, 'COMPLETED'); });

  router.post('/stop', (req, res) => { ExecutionController.updateStatus(req, res, esClient, 'CANCELED'); });

  router.post('/failed', (req, res) => { ExecutionController.updateStatus(req, res, esClient, 'FAILED'); });

  router.post('/query', ExecutionController.getGraphqlServer(esClient));

  return router;
};
