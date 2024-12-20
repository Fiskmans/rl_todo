package com.rl_todo.utils;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Awaitable
{
    AtomicInteger myIsDone = new AtomicInteger(0);
    Consumer<Object> myOnDone = null;

    public void WhenDone(Consumer<Object> aAction)
    {
        myOnDone = aAction;
        CheckDone();
    }

    public void SetDone()
    {
        CheckDone();
    }

    private void CheckDone()
    {
        if (myIsDone.addAndGet(1) == 2)
        {
            myOnDone.accept(this);
        }
    }
}
