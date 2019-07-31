import { Client as EsClient, ClientOptions } from '@elastic/elasticsearch';
import { config } from './config';

export const getEsClient = (): EsClient => {
    const esUri = `${config.es.host}:${config.es.port}`;
    const esClientOptions: ClientOptions = { node: esUri };
    const esClient: EsClient = new EsClient(esClientOptions);
    return esClient;
};