package com.rl_todo.ui;

import com.rl_todo.DrawingUtils;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ResourceView extends JPanel {

    TodoPlugin myPlugin;
    String myText;
    Float myAmount;
    BufferedImage myIcon;

    public ResourceView(TodoPlugin aPlugin, String aId, Float aAmount)
    {
        setMinimumSize(new Dimension(40, 20));
        setPreferredSize(new Dimension(60, 20));
        setMaximumSize(new Dimension(300, 20));

        String prefix = "";

        if (Math.abs(aAmount - 1) > 0.0001)
            prefix = Utilities.PrettyNumber(aAmount) + " ";

        myPlugin = aPlugin;
        myText = prefix + aId;
        myAmount = aAmount;
        myIcon = aPlugin.myUtilities.myErrorImage;


        final String finalPrefix = prefix;
        aPlugin.myClientThread.invokeLater(() ->
        {
            aPlugin.myUtilities.PrettifyIDCompact(aId).ifPresent((prettyId) -> {
                myText = finalPrefix + prettyId;
                repaint();
            });

            myIcon = aPlugin.myUtilities.IconFromID(aId, aAmount.intValue());
            repaint();
        });


    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.drawImage(myIcon, 0,0, 20, 20, null);

        DrawingUtils.DrawText(myPlugin,g, myText, 23, 2, false, true, null);

        Dimension prefferedSize = new Dimension(DrawingUtils.MeasureText(g, myText).width + 26, 20);

        if (!getPreferredSize().equals(prefferedSize))
        {
            setPreferredSize(prefferedSize);
        }

    }

}
