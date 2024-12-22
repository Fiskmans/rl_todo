package com.rl_todo.ui;

import com.rl_todo.utils.DrawingUtils;
import com.rl_todo.TodoPlugin;
import com.rl_todo.utils.Awaitable;
import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ResourceView extends JPanel {

    TodoPlugin myPlugin;
    String myText;
    Float myAmount;
    BufferedImage myIcon;

    Awaitable myAwaitable;

    public ResourceView(TodoPlugin aPlugin, String aId, Float aAmount)
    {
        myAwaitable = new Awaitable();

        setMinimumSize(new Dimension(210, 18));
        setPreferredSize(new Dimension(210, 18));
        setMaximumSize(new Dimension(210, 18));

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

            if (myIcon instanceof AsyncBufferedImage)
                ((AsyncBufferedImage)myIcon).onLoaded(() -> myAwaitable.SetDone());
            else
                myAwaitable.SetDone();
        });
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(), getHeight());

        g.drawImage(myIcon, 1,1, 17, 17, null);

        DrawingUtils.DrawText(myPlugin,g, myText, 20, 17, false, false, null);
    }

    public Awaitable Await()
    {
        return myAwaitable;
    }
}
