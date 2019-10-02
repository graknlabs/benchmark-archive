import { IGlobal } from '../types';

export const checkVerification = (req, res, next) => {
    const { userId } = req.session;
    const isVerified = userId && (global as IGlobal).graknlabsMembers.some(member => member.id === userId);
    isVerified ? next() : res.status(401).json({ authorised: false });
};
