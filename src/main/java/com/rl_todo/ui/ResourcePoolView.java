package com.rl_todo.ui;

import com.rl_todo.ResourcePool;
import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.Stretchable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ResourcePoolView extends JPanel {


    public ResourcePoolView(TodoPlugin aPlugin, ResourcePool aPool, String myName)
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(new LineBorder(Color.gray,1,false));

        add(new JLabel(myName));
        add(new JSeparator());

        aPool   .All()
                .stream()
                .sorted(Comparator.comparing(Map.Entry<String,Float>::getKey))
                .forEach((kvPair) -> add(new ResourceView(aPlugin, kvPair.getKey(), kvPair.getValue())));

        add(new Stretchable());
    }
}
