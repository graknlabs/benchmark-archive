import { Client as EsClient, ClientOptions } from '@elastic/elasticsearch';
import { config } from './config';

export const getEsClient = (): EsClient => {
    const esUri = `${config.es.host}:${config.es.port}`;
    const esClientOptions: ClientOptions = { node: esUri };
    const esClient: EsClient = new EsClient(esClientOptions);
    return esClient;
};


export const sortResults = (orderBy: string, orderMethod: 'desc' | 'asc') => {
    return { sort: [{ [orderBy]: orderMethod || 'desc' }] };
};

export const limitResults = (offset: number, limit: number) => {
    return { from: offset || 0, size: limit || 50 };
};
