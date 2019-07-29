
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
    logDir: string;
    appRoot: string;
}

export const config: IConfig = {
    es: {
        host: (process.env.NODE_ENV === 'production' ? '127.0.0.1' : 'http://35.237.252.2'),
        port: 9200
    },
    web: {
        port: (process.env.NODE_ENV === 'production' ? 443 : 80),
        host: (process.env.NODE_ENV === 'production' ? 'https://benchmark.grakn.ai' : 'https://810d4594.ngrok.io'),
    },
    auth: {
        intervalInMinutesToFetchGraknLabsMembers: 10,
    },
    logDir: (process.env.NODE_ENV === 'production' ?  `${__dirname}/../../logs/`: `${__dirname}/../../logs`),
    appRoot: `${__dirname}/../`
};
