package com.rl_todo.ui;

import com.rl_todo.ResourcePool;
import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.Stretchable;
import com.rl_todo.ui.toolbox.VerticalStrut;
import com.rl_todo.utils.AwaitUtils;
import com.rl_todo.utils.Awaitable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourcePoolView extends JPanel {

    Awaitable myAwaitable = new Awaitable();

    public ResourcePoolView(TodoPlugin aPlugin, ResourcePool aPool, String myName)
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(new LineBorder(Color.darkGray,1,true));

        add(new JLabel(myName));
        add(new JSeparator());

        List<ResourceView> resources = aPool.All()
                .stream()
                .sorted(Comparator.comparing(Map.Entry<String,Float>::getKey))
                .map((kvPair) -> new ResourceView(aPlugin, kvPair.getKey(), kvPair.getValue()))
                .collect(Collectors.toList());

        resources.forEach(this::add);

        add(new VerticalStrut(5));

        setMaximumSize(new Dimension(230, 1000));

        AwaitUtils.WaitAll(resources.stream().map(ResourceView::Await))
                .WhenDone((sender) -> myAwaitable.SetDone());
    }

    public Awaitable Await()
    {
        return myAwaitable;
    }
}
