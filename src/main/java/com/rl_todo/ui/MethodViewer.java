package com.rl_todo.ui;

import com.rl_todo.TodoPlugin;
import com.rl_todo.methods.Method;
import com.rl_todo.ui.toolbox.Arrow;
import com.rl_todo.ui.toolbox.Stretchable;
import com.rl_todo.utils.AwaitUtils;
import com.rl_todo.utils.Awaitable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.stream.Stream;

public class MethodViewer extends JPanel {

    TodoPlugin myPlugin;

    public MethodViewer(TodoPlugin aPlugin)
    {
        myPlugin = aPlugin;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    public void SetMethod(Method aMethod)
    {
        final ResourcePoolView requires = aMethod.myRequires.IsEmpty()  ? null : new ResourcePoolView(myPlugin, aMethod.myRequires, "Requires");
        final ResourcePoolView takes    = aMethod.myTakes.IsEmpty()     ? null : new ResourcePoolView(myPlugin, aMethod.myTakes,    "Takes");
        final ResourcePoolView makes    = aMethod.myMakes.IsEmpty()     ? null : new ResourcePoolView(myPlugin, aMethod.myMakes,    "Makes");

        AwaitUtils.WaitAll(
            Stream.of(requires, takes, makes)
                .filter(Objects::nonNull)
                .map(ResourcePoolView::Await))
            .WhenDone((sender) ->
                SwingUtilities.invokeLater(() ->
                {
                    removeAll();

                                                        add(new WrappingText(myPlugin, aMethod.GetName(), 230));
                    if (requires != null)               add(requires);
                    if (takes != null)                  add(takes);
                    if (takes != null && makes != null) add(new Arrow());
                    if (makes != null)                  add(makes);
                                                        add(new Stretchable());

                    repaint();
                    revalidate();
                }));
    }
}
