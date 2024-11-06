package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.methods.Method;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MethodViewerPopup extends JPopupMenu
{
    float myAlpha;
    Timer myAnimationTimer;

    public MethodViewerPopup(TodoPlugin aPlugin, Method aMethod, JComponent aRelativeTo, int x, int y)
    {
        myAlpha = -0.32f;
        setLayout(new GridBagLayout());

        MethodViewer content = new MethodViewer(aPlugin, aMethod);

        add(content);

        show(aRelativeTo, x, y);

        myAnimationTimer = new Timer(5, e -> Animate());

        myAnimationTimer.setRepeats(true);
        myAnimationTimer.start();
    }

    private void Animate()
    {
        if (myAlpha < 1.f)
            myAlpha += 0.10f;

        if (myAlpha > 1.f) {
            myAlpha = 1.f;
            myAnimationTimer.stop();
        }

        repaint();
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.SrcOver.derive(myAlpha < 0.f ? 0.f : myAlpha));
        super.paint(g2d);
        g2d.dispose();
    }
}
