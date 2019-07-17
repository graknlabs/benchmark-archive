import { IExecution } from '../types';
import * as child from 'child_process';

export class InstanceController {
  execution: IExecution;

  constructor(execution) {
    this.execution = execution;
  }

  start(): void {
    const startScriptPath: string = `${__dirname}/../../launch_executor_server.sh`;
    const { repoUrl, id, commit, vmName } = this.execution;
    const ls: child.ChildProcess = child.spawn('bash', [startScriptPath, repoUrl, id, commit, vmName]);
    this.displayStream(ls);
  }

  delete(): void {
    const deleteScriptPath: string = `${__dirname}/../../delete_instance.sh`;
    const ls: child.ChildProcess = child.spawn('bash', [deleteScriptPath, this.execution.vmName]);
    this.displayStream(ls);
  }

  private displayStream(stream: child.ChildProcess): Promise<void> {
        // ???: we're not resolving or rejecting anything here. why are we returning a promise then?
        // eslint-disable-next-line no-unused-vars
    return new Promise<void>((resolve, reject) => {
      stream.stdout!.on('data', (data) => {
        console.log(`${data}`);
      });

      stream.stderr!.on('data', (data) => {
        process.stdout.write(`${data}`);
      });

      stream.on('close', (code) => {
        if (code !== 0) console.error(`Script terminated with code ${code}`);
      });
    });
  }
}
