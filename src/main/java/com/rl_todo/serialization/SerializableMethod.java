package com.rl_todo.serialization;

import com.rl_todo.ResourcePool;

import java.util.HashMap;

public class SerializableMethod
{
    public SerializableMethod()
    {

    }

    public SerializableMethod(Float aPerCraft)
    {
        per_craft = aPerCraft;
    }

    public String name;
    public Float per_craft;

    public HashMap<String, SerializableMethod> takes;
    public HashMap<String, SerializableMethod> requires;
    public HashMap<String, Float> byproducts;

    public boolean IsValid()
    {
        return name != null && per_craft != null;
    }
}
