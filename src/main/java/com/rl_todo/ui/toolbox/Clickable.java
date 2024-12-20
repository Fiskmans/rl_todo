package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;


public class Clickable<T extends JComponent> extends JPanel
{
    public T myInner;
    Consumer<T> myConsumer;

    Clickable(T aInner, Consumer<T> aConsumer)
    {
        setLayout(new BorderLayout());
        add(aInner, BorderLayout.CENTER);

        myInner = aInner;
        myConsumer = aConsumer;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getButton() != 1)
                    return;

                myConsumer.accept(aInner);
            }
        });
    }
}
