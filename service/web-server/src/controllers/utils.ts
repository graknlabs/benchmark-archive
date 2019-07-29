import { IExecution } from "src/types";

export const sortResults = (orderBy: keyof IExecution, orderMethod: 'desc' | 'asc') => {
    return { sort: [{ [orderBy]: orderMethod || 'desc' }] };
};

export const limitResults = (offset: number, limit: number) => {
    return { from: offset || 0, size: limit || 50 };
};
