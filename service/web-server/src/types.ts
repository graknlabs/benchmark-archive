export type TStatus = 'INITIALISING' | 'STARTED' | 'RUNNING' | 'STOPPED' | 'COMPLETED' | 'FAILED';
export type TStatuses = TStatus[];

export interface IExecution {
  id: string;
  commit: string;
  prMergedAt?: string;
  prUrl?: string;
  prNumber?: number;
  repoUrl: string;
  executionInitialisedAt: string;
  executionStartedAt?: string;
  executionCompletedAt?: string;
  status: TStatus;
  vmName: string;
}
