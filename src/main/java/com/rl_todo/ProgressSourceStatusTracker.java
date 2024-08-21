package com.rl_todo;

public interface ProgressSourceStatusTracker
{
    void OnStatusChanged(ProgressSource.Status aNewStatus);
}
