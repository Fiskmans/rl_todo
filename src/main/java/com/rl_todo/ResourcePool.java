package com.rl_todo;

import jdk.internal.joptsimple.util.KeyValuePair;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.*;
import java.util.stream.Collectors;

public class ResourcePool {

    private final HashMap<String, Float> myResources = new HashMap<>();

    public void Add(String aId, float aAmount)
    {
        myResources.put(aId, myResources.getOrDefault(aId, 0.f) + aAmount);
    }

    public void Add(Resource aResource)
    {
        Add(aResource.myId, aResource.myAmount);
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
                        {
                            return (int) Math.floor(aOther.GetSpecific(kvPair.getKey()) / kvPair.getValue());
                        })
                .min(Integer::compare)
                .orElse(0);
    }

    public Set<String> Ids()
    {
        return myResources.keySet();
    }
}
