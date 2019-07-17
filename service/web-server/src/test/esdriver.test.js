const EsDriver = require('../../src/EsDriver');

test('add new execution to ES',async ()=>{
    const benchmarkDriver = new EsDriver('localhost:9200');
    console.log("eeeeeeeeeeeeee");
    await benchmarkDriver.addExecution({
        commit:'eeeeeeeeeeee',
        repoUrl: '',
        repoId: '23456',
        prMergedAt: '2018-05-30T20:18:50Z',
        prNumber: "2",
        prUrl: "http://test.com/pulls/2",
        executionStartedAt: '',
        executionCompletedAt: '',
        status: 'INITIALISING',
        vmName: 'benchmark-executor-123456789qwertyuiop5467'
    });
    benchmarkDriver.close();
})