package com.rl_todo.serialization;

import javax.annotation.Nullable;
import java.util.HashMap;

public class SerializableMethod
{
    public String name;

    @Nullable
    public String special;

    @Nullable
    public String category;

    public HashMap<String, Float> requires;
    public HashMap<String, Float> takes;
    public HashMap<String, Float> makes;
}
