package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;


public class ClickDecorator<T extends JComponent> extends JPanel
{
    public T myInner;
    Consumer<T> myConsumer;

    ClickDecorator(T aInner, Consumer<T> aConsumer)
    {
        setLayout(new BorderLayout());
        add(aInner, BorderLayout.CENTER);

        myInner = aInner;
        myConsumer = aConsumer;

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (e.getButton() != 1)
                    return;

                myConsumer.accept(aInner);
            }
        };

        myInner.addMouseListener(adapter);
    }

    @Override
    public Dimension getMinimumSize() { return myInner.getMinimumSize(); }
    @Override
    public Dimension getPreferredSize() { return myInner.getPreferredSize(); }
    @Override
    public Dimension getMaximumSize() { return myInner.getMaximumSize(); }
}
