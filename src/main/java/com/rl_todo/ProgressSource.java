package com.rl_todo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProgressSource
{
    private Map<String, Integer> myProgress = new HashMap<>();
    private ProgressManager myManager;

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

        TodoPlugin.debug("Source now has " + aCount + " of " + aId + " (was " + c + ")");
    }
}
