package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.stream.Stream;

public class TreeBranch extends JPanel {

    JPanel myNodePanel = new JPanel();

    boolean myIsHovered = false;
    public static Color myHoverColor = Color.LIGHT_GRAY;
    public static Color myNormalColor = Color.white;

    public static int Indent = 20;
    public static int ArcRadius = 5;

    public TreeBranch()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        HorizontalStrut strut = new HorizontalStrut(Indent);
        strut.setAlignmentY(0.0f);
        add(strut);

        myNodePanel.setLayout(new BoxLayout(myNodePanel, BoxLayout.PAGE_AXIS));
        myNodePanel.setAlignmentY(0.0f);
        add(myNodePanel);

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                myIsHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                myIsHovered = false;
                repaint();
            }
        });
    }

    public <T extends JComponent & TreeNodeItem> void AddNode(T aNode)
    {
        aNode.setAlignmentX(RIGHT_ALIGNMENT);
        myNodePanel.add(aNode);
    }

    public <T extends JComponent & TreeNodeItem> void RemoveNode(T aNode)
    {
        myNodePanel.remove(aNode);
    }

    Stream<TreeNodeItem> Nodes()
    {
        return Arrays.stream(myNodePanel.getComponents())
                .map((child) -> (TreeNodeItem)child);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Point at = new Point(Indent / 2, 0);

        if (myIsHovered)
            g.setColor(myHoverColor);
        else
            g.setColor(myNormalColor);

        for(Component child : myNodePanel.getComponents())
        {
            TreeNodeItem asItem = (TreeNodeItem)child;

            Point target = SwingUtilities.convertPoint(child,0, asItem.GetAnchorDepth(), this);
            Point arcCenter = new Point(at.x + ArcRadius, target.y - ArcRadius);
            Point next = new Point(at.x, arcCenter.y);
            Point startOfBranch = new Point(arcCenter.x, target.y);

            g.drawLine(at.x, at.y, next.x, next.y);

            if (ArcRadius < 10)
                g.drawLine(next.x, next.y, startOfBranch.x, startOfBranch.y);
            else
                g.drawArc(next.x, next.y, ArcRadius, ArcRadius, 90, 180);

            g.drawLine(startOfBranch.x, startOfBranch.y, target.x, target.y);

            at = next;
        }

        //g.setColor(Color.RED);
        //g.drawRect(0,0, getWidth() - 1, getHeight() - 1);
    }
}
