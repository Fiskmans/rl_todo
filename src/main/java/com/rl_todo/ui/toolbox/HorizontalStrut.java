package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;

public class HorizontalStrut extends JComponent {

    public HorizontalStrut(int aSize)
    {
        setPreferredSize(new Dimension(aSize, 0));
        setMinimumSize(new Dimension(aSize, 0));
        setMaximumSize(new Dimension(aSize, 0));

        setBackground(Color.green);
    }
}
