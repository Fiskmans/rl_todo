package com.rl_todo.ui;

import com.rl_todo.ui.toolbox.TreeBranch;
import com.rl_todo.ui.toolbox.TreeNode;
import com.rl_todo.ui.toolbox.TreeNodeItem;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class MethodCategory extends TreeNode implements TreeNodeItem
{
    HashMap<String, MethodCategory> myChildren = new HashMap<>();

    public MethodCategory(String aText)
    {
        super(new JPanel());

        myContent.setAlignmentY(TOP_ALIGNMENT);
        myContent.setAlignmentX(RIGHT_ALIGNMENT);
        myContent.add(new JLabel(aText));
        myContent.setPreferredSize(new Dimension(10, 18));
    }

    MethodCategory GetOrCreateChild(String aKey)
    {
        if (myChildren.containsKey(aKey))
            return myChildren.get(aKey);

        MethodCategory created = new MethodCategory(aKey);
        myChildren.put(aKey, created);

        created.setAlignmentX(RIGHT_ALIGNMENT);
        AddNode(created);

        return created;
    }

    @Override
    public int GetAnchorDepth() {
        return 8;
    }
}
