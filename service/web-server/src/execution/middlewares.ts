export const checkPRMerged = (req, res, next) => {
    const { body: { action, pull_request: { merged }}} = req;
    const isMerged = action === 'closed' && merged;
    isMerged ? next() : res.status(200).json({ triggered: false, error: 'PR has not been merged yet.' });
};