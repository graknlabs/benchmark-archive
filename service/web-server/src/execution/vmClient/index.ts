import ComputeClient from '@google-cloud/compute';
import { spawn, exec } from 'child-process-promise';
import { IExecution } from '../types';
import { config } from '../../config';

export interface IVMController {
    execution: IExecution;
    computeClient;
    zone: 'us-east1-b';
    project: 'grakn-dev';
    machineType: 'n1-standard-16';
    imageName: 'benchmark-executor-image-2';
    logsDestPath: string;
    esUri: string;
    webUri: string;
    logPath: string;

    start: () => Promise<void>;
    execute: () => Promise<void>;
    terminate: () => Promise<void>;
    downloadLogs: () => Promise<void>;
}

export function VMController(execution: IExecution) {
    this.execution = execution;
    this.computeClient = new ComputeClient();
    this.zone = 'us-east1-b';
    this.project = 'grakn-dev';
    this.machineType = 'n1-standard-16';
    this.imageName = 'benchmark-executor-image-2';
    this.esUri = `${config.es.host}:${config.es.port}`;
    this.webUri = `${config.web.host}`;
    this.logPath = config.logPath;

    this.start = start.bind(this);
    this.execute = execute.bind(this);
    this.terminate = terminate.bind(this);
    this.downloadLogs = downloadLogs.bind(this);
}

async function start() {
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

    const [vm, operation] = await this.computeClient.zone(this.zone).createVM(vmName, config).catch((error) => { throw error; });
    console.log(`Polling operation ${operation.id} of ${vmName} VM instance...`);
    await operation.promise();
    operation.on('error', async (error) => { throw error; });

    const [metadata] = await vm.getMetadata();
    const ip: string = metadata.networkInterfaces[0].accessConfigs[0].natIP;

    console.log(`${vmName} VM instance is starting. Waiting for IP ${ip} `);
    await pingVM(ip);

    console.log(`${vmName} VM instance is up and running.`);
}

async function execute() {
    const { vmName, commit, repoUrl, id } = this.execution;
    const bashFile = `${__dirname}/scripts/runExecute.sh`;
    const executeFile = `${config.resourcesPath}/execute.sh`;

    console.log(`Executing benchmark on ${vmName} VM instance.`);

    await executeBashOnVm(
        bashFile,
        [vmName, this.zone, executeFile, commit, this.esUri, this.webUri, repoUrl, id],
    ).catch((error) => { throw error; });
}

async function terminate() {
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

async function downloadLogs() {
    const { vmName } = this.execution;
    const bashFile = `${__dirname}/scripts/downloadLogs.sh`;

    console.log(`Downloading logs from ${vmName} VM instance.`);

    await executeBashOnVm(
        bashFile,
        [vmName, this.zone, this.logPath],
    ).catch((error) => { throw error; });
}

async function executeBashOnVm(bashFile: string, options: string[] = []) {
    const promise = spawn(
        'bash',
        [bashFile, process.env.GOOGLE_APPLICATION_CREDENTIALS, ...options]
    );
    const childProcess = promise.childProcess;

    childProcess.stdout.on('data', (data) => { console.log('[spawn] stdout: ', data.toString()); });
    childProcess.stderr.on('data', (data) => { console.log('[spawn] stderr: ', data.toString()); });

    await promise.catch((error) => { throw error; });
}

async function pingVM(ip: string) {
    let exit = false;
    while (!exit) {
        await new Promise(r => setTimeout(r, 2000));
        exec(`ping -c 1 ${ip}`)
            .then(() => { exit = true; })
            .catch(() => { process.stdout.write('.'); })
    }
}