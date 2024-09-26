package com.rl_todo.ui;

import com.rl_todo.ResourcePool;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ResourcePoolView extends JPanel {


    public ResourcePoolView(TodoPlugin aPlugin, ResourcePool aPool, String myName)
    {
        if (aPool.IsEmpty())
            return;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(new JLabel(myName));

        aPool   .All()
                .stream()
                .sorted(Comparator.comparing(Map.Entry<String,Float>::getKey))
                .forEach((kvPair) ->
        {
            add(new ResourceView(aPlugin, kvPair.getKey(), kvPair.getValue()));
        });
    }
}
