package com.rl_todo.ui.toolbox;

import com.rl_todo.TodoPlugin;

import java.util.HashMap;

public class Category extends TreeNode implements TreeNodeItem
{
    HashMap<String, Category> myChildren = new HashMap<>();
    TodoPlugin myPlugin;
    String myText;

    public Category(TodoPlugin aPlugin, String aText)
    {
        super(new ClickableText(aPlugin, aText, null, true));

        ((ClickableText)myContent).myOnClick = (text) -> Toggle();

        myPlugin = aPlugin;
        myText = aText;
    }

    public Category GetOrCreateChild(String aKey)
    {
        if (myChildren.containsKey(aKey))
            return myChildren.get(aKey);

        Category created = new Category(myPlugin, aKey);
        myChildren.put(aKey, created);

        created.setAlignmentX(RIGHT_ALIGNMENT);
        AddNode(created, false);

        return created;
    }

    @Override
    public int GetAnchorDepth() {
        return 8;
    }

    @Override
    public boolean IsBefore(TreeNodeItem aOther)
    {
        if (aOther instanceof ClickableText)
            return true;

        if (aOther instanceof Category)
            return myText.compareTo(((Category) aOther).myText) < 0;

        return false;
    }
}
