package com.todo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressManager
{
    private Map<String, List<ProgressTracker>> myTrackers = new HashMap<>();
    private List<ProgressSource> mySources = new ArrayList<>();

    public void AddSource(ProgressSource aSource)
    {
        mySources.add(aSource);
        aSource.SetManager(this);
    }

    public void RemoveAllSources()
    {
        mySources.clear();
    }

    public int GetProgress(String aId)
    {
        int total = 0;
        for (ProgressSource source : mySources)
            total += source.GetProgress(aId);
        return total;
    }

    public int AddTracker(ProgressTracker aTracker, String aId)
    {
        myTrackers.putIfAbsent(aId, new ArrayList<>());
        myTrackers.get(aId).add(aTracker);

        return  GetProgress(aId);
    }

    public void CountUpdated(String aId)
    {
        if (!myTrackers.containsKey(aId))
            return;

        int count = GetProgress(aId);

        for (ProgressTracker tracker : myTrackers.get(aId))
            tracker.CountUpdated(aId, count);
    }
}
