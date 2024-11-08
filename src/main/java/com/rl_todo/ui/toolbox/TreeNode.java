package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;

public class TreeNode extends JPanel
{
    TreeBranch myBranch = null;

    protected JComponent myContent;

    public TreeNode(JComponent aContent)
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        aContent.setAlignmentX(RIGHT_ALIGNMENT);

        add(aContent);
        myContent = aContent;
    }

    public <T extends JComponent & TreeNodeItem> void AddNode(T aNode)
    {
        if (myBranch == null)
        {
            myBranch = new TreeBranch();
            myBranch.setAlignmentX(RIGHT_ALIGNMENT);
            add(myBranch);
        }

        myBranch.AddNode(aNode);
        //repaint();
    }

    public void RemoveAllNodes()
    {
        if (myBranch != null)
        {
            remove(myBranch);
            myBranch = null;
        }
    }

    public void Toggle()
    {
        if (myBranch != null)
            myBranch.setVisible(!myBranch.isVisible());
    }
}
