// tslint:disable:max-line-length

import * as ComputeClient from '@google-cloud/compute';
import { spawn } from 'child-process-promise';
import { IExecution } from '../../types';
import { config } from '../../config';

export class VmController {
  private execution: IExecution;
  private client;
  private zone: string;
  private project: string;
  private esUri: string;

  constructor(execution: IExecution) {
    this.execution = execution;
    this.client = new ComputeClient();
    this.zone = 'us-east1-b';
    this.project = 'grakn-dev';
    this.esUri = `${config.es.host}:${config.es.port}`;
  }

  start = async (): Promise<any> => {
    const { vmName } = this.execution;

    const config = {
      machineType: 'n1-standard-16',
      disks: [{
        boot: true,
        initializeParams: {
          sourceImage:
            `https://www.googleapis.com/compute/v1/projects/${this.project}/global/images/benchmark-executor-image-2`,
        },
      }],
      // this config assigns an external IP to the VM instance which is required for ssh access
      networkInterfaces: [{ accessConfigs: [{}] }],
    };

    console.log(`Starting the ${vmName} VM instance`);
    const createVMResp = await this.client.zone(this.zone).createVM(vmName, config);
    const operation = createVMResp[1];
    return operation;
  }

  delete = async (): Promise<void> => {
    const vmName: string = this.execution.vmName;

    const vm = this.client.zone(this.zone).vm(vmName);
    await vm.delete();

    const disk = this.client.zone(this.zone).disk(vmName);
    await disk.delete();

    console.log(`${vmName} VM instance and associated disk were successfully deleted.`);
  }

  setUp = (execution: IExecution, callback) => {
    console.log('Setting up the instance.');
    const bashFile = `${__dirname}/scripts/setup.sh`;
    const { vmName, repoUrl, commit } = execution;
    this.executeBashOnVm(bashFile, callback, [vmName, this.zone, repoUrl, commit]);
  }

  runZipkin = (execution: IExecution, callback: any) => {
    console.log('Running Zipkin.');
    const bashFile: string = `${__dirname}/scripts/zipkin.sh`;
    this.executeBashOnVm(bashFile, callback, [execution.vmName, this.zone]);
  }

  runBenchmark = (execution: IExecution, callback: any) => {
    console.log('Running benchmark.');
    const bashFile: string = `${__dirname}/scripts/benchmark.sh`;
    const { vmName, id } = execution;
    this.executeBashOnVm(bashFile, callback, [vmName, this.zone, id, this.esUri]);
  }

  private executeBashOnVm = async (bashFile: string, callback, options: any[] = []): Promise<(void)> => {
    const promise = spawn('bash', [bashFile, process.env.GOOGLE_APPLICATION_CREDENTIALS, ...options]);
    const childProcess = promise.childProcess;

    childProcess.stdout.on('data', (data) => { console.log('[spawn] stdout: ', data.toString()); });
    childProcess.stderr.on('data', (data) => { console.log('[spawn] stderr: ', data.toString()); });

    promise
      .then(() => { callback(); })
      .catch((error) => { console.error('[spawn] ERROR: ', error); });
  }
}
