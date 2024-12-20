package com.rl_todo.ui.toolbox;

import javax.swing.*;
import java.awt.*;

public class Stretchable extends JPanel {
    public Stretchable()
    {
        setMinimumSize(new Dimension(0,0));
        setPreferredSize(new Dimension(0,0));
        setMaximumSize(new Dimension(10000,10000));
    }
}
