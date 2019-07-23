import * as ComputeClient from '@google-cloud/compute';
import { spawn } from 'child-process-promise';
import { IExecution } from '../../types';
import { config } from '../../config';

export interface IVMController {
  execution: IExecution;
  computeClient: any;
  zone: 'us-east1-b';
  project: 'grakn-dev';
  esUri: string;

  start: () => any;
  terminate: () => {};
  setUp: (callback: () => {}) => void;
  runZipkin: (callback: () => {}) => void;
  runBenchmark: (callback: () => {}) => void;
}

// tslint:disable-next-line: function-name
export function VMController(this: IVMController, execution: IExecution) {
  this.execution = execution;
  this.computeClient = new ComputeClient();
  this.zone = 'us-east1-b';
  this.project = 'grakn-dev';
  this.esUri = `${config.es.host}:${config.es.port}`;

  this.start = start;
  this.terminate = terminate;
  this.setUp = setUp;
  this.runZipkin = runZipkin;
  this.runBenchmark = runBenchmark;
}

async function start(this: IVMController) {
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
  const createVMResp = await this.computeClient.zone(this.zone).createVM(vmName, config);
  const operation = createVMResp[1];
  return operation;
}

async function terminate(this: IVMController) {
  const vmName: string = this.execution.vmName;

  const vm = this.computeClient.zone(this.zone).vm(vmName);
  await vm.delete();

  const disk = this.computeClient.zone(this.zone).disk(vmName);
  await disk.delete();

  console.log(`${vmName} VM instance and associated disk were successfully deleted.`);
}

function setUp(this: IVMController, callback: () => {}) {
  console.log('Setting up the instance.');
  const bashFile = `${__dirname}/scripts/setup.sh`;
  const { vmName, repoUrl, commit } = this.execution;
  executeBashOnVm(bashFile, callback, [vmName, this.zone, repoUrl, commit]);
}

function runZipkin(this: IVMController, callback: any) {
  console.log('Running Zipkin.');
  const bashFile: string = `${__dirname}/scripts/zipkin.sh`;
  executeBashOnVm(bashFile, callback, [this.execution.vmName, this.zone]);
}

function runBenchmark(this: IVMController, callback: any) {
  console.log('Running benchmark.');
  const bashFile: string = `${__dirname}/scripts/benchmark.sh`;
  const { vmName, id } = this.execution;
  executeBashOnVm(bashFile, callback, [vmName, this.zone, id, this.esUri]);
}

function executeBashOnVm(bashFile: string, callback, options: any[] = []) {
  const promise = spawn('bash', [bashFile, process.env.GOOGLE_APPLICATION_CREDENTIALS, ...options]);
  const childProcess = promise.childProcess;

  childProcess.stdout.on('data', (data) => { console.log('[spawn] stdout: ', data.toString()); });
  childProcess.stderr.on('data', (data) => { console.log('[spawn] stderr: ', data.toString()); });

  promise
    .then(() => { callback(); })
    .catch((error) => { console.error('[spawn] ERROR: ', error); });
}
