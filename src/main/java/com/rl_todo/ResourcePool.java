package com.rl_todo;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.*;
import java.util.stream.Collectors;

public class ResourcePool {

    private final HashMap<String, Float> myResources = new HashMap<>();

    public boolean IsEmpty()
    {
        return myResources.isEmpty();
    }

    public void Clear() {
        myResources.clear();
    }

    public void Add(String aId, float aAmount)
    {
        myResources.put(aId, myResources.getOrDefault(aId, 0.f) + aAmount);
    }

    public void Add(Resource aResource)
    {
        Add(aResource.myId, aResource.myAmount);
    }

    public void Remove(String aId)
    {
        myResources.remove(aId);
    }

    public boolean Set(String aId, float aAmount)
    {
        if (Math.abs(myResources.getOrDefault(aId, 0.f) - aAmount) < 0.001f)
            return false;

        myResources.put(aId, aAmount);

        return true;
    }

    public void AddAll(Collection<Resource> aCollection)
    {
        aCollection.forEach(this::Add);
    }

    public void AddAll(ResourcePool aPool)
    {
        aPool.myResources.forEach(this::Add);
    }

    public List<String> SharedKeys(ResourcePool aOtherPool)
    {
        List<String> shared = new ArrayList<>();

        for(String id : myResources.keySet())
            if (aOtherPool.myResources.containsKey(id))
                shared.add(id);

        return shared;
    }

    public List<Resource> GetAsList()
    {
        return myResources
                .entrySet()
                .stream()
                .map((kvPair) -> new Resource(kvPair.getKey(), kvPair.getValue()))
                .collect(Collectors.toList());
    }

    public Set<Map.Entry<String, Float>> All()
    {
        return myResources.entrySet();
    }

    public float GetSpecific(String aId)
    {
        return myResources.getOrDefault(aId, 0.f);
    }

    public ResourcePool Scaled(int aScale)
    {
        ResourcePool out = new ResourcePool();

        myResources.forEach((key, value) -> out.Add(key, value * aScale));

        return out;
    }

    public int AvailableRepeats(ResourcePool aOther)
    {
        return myResources
                .entrySet()
                .stream()
                .map((kvPair) ->
                        (int) Math.floor(aOther.GetSpecific(kvPair.getKey()) / kvPair.getValue()))
                .min(Integer::compare)
                .orElse(0);
    }

    public Set<String> Ids()
    {
        return myResources.keySet();
    }
}
