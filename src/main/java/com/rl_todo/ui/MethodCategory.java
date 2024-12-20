package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.ui.toolbox.TreeNode;
import com.rl_todo.ui.toolbox.TreeNodeItem;

import java.util.HashMap;

public class MethodCategory extends TreeNode implements TreeNodeItem
{
    HashMap<String, MethodCategory> myChildren = new HashMap<>();
    TodoPlugin myPlugin;
    String myText;

    public MethodCategory(TodoPlugin aPlugin, String aText)
    {
        super(new Selectable(aPlugin, aText, null));

        ((Selectable)myContent).SetOnSelection(this::Toggle);
        ((Selectable)myContent).setUnderlined(true);

        myPlugin = aPlugin;
        myText = aText;
    }

    MethodCategory GetOrCreateChild(String aKey)
    {
        if (myChildren.containsKey(aKey))
            return myChildren.get(aKey);

        MethodCategory created = new MethodCategory(myPlugin, aKey);
        myChildren.put(aKey, created);

        created.setAlignmentX(RIGHT_ALIGNMENT);
        AddNode(created, false);

        return created;
    }

    @Override
    public int GetAnchorDepth() {
        return 8;
    }
}
