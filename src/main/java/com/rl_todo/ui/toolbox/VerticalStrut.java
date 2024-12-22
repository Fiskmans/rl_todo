package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;

public class VerticalStrut extends JComponent {

    public VerticalStrut(int aSize)
    {
        setPreferredSize(new Dimension(0, aSize));
        setMinimumSize(new Dimension(0, aSize));
        setMaximumSize(new Dimension(0, aSize));

        setBackground(Color.green);
    }
}
