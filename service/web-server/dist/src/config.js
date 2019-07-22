"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.config = {
    es: {
        host: (process.env.NODE_ENV === 'production' ? 'http://127.0.0.1' : 'http://35.237.252.2'),
        port: 9200,
        ip: '35.237.252.2',
    },
    web: {
        port: (process.env.NODE_ENV === 'production' ? 443 : 80),
    },
    auth: {
        intervalInMinutesToFetchGraknLabsMembers: 10,
    },
};
