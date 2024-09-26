package com.rl_todo.ui;

import com.rl_todo.DrawingUtils;
import com.rl_todo.Goal;
import com.rl_todo.GoalSubscriber;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class GoalUI extends JPanel implements GoalSubscriber {

    TodoPlugin myPlugin;
    Goal myGoal;

    BufferedImage myIcon;
    String myPrettyId;
    boolean myIsHovered = false;

    static int IconSize = 18;
    static int Height = 20;

    GoalUI(TodoPlugin aPlugin, Goal aGoal)
    {
        myPlugin = aPlugin;
        myGoal = aGoal;

        myGoal.AddSubscriber(this);

        myIcon = myPlugin.myUtilities.myErrorImage;
        myPrettyId = myGoal.GetId();

        myPlugin.myClientThread.invokeLater(() ->
        {
            myIcon = myPlugin.myUtilities.IconFromID(myGoal.GetId(), myGoal.GetTarget());
            myPlugin.myUtilities.PrettifyID(myGoal.GetId()).ifPresent((prettyId) -> myPrettyId = prettyId);
            repaint();
        });

        setMaximumSize(new Dimension(2000, Height));
        setPreferredSize(new Dimension(IconSize, Height));
        setMinimumSize(new Dimension(IconSize, Height));


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
            public void mouseEntered(MouseEvent e) {
                myIsHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                myIsHovered = false;
                repaint();
            }

            private void OpenPopup(MouseEvent e)
            {
                GoalPopup menu = new GoalPopup(myPlugin, myGoal);
                menu.show(e.getComponent(), e.getX(), e.getY() + 14);
            }
        });
    }

    @Override
    public void OnSubGoalAdded(Goal aSubGoal) {
    }

    @Override
    public void OnSubGoalsCleared() {
    }

    @Override
    public void OnTargetChanged() {
        repaint();
    }

    @Override
    public void OnBankedChanged() {
        repaint();
    }

    @Override
    public void OnProgressChanged() {
        repaint();
    }

    @Override
    public void OnMethodChanged() {

    }

    @Override
    public void OnCompleted() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();

        //if (width <= 60)
        //    PaintCompact(g);
        //else
            PaintNormal(g);
    }

    void PaintCompact(Graphics g)
    {
        // TODO: draw a compact rotation progressbar
    }

    void PaintNormal(Graphics g)
    {
        g.setColor(Utilities.BlendColors(Color.DARK_GRAY, Color.WHITE, myIsHovered ? 0.2f : 0.0f));
        g.fillRect(0, 0, getWidth(), getHeight());

        int bankedWidth = (int)Math.floor(myGoal.GetBankedFraction() * getWidth());

        g.setColor(Utilities.BlendColors(myPlugin.myConfig.bankedColor(), Color.WHITE, myIsHovered ? 0.2f : 0.0f));
        g.fillRect(0,0, bankedWidth, getHeight());

        int progressWidth = (int)Math.floor(myGoal.GetProgressFraction() * getWidth());

        g.setColor(Utilities.BlendColors(myPlugin.myConfig.completedColor(), Color.WHITE, myIsHovered ? 0.2f : 0.0f));
        g.fillRect(0,0, progressWidth, getHeight());


        g.setColor(Color.BLACK);
        g.fillRect(0,0,IconSize, IconSize);
        g.drawImage(myIcon, 2, 2, IconSize - 2, IconSize - 2, null);
        g.setColor(myPlugin.myConfig.treeColor());
        g.drawRect(0,0,IconSize - 1, IconSize - 1);

        DrawingUtils.DrawText(myPlugin, g, myPrettyId, IconSize + 3, 2, false, true, null);

        myPlugin.myUtilities.ProgressText(myGoal.GetProgress(), myGoal.GetTarget())
            .ifPresent((progressText) ->
                DrawingUtils.DrawText(myPlugin, g, progressText, getWidth() - 2, getHeight() - 2, true, false, null));
    }

}
