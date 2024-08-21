package com.rl_todo;

import java.util.*;
import java.util.concurrent.Future;

public class ProgressSource
{
    public enum Status
    {
        NOT_SYNCED,
        SYNCED
    }

    private Status myStatus = Status.NOT_SYNCED;
    private Map<String, Integer> myProgress = new HashMap<>();
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

    public int GetProgress(String aId)
    {
        return  myProgress.getOrDefault(aId, 0);
    }

    public Set<String> GetAllKeys()
    {
        return myProgress.keySet();
    }

    public void Reset()
    {
        Set<String> keys = myProgress.keySet();
        myProgress.clear();

        keys.stream().map((String aKey) -> { myManager.CountUpdated(aKey); return true; } );
    }

    public void SetProgress(String aId, int aCount)
    {
        SetStatus(Status.SYNCED);

        int c = GetProgress(aId);
        if (c == aCount)
            return;

        if (aCount == 0)
        {
            myProgress.remove(aId);
        }
        else
        {
            myProgress.put(aId, aCount);
        }
        myManager.CountUpdated(aId);

        TodoPlugin.debug(myName + ": " + aId + " " + c + " -> " + aCount, 4);
    }
}
