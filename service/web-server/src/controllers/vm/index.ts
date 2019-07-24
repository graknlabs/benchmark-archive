import * as appRoot from 'app-root-path';
import * as ComputeClient from '@google-cloud/compute';
import { spawn } from 'child-process-promise';
import { IExecution } from '../../types';
import { config } from '../../config';

export interface IVMController {
    execution: IExecution;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    computeClient: any;
    zone: 'us-east1-b';
    project: 'grakn-dev';
    machineType: 'n1-standard-16';
    imageName: 'benchmark-executor-image-2';
    logsDestPath: string;
    esUri: string;
    webUri: string;

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    start: () => Promise<any>;
    execute: () => Promise<void>;
    terminate: () => Promise<void>;
    downloadLogs: () => Promise<void>;
}

// tslint:disable-next-line: function-name
export function VMController(this: IVMController, execution: IExecution) {
    this.execution = execution;
    this.computeClient = new ComputeClient();
    this.zone = 'us-east1-b';
    this.project = 'grakn-dev';
    this.machineType = 'n1-standard-16';
    this.imageName = 'benchmark-executor-image-2';
    this.esUri = `${config.es.host}:${config.es.port}`;
    this.webUri = `${config.web.host}`;

    this.start = start.bind(this);
    this.execute = execute.bind(this);
    this.terminate = terminate.bind(this);
    this.downloadLogs = downloadLogs.bind(this);
}

async function start(this: IVMController) {
    const { vmName } = this.execution;

    const config = {
        machineType: this.machineType,
        disks: [{
            boot: true,
            initializeParams: {
                sourceImage:
					`https://www.googleapis.com/compute/v1/projects/${this.project}/global/images/${this.imageName}`,
            },
        }],
        // this config assigns an external IP to the VM instance which is required for ssh access
        networkInterfaces: [{ accessConfigs: [{}] }],
    };

    console.log(`Starting the ${vmName} VM instance`);
    const createVMResp = await this.computeClient.zone(this.zone).createVM(vmName, config).catch((error) => { throw error; });
    const operation = createVMResp[1];
    return operation;
}

async function execute(this: IVMController) {
    const { vmName, commit, repoUrl, id } = this.execution;
    const bashFile = `${__dirname}/scripts/runExecute.sh`;
    const executeFile = `${appRoot.toString()}/resources/execute.sh`;

    console.log(`Executing benchmark on ${vmName} VM instance.`);

    await executeBashOnVm(
        bashFile,
        [vmName, this.zone, executeFile, commit, this.esUri, this.webUri, repoUrl, id],
    ).catch((error) => { throw error; });
}

async function terminate(this: IVMController) {
    const vmName: string = this.execution.vmName;

    console.log(`Terminating ${vmName} VM instance.`);

    const vm = this.computeClient.zone(this.zone).vm(vmName);
    const [operation] = await vm.delete();

    operation.on('complete', async () => {
        console.log(`Terminating ${vmName} disk.`);

        const disk = this.computeClient.zone(this.zone).disk(vmName);
        await disk.delete();

        console.log(`${vmName} VM instance and associated disk were successfully deleted.`);
    });
}

async function downloadLogs(this: IVMController) {
    const { vmName } = this.execution;
    const bashFile = `${__dirname}/scripts/downloadLogs.sh`;

    console.log(`Downloading logs from ${vmName} VM instance.`);

    await executeBashOnVm(
        bashFile,
        [vmName, this.zone],
    ).catch((error) => { throw error; });
}

async function executeBashOnVm(bashFile: string, options: string[] = []) {
    const workingDirectory = process.env.NODE_ENV === 'production' ? '/home/ubuntu' : appRoot.toString();

    const promise = spawn(
        'bash',
        [bashFile, process.env.GOOGLE_APPLICATION_CREDENTIALS, ...options],
        { cwd: workingDirectory },
    );
    const childProcess = promise.childProcess;

    childProcess.stdout.on('data', (data) => { console.log('[spawn] stdout: ', data.toString()); });
    childProcess.stderr.on('data', (data) => { console.log('[spawn] stderr: ', data.toString()); });

    await promise.catch((error) => { throw error; });
}
