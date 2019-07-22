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
    const { id, repoUrl, commit, vmName } = this.execution;
    const startupScript = `
      #!/bin/bash

      report_failure() {
        curl --header "Content-Type: application/json" \
          --request POST \
          --data "{\"executionId\":\"${id}\" }" \
          --insecure \
          https://${this.esUri}/execution/failed

        exit 1
      }
      # catch any error - report failure to Service and exit the script
      trap report_failure ERR

      # navigate to home
      cd /home/ubuntu

      # build and unzip grakn
      git clone ${repoUrl}
      cd grakn
      sudo git checkout ${commit}
      sudo bazel build //:assemble-linux-targz
      cd bazel-genfiles
      sudo tar -xf grakn-core-all-linux.tar.gz

      # build and unzip benchmark
      cd /home/ubuntu
      git clone https://github.com/graknlabs/benchmark.git
      cd benchmark
      bazel build //:profiler-distribution
      cd bazel-genfiles
      unzip profiler.zip
    `;

    const config = {
      machineType: 'n1-standard-16',
      disks: [{
        boot: true,
        source: `https://www.googleapis.com/compute/v1/projects/${this.project}/zones/${this.zone}/disks/benchmark-executor`,
      }],
      metadata: {
        items: [
          {
            key: 'startup-script',
            value: startupScript,
          },
        ],
      },
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
    console.log(`${vmName} VM instance was successfully deleted.`);
  }

  runZipkin = (vmName: string, callback: any) => {
    console.log('Running Zipkin.');
    const bashFile: string = `${__dirname}/srcipts/zipkin.sh`;
    this.executeBashOnVm(bashFile, vmName, callback);
  }

  runBenchmark = (vmName: string, executionId: String, callback: any) => {
    console.log('Running benchmark.');
    const bashFile: string = `${__dirname}/srcipts/benchmark.sh`;
    this.executeBashOnVm(bashFile, vmName, callback, [executionId, this.esUri]);
  }

  private executeBashOnVm = async (bashFile: string, vmName: string, callback, options: any[] = []): Promise<(void)> => {
    const promise = spawn('bash', [bashFile, process.env.GOOGLE_APPLICATION_CREDENTIALS, vmName, this.zone, ...options]);
    const childProcess = promise.childProcess;

    childProcess.stdout.on('data', (data) => { console.log('[spawn] stdout: ', data.toString()); });
    childProcess.stderr.on('data', (data) => { console.log('[spawn] stderr: ', data.toString()); });

    promise
      .then(() => { callback(); })
      .catch((error) => { console.error('[spawn] ERROR: ', error); });
  }
}
