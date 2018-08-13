package com.hemendra.comicreader.model.utils;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.WorkerThread;

/**
 * After observing several different issues with the Android's {@link android.os.AsyncTask},
 * I decided to create my own implementation of it which works in a similar way, so that,
 * replacing it in any old project is easy. We just need to change the class name.
 * @param <Params> Parameter type
 * @param <Progress> Progress type
 * @param <Result> Result type
 */
public abstract class CustomAsyncTask<Params, Progress, Result> implements Handler.Callback {

    private boolean isFinished = true;
    private Result res;
    private Thread executingThread = null;
    private boolean isCancelled = false;

    private Handler progressHandler = new Handler(this);

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null) {
            if(msg.what == 1) {
                onProgressUpdate((Progress[]) msg.obj);
            } else if(msg.what == 2) {
                if (!isCancelled)
                    onPostExecute(res);
                else
                    onCancelled();
            }
        }
        return true;
    }

    /**
     * This method gets called right before the doInBackground starts executing.
     */
    protected void onPreExecute() { }

    /**
     * Call this method in the middle of task execution to request it to stop.
     * @param interrupt if TRUE then the worker thread will be interrupted.
     */
    public void cancel(boolean interrupt) {
        isCancelled = true;
        isFinished = true;
        if (interrupt && executingThread != null)
            executingThread.interrupt();
    }

    /**
     * Check whether the task execution has been cancelled or not.
     * @return TRUE if cancelled, FALSE otherwise.
     */
    protected boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Check whether the task execution is going on, or not.
     * @return TRUE if in progress, FALSE otherwise.
     */
    public boolean isExecuting() {
        return !isFinished;
    }

    /**
     * Call this method to invoke the "onProgressUpdate" method from main thread. This method is
     * typically used to update the UI elements.
     * @param progress Provide the object array of type 'Progress'.
     */
    public void publishProgress(Progress... progress) {
        Message msg = new Message();
        msg.what = 1;
        msg.obj = progress;
        progressHandler.sendMessage(msg);
    }

    /**
     * This method is called when there is a call to "publishProgress" has been made.
     * @param progress Provides the object array of the type 'Progress'.
     */
    protected void onProgressUpdate(Progress... progress) { }

    /**
     * The instructions defined in this method will be executed in a separate background (worker)
     * thread.
     * @param params Provides the object array of type 'Params'.
     * @return Object of type 'Result'.
     */
    @WorkerThread
    protected abstract Result doInBackground(Params... params);

    /**
     * This method gets called when 'doInBackground' has been executed completely.
     * @param result Object of type 'Result' which was returned by 'doInBackground'.
     */
    protected void onPostExecute(Result result) { }

    /**
     * This method gets called when the execution was cancelled.
     */
    private void onCancelled() { }

    /**
     * Start the execution
     * @param params Provide the parameters (if any), or leave empty otherwise.
     */
    @SafeVarargs
    public final void execute(Params... params) {
        isCancelled = false;
        onPreExecute();
        //
        isFinished = false;
        //
        executingThread = new Thread(() -> {
            res = doInBackground(params);
            isFinished = true;
            Message msg = new Message();
            msg.what = 2;
            progressHandler.sendMessage(msg);
        });
        executingThread.start();
    }

}
