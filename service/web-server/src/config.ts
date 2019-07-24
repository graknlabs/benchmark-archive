
interface IUri {
    host?: string;
    port: number;
    ip?: string;
}

export interface IConfig {
    es: IUri;
    web: IUri;
    auth: {
        intervalInMinutesToFetchGraknLabsMembers: number;
    };
}

export const config: IConfig = {
    es: {
        host: (process.env.NODE_ENV === 'production' ? 'http://127.0.0.1' : 'http://benchmark.grakn.ai'),
        port: 9200
    },
    web: {
        port: (process.env.NODE_ENV === 'production' ? 443 : 80),
        host: (process.env.NODE_ENV === 'production' ? 'https://benchmark.grakn.ai' : 'https://6fd44ced.ngrok.io'),
    },
    auth: {
        intervalInMinutesToFetchGraknLabsMembers: 10,
    },
};
