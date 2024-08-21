package com.rl_todo;

public class Utils
{
    public static int Clamp(int aValue, int aMin, int aMax)
    {
        if (aValue < aMin)
            return aMin;

        if (aValue > aMax)
            return aMax;

        return aValue;
    }
}
