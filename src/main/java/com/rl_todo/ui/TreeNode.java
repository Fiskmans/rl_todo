package com.rl_todo.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TreeNode extends JPanel
{
    int myIndent = 20;
    int myMinHeight = 20;

    TreeBranch myBranch = null;

    TreeNode(JComponent aContent)
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(aContent);
    }

    void AddChild(JComponent aComponent)
    {
        if (myBranch == null)
        {
            myBranch = new TreeBranch();
            add(myBranch);
        }

        myBranch.AddLeaf(new TreeNode(aComponent));
        repaint();
    }

    @Override
    public Dimension getPreferredSize()
    {
        Dimension nodeArea = myContent.getPreferredSize();
        Dimension childArea = new Dimension(0, 0);

        for (TreeNode child : myChildren) {
            Dimension childSize = child.getPreferredSize();

            childArea.height += childArea.height;
            childArea.width = Math.max(childArea.width, childSize.width);
        }

        return new Dimension(Math.max(nodeArea.width, childArea.width + myIndent), Math.max(myMinHeight, nodeArea.height) + childArea.height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        paintBackground(g);
        int barStart = paintTree(g);
        if (myTarget == UNBOUNDED)
        {
            // TODO paint unbounded goals
        }
        else
        {
            paintProgressBar(g, barStart);
        }
    }
}
