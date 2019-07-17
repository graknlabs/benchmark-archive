/**
 * /routes/span
 */

const restify = require('restify');

const controller = require('../controllers/span');

const router = restify.createServer()

router.post('/query', controller);

module.exports = router;
