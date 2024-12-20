package com.rl_todo.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AwaitUtils
{
    public static Awaitable WaitAll(Stream<Awaitable> aCollection)
    {
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger done = new AtomicInteger(0);

        Awaitable compound = new Awaitable();

        Consumer<Object> oneFinished = (sender) ->
        {
            if (done.addAndGet(1) == total.get())
                compound.SetDone();
        };

        Awaitable fence = new Awaitable();

        total.addAndGet(1);
        fence.WhenDone(oneFinished);

        aCollection.forEach((awaitable) ->
        {
            total.addAndGet(1);
            awaitable.WhenDone(oneFinished);
        });

        fence.SetDone();

        return compound;
    }
}
