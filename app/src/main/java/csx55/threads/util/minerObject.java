package csx55.threads.util;

import csx55.threads.hashing.MinerThread;

public class minerObject {

    private Thread thread;
    private MinerThread myMiner;

    public minerObject(Thread t, MinerThread m){
        this.thread = t;
        this.myMiner = m;
    }

    public Thread getThread(){
        return this.thread;
    }

    public MinerThread getMiner(){
        return this.myMiner;
    }
    
}
