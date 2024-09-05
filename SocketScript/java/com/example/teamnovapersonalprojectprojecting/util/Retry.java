package com.example.teamnovapersonalprojectprojecting.util;

import android.util.Log;

import com.sun.mail.imap.protocol.IMAPReferralException;

import org.json.JSONException;

public class Retry {
    private int currentRetry = 0;
    private int maxRetries = 1;
    private int retryInterval = 0;
    private RetryInterface retryInterface;

    public Retry(RetryInterface retryInterface){
        this.retryInterface = retryInterface;
    }

    public Retry setMaxRetries(int maxRetries){
        this.maxRetries = maxRetries;
        return this;
    }
    public Retry setRetryInterval(int retryInterval){
        this.retryInterval = retryInterval;
        return this;
    }

    public boolean execute(){
        while (currentRetry < maxRetries){
            ++currentRetry;
            if( retryInterface.execute()){
                return true;
            }
            Log.d("Retry", "Retry: " + currentRetry + "/" + maxRetries);

            if(retryInterval > 0){
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }

    public void executeAsync(){
        new Thread(() -> {
            if( execute() ){
                retryInterface.success();
            } else {
                retryInterface.allFail();
            }
        }).start();
    }

    public interface RetryInterface {
        boolean execute();

        default void allFail() { }
        default void success() { }
    }
}
