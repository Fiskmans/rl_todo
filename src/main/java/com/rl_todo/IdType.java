package com.rl_todo;

public enum IdType {

    Item("item"),
    Quest("quest"),
    Progression("progression");

    String myName;

    IdType(String aName)
    {
        myName = aName;
    }

    public String GetName()
    {
        return myName;
    }
}
