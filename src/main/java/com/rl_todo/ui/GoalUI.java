package com.rl_todo.ui;

import com.rl_todo.Goal;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GoalUI extends JPanel {

    TodoPlugin myPlugin;
    Goal myGoal;

    GoalUI(TodoPlugin aPlugin, Goal aGoal)
    {
        myPlugin = aPlugin;
        myGoal = aGoal;

        add(Box.createRigidArea(new Dimension(50,50)));

        setMaximumSize(new Dimension(20000, 20));
        setPreferredSize(new Dimension(200, 20));
        setMinimumSize(new Dimension(20, 20));
        setBackground(Color.white);

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    OpenPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    OpenPopup(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

            private void OpenPopup(MouseEvent e)
            {
                GoalPopup menu = new GoalPopup(myPlugin, myGoal);
                menu.show(e.getComponent(), e.getX(), e.getY() + 14);
            }
        });
    }

    boolean CanClearMethod()
    {
        return myGoal.HasMethod();
    }

    boolean CanSetTarget()
    {
        return myGoal.CanSetTarget();
    }

    boolean CanDelete()
    {
        return myGoal.IsRoot();
    }

    String GetSetAmountStartText()
    {
        return Integer.toString(myGoal.GetTarget());
    }

    int GetMaxTarget()
    {
        return myGoal.MaxTarget();
    }

    boolean TrySetTarget(int aTarget)
    {
        return myGoal.SetTarget(aTarget);
    }

    /*
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

    private void paintBackground(Graphics g)
    {
        if (myIsHovered)
        {
            g.setColor(new Color(60, 60, 60));
        }
        else if (!Objects.isNull(myParent) && myParent.myIsHovered)
        {
            g.setColor(new Color(50, 50, 50));
        }
        else
        {
            g.setColor(new Color(43, 43, 44));
        }

        if (!Objects.isNull(ourDragging))
        {
            if (ourDragging == this)
            {
                g.setColor(new Color(60, 255, 60));
            }
            else if (myParent == ourDragging.myParent)
            {
                if (myIsHovered)
                {
                    g.setColor(new Color(60, 150, 150));
                }
                else
                {
                    g.setColor(new Color(60, 100, 100));
                }
            }
        }

        g.fillRect(0,0,getWidth(),getHeight());
    }

    private int paintTree(Graphics g)
    {
        Stack<Goal> hierarcy = new Stack<>();

        Goal at = myParent;
        while (!Objects.isNull(at))
        {
            hierarcy.push(at);
            at = at.myParent;
        }

        g.setColor(myPlugin.myConfig.treeColor());

        int depth = 0;

        if(!hierarcy.isEmpty())
        {
            hierarcy.pop(); // the root node does not have any branches

            while(!hierarcy.isEmpty())
            {
                Goal goal = hierarcy.pop();

                int xMiddle = depth + (int)Math.round(myPlugin.myConfig.indent() * 0.5);
                if (!goal.myIsLast)
                    g.drawLine(xMiddle, 0, xMiddle, getHeight());

                depth += myPlugin.myConfig.indent();
            }
        }

        if(!Objects.isNull(myParent))
        {
            int xMiddle = depth + (int)Math.round(myPlugin.myConfig.indent() * 0.5);
            int xRight = depth + myPlugin.myConfig.indent();
            int yMiddle = (int)Math.round(myPlugin.myConfig.rowHeight() * 0.5);
            int offset = (int)Math.round(Math.min(myPlugin.myConfig.indent(), myPlugin.myConfig.rowHeight()) * 0.2);

            if (myIsLast)
            {
                g.drawLine(xMiddle, 0, xMiddle, yMiddle - offset);
                g.drawLine(xMiddle, yMiddle - offset, xMiddle + offset, yMiddle);
                g.drawLine(xMiddle + offset, yMiddle, xRight, yMiddle);
            }
            else
            {
                g.drawLine(xMiddle, 0, xMiddle, getHeight());
                g.drawLine(xMiddle, yMiddle, xRight, yMiddle);
            }
            depth += myPlugin.myConfig.indent();
        }


        int iconStart = depth;

        if (myIcon != null)
            g.drawImage(myIcon, iconStart + 1,1,myPlugin.myConfig.rowHeight() - 2,myPlugin.myConfig.rowHeight() - 2, null);

        //image border
        g.drawRect(depth, 0, myPlugin.myConfig.rowHeight() - 1, myPlugin.myConfig.rowHeight() - 1);

        if (myIsInDoubleRowMode && myChildren.size() > 0)
        {
            int childBranch = depth + (int)Math.round(myPlugin.myConfig.indent() * 0.5);

            g.drawLine(childBranch, myPlugin.myConfig.rowHeight(), childBranch, getHeight());
        }

        return depth + myPlugin.myConfig.rowHeight();
    }

    private void paintProgressBar(Graphics g, int aStartPosition)
    {
        float progress = (float)GetProgress() / GetTarget();
        float banked = (float)Math.min(GetProgress() + GetBanked(), GetTarget()) / GetTarget();


        int barWidth = getWidth() - aStartPosition;

        FontMetrics metrics = g.getFontMetrics();
        String progressText = GetProgressText();

        int totalTextWidth = 0;

        {
            char[] chars = new char[myPrettyId.length()];
            myPrettyId.getChars(0,myPrettyId.length(), chars, 0);

            totalTextWidth += metrics.charsWidth(chars,0, chars.length);
        }

        if (myTarget != 1)
        {
            char[] chars = new char[progressText.length()];
            progressText.getChars(0,progressText.length(), chars, 0);
            totalTextWidth += metrics.charsWidth(chars,0, chars.length);
        }

        if (totalTextWidth + 10 > barWidth && !progressText.equals(""))
        {
            if (!myIsInDoubleRowMode)
            {
                myIsInDoubleRowMode = true;
                myPlugin.myPanel.GetGoals().invalidate();
                myPlugin.myPanel.GetGoals().revalidate();
                return;
            }
        }
        else
        {
            if (myIsInDoubleRowMode)
            {
                myIsInDoubleRowMode = false;
                myPlugin.myPanel.GetGoals().invalidate();
                myPlugin.myPanel.GetGoals().revalidate();
                return;
            }
        }

        int progressWidth = (int)(barWidth * progress);
        int bankedWidth = (int)(barWidth * banked);

        g.setColor(myPlugin.myConfig.completedColor());
        g.fillRect(aStartPosition,0,progressWidth,getHeight());

        g.setColor(myPlugin.myConfig.bankedColor());
        g.fillRect(aStartPosition + progressWidth,0,bankedWidth - progressWidth,getHeight());

        DrawingUtils.DrawText(myPlugin, g, myPrettyId, aStartPosition + 3, 2, false, true);

        if (!progressText.equals(""))
            DrawingUtils.DrawText(myPlugin, g, progressText, getWidth(), getHeight() - 2, true, false);

        g.setColor(myPlugin.myConfig.treeColor());
        g.drawLine(aStartPosition, getHeight() - 1, getWidth(), getHeight() - 1);
        if (myIsInDoubleRowMode)
        {
            g.drawLine(aStartPosition - 1, myPlugin.myConfig.rowHeight(), aStartPosition - 1, getHeight() - 1);
        }
    }

     */
}
