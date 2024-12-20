package com.rl_todo.ui.toolbox;

import com.rl_todo.utils.DrawingUtils;
import com.rl_todo.TodoPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ClickableText extends Clickable<Stretchable> implements TreeNodeItem
{
    TodoPlugin myPlugin;
    String myText;
    boolean myIsUnderlined = false;
    public Consumer<String> myOnClick;

    public ClickableText(TodoPlugin aPlugin, String aText, Consumer<String> aOnClick, boolean aUnderlined)
    {
        super(new Stretchable(), null);

        myOnClick = aOnClick;
        super.myConsumer = (panel) -> myOnClick.accept(aText);
        myPlugin = aPlugin;
        myText = aText;
        myIsUnderlined = true;

        setBorder(new EmptyBorder(1,1,1,1));

        setMaximumSize(new Dimension(400, 20));
        setPreferredSize(new Dimension(200, 20));
        setMinimumSize(new Dimension(200, 20));

        myInner.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                setBorder(new LineBorder(Color.gray));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                setBorder(new EmptyBorder(1,1,1,1));
                repaint();
            }
        });
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0,0, getWidth(), getHeight());

        Dimension textSize = DrawingUtils.DrawText(myPlugin, g, myText, 3, 1, false ,true, null);

        if (myIsUnderlined)
        {
            g.setColor(new Color(130,130,158));
            g.drawLine(3, 1 + textSize.height, 3 + textSize.width, 1 + textSize.height);
        }
    }

    @Override
    public int GetAnchorDepth() {
        return 10;
    }
}
