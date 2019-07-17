"use strict";
/**
 * /routes/execution
 */
Object.defineProperty(exports, "__esModule", { value: true });
const express = require("express");
const execution_1 = require("../controllers/execution");
exports.getExecutionRoutes = (esClient) => {
    const router = express.Router();
    const controller = new execution_1.ExecutionController(esClient);
    router.post('/new', controller.create);
    router.post('/delete', controller.destroy);
    router.post('/start', (req, res, next) => { res.locals.newStatus = 'START'; next(); }, controller.updateStatus);
    router.post('/completed', (req, res, next) => { res.locals.newStatus = 'COMPLETED'; next(); }, controller.updateStatus);
    router.post('/stop', (req, res, next) => { res.locals.newStatus = 'STOPPED'; next(); }, controller.updateStatus);
    router.post('/failed', (req, res, next) => { res.locals.newStatus = 'FAILED'; next(); }, controller.updateStatus);
    router.post('/query', controller.getGraphqlServer());
    return router;
};
