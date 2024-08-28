package com.rl_todo.serialization;

public class SerializableGoal
{
    public String id;
    public int target;

    public SerializableMethod from;

    public boolean IsValid()
    {
        return !id.isEmpty() && target > 0;
    }
}
