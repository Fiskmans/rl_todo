package com.rl_todo.serialization;

import javax.annotation.Nullable;
import java.util.HashMap;

public class SerializableRecursiveMethod
{
    public SerializableRecursiveMethod()
    {

    }

    public SerializableRecursiveMethod(Float aPerCraft)
    {
        per_craft = aPerCraft;
    }

    public String name;

    @Nullable
    public String special;

    public Float per_craft;

    public HashMap<String, SerializableRecursiveMethod> takes;
    public HashMap<String, SerializableRecursiveMethod> requires;
    public HashMap<String, Float> byproducts;

    public boolean IsFullMethod()
    {
        return  name != null &&
                per_craft != null;
    }
}
