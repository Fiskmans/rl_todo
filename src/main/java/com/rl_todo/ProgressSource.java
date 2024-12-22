package com.rl_todo;

import java.util.*;

public class ProgressSource
{
    public enum Status
    {
        NOT_SYNCED,
        SYNCED
    }

    private Status myStatus = Status.NOT_SYNCED;
    private ResourcePool myProgress = new ResourcePool();
    private ProgressManager myManager;
    private String myName;
    private List<ProgressSourceStatusTracker> myStatusSubscribers = new ArrayList<>();
    private Runnable myRefreshAction = null;

    public ProgressSource(String aName)
    {
        myName = aName;
    }

    public void SetRefresh(Runnable aRunnable)
    {
        myRefreshAction = aRunnable;
    }

    public void Refresh()
    {
        if (Objects.isNull(myRefreshAction))
            return;

        myRefreshAction.run();
    }

    public String GetName()
    {
        return myName;
    }

    public ResourcePool All()
    {
        return myProgress;
    }

    private void SetStatus(Status aStatus)
    {
        if (aStatus.equals(myStatus))
            return;

        myStatus = aStatus;

        for (ProgressSourceStatusTracker tracker : myStatusSubscribers) {
            tracker.OnStatusChanged(myStatus);
        }
    }

    public void OnStatusChanged(ProgressSourceStatusTracker aTracker)
    {
        myStatusSubscribers.add(aTracker);

        aTracker.OnStatusChanged(myStatus);
    }

    public void SetManager(ProgressManager aManager)
    {
        myManager = aManager;
    }

    public float GetProgress(String aId)
    {
        return myProgress.GetSpecific(aId);
    }

    public Set<String> GetAllKeys()
    {
        return myProgress.Ids();
    }

    public void Reset()
    {
        Set<String> keys = myProgress.Ids();
        myProgress.Clear();

        keys.forEach((String aKey) -> myManager.CountUpdated(aKey));
    }

    public void SetProgress(String aId, int aCount)
    {
        SetStatus(Status.SYNCED);

        if (myProgress.Set(aId, aCount))
            myManager.CountUpdated(aId);
    }
}
