package com.rl_todo;

import com.rl_todo.methods.Method;
import com.rl_todo.ui.GoalPopup;
import com.rl_todo.ui.Selectable;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemComposition;
import net.runelite.api.Quest;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.api.SpriteID.*;

class GoalMouseListener extends MouseAdapter
{
    private TodoPlugin myPlugin;
    private Goal myOwner;
    public GoalMouseListener(TodoPlugin aPlugin, Goal aOwner)
    {
        myPlugin = aPlugin;
        myOwner = aOwner;
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        if (e.isPopupTrigger())
            doPop(e);
        else
            myOwner.StartDragging();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (e.isPopupTrigger())
            doPop(e);
        else
            myOwner.StopDragging();
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        myOwner.SetHovered(true);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        myOwner.SetHovered(false);
    }

    private void doPop(MouseEvent e)
    {
        GoalPopup menu = new GoalPopup(myPlugin, myOwner);
        menu.show(e.getComponent(), e.getX(), e.getY() + 14);
    }
}

public class Goal extends JComponent implements ProgressTracker
{
    public static final int UNBOUNDED = Integer.MAX_VALUE;

    class SelectedMethod
    {
        Goal myOwner;
        Method myMethod;
        List<Goal> myGoals = new ArrayList<>();

        SelectedMethod(Method aMethod, Goal aOwner, boolean aIsOneOfMultipleMethods)
        {
            myOwner = aOwner;
            myMethod = aMethod;

            if (aIsOneOfMultipleMethods)
            {
                for(Resource resource : myMethod.myRequires)
                    myGoals.add(new Goal(myOwner, myOwner.myPlugin, resource.myId, (int)Math.ceil(resource.myAmount), false));

                for(Resource resource : myMethod.myTakes)
                    myGoals.add(new Goal(myOwner, myOwner.myPlugin, resource.myId, UNBOUNDED, false));
            }
            else
            {
                List<Resource> needed = myMethod.Calculate(myOwner.myPlugin, myOwner.myId, myOwner.myTarget);

                for(Resource resource : needed)
                    myGoals.add(new Goal(myOwner, myOwner.myPlugin, resource.myId, (int)Math.ceil(resource.myAmount), false));
            }
        }

        int mySkipTo = -1;
        SelectedMethod(Method aMethod, Goal aOwner, List<String> aLines, int aStartsAt)
        {
            myOwner = aOwner;
            myMethod = aMethod;

            int at = aStartsAt;

            if (!aLines.get(at).strip().equals("["))
            {
                TodoPlugin.FixableError("Expected a [ on line " + (at + 1), "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));
            }

            at++;

            boolean good = false;
            while (at < aLines.size())
            {
                if (aLines.get(at).strip().equals("]"))
                {
                    good = true;
                    break;
                }

                Goal goal = new Goal(aOwner, aOwner.myPlugin, aLines, at);

                at = goal.mySkipTo;

                myGoals.add(goal);
            }

            if (!good)
            {
                TodoPlugin.FixableError("Expected a ] on line " + (at + 1), "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));
            }

            mySkipTo = at;
        }

        void UpdateTargets()
        {
            List<Resource> resources = myMethod.Calculate(myOwner.myPlugin, myOwner.myId, myOwner.myTarget);
            for(Resource resource : resources)
            {
                for (Goal goal : myGoals)
                {
                    if (goal.myId.equals(resource.myId))
                    {
                        goal.SetTarget((int)Math.ceil(resource.myAmount), false);
                    }
                }
            }
        }

        int CalculateBanked()
        {
            List<Resource> myAvailableResources = new ArrayList<>();

            for (Goal goal : myGoals)
            {
                goal.RecalculateBanked();
                myAvailableResources.add(new Resource(goal.myId, goal.myProgress + goal.myBanked));
            }

            return myMethod.CalculateAvailable(myOwner.myPlugin, myAvailableResources, myOwner.myId, myOwner.myTarget);
        }
    }


    public Goal myParent = null;
    public List<SelectedMethod> myMethods = new ArrayList<>();
    public boolean IsRoot() { return myParent == null; }
    public boolean HasMethod() { return myMethods.size() > 0; }
    public String GetMethodName()
    {
        switch (myMethods.size())
        {
            case 0:
                return "None";
            case 1:
                return myMethods.get(0).myMethod.myName;
            default:
                return "Multiple";
        }
    }
    public boolean IsUsingMethod(Method aMethod)
    {
        return !myMethods.stream().filter((SelectedMethod m) -> m.myMethod == aMethod).findAny().isEmpty();
    }

    private boolean myIsInDoubleRowMode = false;
    private static Goal ourDragging = null;

    private List<Goal> myChildren = new ArrayList<>();
    private boolean myIsLast;

    protected BufferedImage myIcon;
    private boolean myIsHovered = false;
    public void SetHovered(boolean aState)
    {
        if (myIsHovered != aState)
        {
            repaint();
            for (Goal goal : myChildren) {
                goal.repaint();
            }
        }

        myIsHovered = aState;
    }

    protected int myTarget = 1;
    protected int myProgress = 0;
    protected int myBanked = 0;
    public int GetTarget() { return  myTarget; }
    public int GetProgress() { return myProgress; }
    public int GetBanked() { return myBanked; }

    protected TodoPlugin myPlugin;

    private String myId;
    private String myPrettyId;
    public String GetId() { return myId; };
    public String GetPrettyId() { return myPrettyId; };

    private GoalCollection myOwner;

    public void SetOwner(GoalCollection aCollection)
    {
        myOwner = aCollection;
    }
    public void Serialize(List<String> aOutput, int aDepth)
    {
        String indent = " ".repeat(aDepth);

        aOutput.add(indent + "{");
        aOutput.add(indent + " id " + myId);

        switch (myTarget)
        {
            case 1:
                break;
            case UNBOUNDED:
                aOutput.add(indent + " target unbounded");
                break;
            default:
                aOutput.add(indent + " target " + myTarget);
        }

        for (SelectedMethod selected : myMethods)
        {
            aOutput.add(indent + " method " + selected.myMethod.myName);
            aOutput.add(indent + " [");

            for (Goal goal : selected.myGoals)
                goal.Serialize(aOutput, aDepth + 1);

            aOutput.add(indent + " ]");
        }
        aOutput.add(indent + "}");
    }

    @Override
    public String toString() {
        return myPrettyId + ": " + myProgress + " + (" + myBanked + ")/" + myTarget;
    }

    public Goal(Goal aParent, TodoPlugin aPlugin, String aId, int aCount, boolean aIsUserAction)
    {
        myPlugin = aPlugin;
        myParent = aParent;
        myIsLast = true;

        myId = aId;
        myPrettyId = aId;
        if (aCount == UNBOUNDED)
        {
            myTarget = aCount;
        }
        else
        {
            myTarget = Utils.Clamp(aCount, 1, MaxTarget());
        }

        if (Objects.isNull(myParent))
        {
            myPlugin.myPanel.GetGoals().AddGoal(this);
        }
        else
        {
            myParent.AddChild(this);
        }

        Setup(aIsUserAction);
    }

    public int mySkipTo = -1; // pretend this is an out parameter of the function
    public Goal(Goal aParent, TodoPlugin aPlugin, List<String> aLines, int aReadFrom)
    {
        myParent = aParent;
        myPlugin = aPlugin;
        myIsLast = true;

        myId = "ERROR";
        myPrettyId = "ERROR";
        myTarget = 1;

        int consumed = aReadFrom;

        String opener = aLines.get(consumed).strip();
        consumed++;

        if (!opener.equals("{"))
        {
            TodoPlugin.FixableError("Expected a { on line " + consumed + " was: " + opener, "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));
            mySkipTo = consumed + 1;
            return;
        }

        Parsing:
        for (; consumed < aLines.size(); consumed++)
        {
            String line = aLines.get(consumed).strip();

            if (line.isEmpty())
                continue;

            String[] parts = line.split(" ");
            TodoPlugin.debug(parts, 0);

            // since string is not empty there is at least one
            switch (parts[0])
            {
                case "id":
                    if (parts.length < 2)
                    {
                        TodoPlugin.FixableError("[id] on line " + (consumed + 1) + " does not have a value", "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));
                        continue;
                    }
                    myId = parts[1];
                    myPrettyId = parts[1];

                    myTarget = Utils.Clamp(myTarget, 1, MaxTarget());

                    continue;
                case "target":
                    if (parts.length < 2)
                    {
                        TodoPlugin.FixableError("[target] on line " + (consumed + 1) + " does not have a value", "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));
                        continue;
                    }

                    if (parts[1].equals("unbounded"))
                    {
                        myTarget = UNBOUNDED;
                        continue;
                    }

                    try
                    {
                        myTarget = Utils.Clamp(Integer.parseInt(parts[1]), 1, MaxTarget());
                    }
                    catch (Exception e)
                    {
                        TodoPlugin.FixableError("[target] on line " + (consumed + 1) + " is not a number", "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));
                        continue;
                    }
                    continue;
                case "method":
                case "}":
                    break Parsing;
                default:
                    TodoPlugin.FixableError("Unkown key on line " + (consumed + 1) + ": " + parts[0], "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));
                    continue;
            }
        }

        if (Objects.isNull(myParent))
        {
            myPlugin.myPanel.GetGoals().AddGoal(this);
        }
        else
        {
            myParent.AddChild(this);
        }

        Setup(false);

        final String methodKey = "method";

        boolean good = false;

        for (; consumed < aLines.size(); consumed++)
        {
            String line = aLines.get(consumed).strip();

            if (line.isEmpty())
                continue;

            if (line.equals("}"))
            {
                good = true;
                break;
            }

            if (!line.startsWith(methodKey))
            {
                TodoPlugin.FixableError("Expected 'method' or '}' on line " + (consumed + 1) + " was: " + line, "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));
                continue;
            }

            String methodName = line.substring(methodKey.length()).strip();
            Method method = myPlugin.myMethodManager.GetMethodByName(methodName);

            if (Objects.isNull(method))
            {
                TodoPlugin.FixableError("The method on line " + (consumed + 1) + ": [" + methodName + "] does not exist", "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));

                int depth = 0;
                SkippingMethodContent:
                for (; consumed < aLines.size(); consumed++)
                {
                    switch (aLines.get(consumed).strip())
                    {
                        case "[":
                        case "{":
                            depth++;
                            break;
                        case "]":
                        case "}":
                            depth--;
                            if (depth == 0)
                                break SkippingMethodContent;
                            break;
                    }
                }

                continue;
            }

            SelectedMethod selectable = new SelectedMethod(method, this, aLines, consumed + 1);
            consumed = selectable.mySkipTo;
            myMethods.add(selectable);
        }

        if (!good)
            TodoPlugin.FixableError("Expected a } on line " + (consumed + 1), "Reset goals", () -> TodoPlugin.myGlobalInstance.myConfig.setGoals(""));

        mySkipTo = consumed + 1;
        Setup(false);
    }

    public int MaxTarget()
    {
        if (myId.startsWith("quest."))
            return 1;

        if (myId.startsWith("level."))
            return 99;

        if (myId.startsWith("xp."))
            return 2 * 1000 * 1000 * 1000;

        return Integer.MAX_VALUE;
    }

    public void SetTarget(int aValue, boolean aIsUserAction)
    {
        if (aValue == myTarget)
            return;

        myTarget = aValue;

        RefreshChildrenTarget();
        RecalculateBanked();

        repaint();

        if (aIsUserAction)
        {
            myOwner.SaveConfig();
        }
    }

    private void Setup(boolean aIsUserAction)
    {
        addMouseListener(new GoalMouseListener(myPlugin, this));

        SetupIconAndPrettyId();

        RecalculateProgress();
        RecalculateBanked();

        if (aIsUserAction)
        {
            myOwner.SaveConfig();
        }
    }

    private void SetupIconAndPrettyId()
    {
        int index =  myId.indexOf('.');
        if (index == -1)
            return;

        String type = myId.substring(0, index);
        String part = myId.substring(index + 1);
        switch (type)
        {
            case "item":
                try
                {
                    int itemId = Integer.parseInt(part);
                    myPlugin.myClientThread.invokeLater(()->
                    {
                        ItemComposition itemComposition = myPlugin.myItemManager.getItemComposition(itemId);

                        AsyncBufferedImage icon = myPlugin.myItemManager.getImage(itemId, myTarget, itemComposition.isStackable());
                        myIcon = icon;
                        icon.onLoaded(() -> { repaint(); });

                        myPrettyId = itemComposition.getMembersName();
                        repaint();
                    });
                }
                catch (final NumberFormatException e)
                {
                    myIcon = ImageUtil.loadImageResource(TodoPlugin.class, "/Icon_16x16.png");
                }
                break;
            case "quest":
                for(Quest q : Quest.values())
                {
                    if (Integer.toString(q.getId()).equals(part))
                    {
                        myPrettyId = q.getName();
                    }
                }
                myPlugin.myClientThread.invokeLater(() ->
                {
                    myIcon = myPlugin.mySpriteManager.getSprite(TAB_QUESTS, 0);
                    repaint();
                });
                break;
            case "xp":
                myPrettyId = part.substring(0,1).toUpperCase() + part.substring(1) + " xp";
                myIcon = ImageUtil.loadImageResource(myPlugin.myClient.getClass(), "/skill_icons/" + part.toLowerCase() + ".png");
                break;
            case "level":
                myPrettyId = part.substring(0,1).toUpperCase() + part.substring(1).toLowerCase();
                myIcon = ImageUtil.loadImageResource(myPlugin.myClient.getClass(), "/skill_icons/" + part.toLowerCase() + ".png");
                break;
            case "nmz":
                myPrettyId = "NMZ points";
                myPlugin.myClientThread.invokeLater(() ->
                {
                    myIcon = myPlugin.mySpriteManager.getSprite(TAB_QUESTS_RED_MINIGAMES, 0);
                    repaint();
                });
                break;
        }
    }

    public void StartDragging()
    {
        /*
        ourDragging = this;

        if (!Objects.isNull(myParent))
        {
            for (Goal goal : myParent.myChildren)
            {
                goal.repaint();
            }
        }
        else
        {
            myPlugin.myPanel.GetGoals().RepaintRoots();
        }
        */
    }

    public void StopDragging()
    {
        /*
        if (Objects.isNull(ourDragging))
            return;

        Goal dropped = ourDragging;
        ourDragging = null;

        if (!Objects.isNull(dropped.myParent))
        {
            for (Goal goal : dropped.myParent.myChildren)
            {
                goal.repaint();
            }
        }
        else
        {
            myPlugin.myPanel.GetGoals().RepaintRoots();
        }


        if (myParent != dropped)
            return;

        if (ourDragging.IsRoot())
        {
            // TODO shuffle places at root
            return;
        }

        // TODO shuffle siblings
        */
    }

    public List<Method> GetMethodCandidates()
    {
        List<Method> blacklist = new ArrayList<>();

        Goal at = myParent;
        while(!Objects.isNull(at))
        {
            blacklist.addAll(at.myMethods.stream().map((SelectedMethod m) -> m.myMethod).collect(Collectors.toList()));

            at = at.myParent;
        }

        List<Method> candidates = myPlugin.myMethodManager.GetAvailableMethods(myId);

        return candidates.stream().filter((Method aMethod) -> !blacklist.contains(aMethod)).collect(Collectors.toList());
    }

    public void UnsetMethods(boolean aIsUserAction)
    {
        RemoveAllChildren();
        myMethods.clear();

        if (aIsUserAction)
            TodoPlugin.myGlobalInstance.myPanel.SaveConfig();
    }

    public void SetMethod(Method aMethod, boolean aIsUserAction)
    {
        UnsetMethods(false);
        SelectedMethod selected = new SelectedMethod(aMethod, this, false);

        myMethods.add(selected);
        myChildren.addAll(selected.myGoals);

        if (aIsUserAction)
            TodoPlugin.myGlobalInstance.myPanel.SaveConfig();
    }

    public void RemoveMethod(Method aMethod, boolean aIsUserAction)
    {
        Optional<SelectedMethod> selected = myMethods.stream().filter((SelectedMethod m) -> m.myMethod == aMethod).findAny();

        assert !selected.isEmpty();

        myMethods.remove(selected);

        for (Goal goal : selected.get().myGoals)
            myOwner.RemoveGoal(goal);

        myChildren.removeAll(selected.get().myGoals);

        if (aIsUserAction)
            TodoPlugin.myGlobalInstance.myPanel.SaveConfig();
    }

    public void AddMethod(Method aMethod, boolean aIsUserAction)
    {
        SelectedMethod selected = new SelectedMethod(aMethod, this, true);

        myMethods.add(selected);
        myChildren.addAll(selected.myGoals);

        if (aIsUserAction)
            TodoPlugin.myGlobalInstance.myPanel.SaveConfig();
    }

    public void OnRemoved()
    {
        RemoveAllChildren();
    }

    private void RemoveAllChildren()
    {
        for (Goal child : myChildren)
            myOwner.RemoveGoal(child);

        myChildren.clear();
    }

    @Override
    public Dimension getPreferredSize()
    {
        if (myIsInDoubleRowMode)
        {
            return new Dimension(0, myPlugin.myConfig.rowHeight() * 2);
        }
        else
        {
            return new Dimension(0, myPlugin.myConfig.rowHeight());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        paintBackground(g);
        int barStart = paintTree(g);
        if (myTarget == UNBOUNDED)
        {
            // TODO paint unbounded goals
        }
        else
        {
            paintProgressBar(g, barStart);
        }
    }

    private void paintBackground(Graphics g)
    {
        if (myIsHovered)
        {
            g.setColor(new Color(60, 60, 60));
        }
        else if (!Objects.isNull(myParent) && myParent.myIsHovered)
        {
            g.setColor(new Color(50, 50, 50));
        }
        else
        {
            g.setColor(new Color(43, 43, 44));
        }

        if (!Objects.isNull(ourDragging))
        {
            if (ourDragging == this)
            {
                g.setColor(new Color(60, 255, 60));
            }
            else if (myParent == ourDragging.myParent)
            {
                if (myIsHovered)
                {
                    g.setColor(new Color(60, 150, 150));
                }
                else
                {
                    g.setColor(new Color(60, 100, 100));
                }
            }
        }

        g.fillRect(0,0,getWidth(),getHeight());
    }

    private int paintTree(Graphics g)
    {
        Stack<Goal> hierarcy = new Stack<>();

        Goal at = myParent;
        while (!Objects.isNull(at))
        {
            hierarcy.push(at);
            at = at.myParent;
        }

        g.setColor(myPlugin.myConfig.treeColor());

        int depth = 0;

        if(!hierarcy.isEmpty())
        {
            hierarcy.pop(); // the root node does not have any branches

            while(!hierarcy.isEmpty())
            {
                Goal goal = hierarcy.pop();

                int xMiddle = depth + (int)Math.round(myPlugin.myConfig.indent() * 0.5);
                if (!goal.myIsLast)
                    g.drawLine(xMiddle, 0, xMiddle, getHeight());

                depth += myPlugin.myConfig.indent();
            }
        }

        if(!Objects.isNull(myParent))
        {
            int xMiddle = depth + (int)Math.round(myPlugin.myConfig.indent() * 0.5);
            int xRight = depth + myPlugin.myConfig.indent();
            int yMiddle = (int)Math.round(myPlugin.myConfig.rowHeight() * 0.5);
            int offset = (int)Math.round(Math.min(myPlugin.myConfig.indent(), myPlugin.myConfig.rowHeight()) * 0.2);

            if (myIsLast)
            {
                g.drawLine(xMiddle, 0, xMiddle, yMiddle - offset);
                g.drawLine(xMiddle, yMiddle - offset, xMiddle + offset, yMiddle);
                g.drawLine(xMiddle + offset, yMiddle, xRight, yMiddle);
            }
            else
            {
                g.drawLine(xMiddle, 0, xMiddle, getHeight());
                g.drawLine(xMiddle, yMiddle, xRight, yMiddle);
            }
            depth += myPlugin.myConfig.indent();
        }


        int iconStart = depth;

        if (myIcon != null)
            g.drawImage(myIcon, iconStart + 1,1,myPlugin.myConfig.rowHeight() - 2,myPlugin.myConfig.rowHeight() - 2, null);

        //image border
        g.drawRect(depth, 0, myPlugin.myConfig.rowHeight() - 1, myPlugin.myConfig.rowHeight() - 1);

        if (myIsInDoubleRowMode && myChildren.size() > 0)
        {
            int childBranch = depth + (int)Math.round(myPlugin.myConfig.indent() * 0.5);

            g.drawLine(childBranch, myPlugin.myConfig.rowHeight(), childBranch, getHeight());
        }

        return depth + myPlugin.myConfig.rowHeight();
    }

    private void paintProgressBar(Graphics g, int aStartPosition)
    {
        float progress = (float)GetProgress() / GetTarget();
        float banked = (float)Math.min(GetProgress() + GetBanked(), GetTarget()) / GetTarget();


        int barWidth = getWidth() - aStartPosition;

        FontMetrics metrics = g.getFontMetrics();
        String progressText = GetProgressText();

        int totalTextWidth = 0;

        {
            char[] chars = new char[myPrettyId.length()];
            myPrettyId.getChars(0,myPrettyId.length(), chars, 0);

            totalTextWidth += metrics.charsWidth(chars,0, chars.length);
        }

        if (myTarget != 1)
        {
            char[] chars = new char[progressText.length()];
            progressText.getChars(0,progressText.length(), chars, 0);
            totalTextWidth += metrics.charsWidth(chars,0, chars.length);
        }

        if (totalTextWidth + 10 > barWidth && !progressText.equals(""))
        {
            if (!myIsInDoubleRowMode)
            {
                myIsInDoubleRowMode = true;
                myPlugin.myPanel.GetGoals().invalidate();
                myPlugin.myPanel.GetGoals().revalidate();
                return;
            }
        }
        else
        {
            if (myIsInDoubleRowMode)
            {
                myIsInDoubleRowMode = false;
                myPlugin.myPanel.GetGoals().invalidate();
                myPlugin.myPanel.GetGoals().revalidate();
                return;
            }
        }

        int progressWidth = (int)(barWidth * progress);
        int bankedWidth = (int)(barWidth * banked);

        g.setColor(myPlugin.myConfig.completedColor());
        g.fillRect(aStartPosition,0,progressWidth,getHeight());

        g.setColor(myPlugin.myConfig.bankedColor());
        g.fillRect(aStartPosition + progressWidth,0,bankedWidth - progressWidth,getHeight());

        DrawingUtils.DrawText(myPlugin, g, myPrettyId, aStartPosition + 3, 2, false, true);

        if (!progressText.equals(""))
            DrawingUtils.DrawText(myPlugin, g, progressText, getWidth(), getHeight() - 2, true, false);

        g.setColor(myPlugin.myConfig.treeColor());
        g.drawLine(aStartPosition, getHeight() - 1, getWidth(), getHeight() - 1);
        if (myIsInDoubleRowMode)
        {
            g.drawLine(aStartPosition - 1, myPlugin.myConfig.rowHeight(), aStartPosition - 1, getHeight() - 1);
        }
    }

    public String GetProgressText()
    {
        String progressText = "";

        if (myTarget != 1)
            progressText = DisplayNum(myProgress) + "/" + DisplayNum(myTarget);

        if (myProgress == myTarget)
            progressText = "Done";

        return progressText;
    }

    private String DisplayNum(int aNumber)
    {
        int num = aNumber;

        if (num > 1000000)
            return String.format("%.2fm", (float)num / 1000000);

        if (num > 1000)
            return String.format("%.2fk", (float)num / 1000);

        return Integer.toString(num);
    }

    private void AddChild(Goal aGoal)
    {
        if (!myChildren.isEmpty())
            aGoal.myIsLast = false; // goals are inserted into the treeview, not appended

        myChildren.add(aGoal);
        if (myOwner != null)
        {
            myPlugin.myPanel.GetGoals().AddSubGoal(this, aGoal);
        }
    }

    @Override
    public void CountUpdated(String aId)
    {
        assert aId.equals(myId);

        RecalculateProgress();
        RefreshChildrenTarget();
    }

    public void RecalculateProgress()
    {
        int aProgress = myPlugin.myProgressManager.GetProgress(myId);
        int clamped = Math.min(aProgress, myTarget);
        if (clamped != myProgress)
        {
            myProgress = clamped;
            CheckCompletion();
            repaint();
            TodoPlugin.debug("Progress on " + toString(), 1);

            if (!Objects.isNull(myParent))
            {
                myParent.RecalculateBanked();
            }
        }
    }

    public void CheckCompletion()
    {
        if(myProgress == myTarget)
        {
            if (isVisible())
            {
                if (!IsRoot())
                {
                    setVisible(false);
                    myPlugin.myPanel.GetGoals().invalidate();
                    myPlugin.myPanel.GetGoals().revalidate();
                }
                if (myPlugin.myConfig.messageOnCompletion())
                {
                    myPlugin.myClientThread.invokeLater(() -> {
                        myPlugin.myClient.addChatMessage(ChatMessageType.GAMEMESSAGE, "Todo", "You completed a goal!", "Todo"); // TODO rephrase and include which goal
                    });

                    // TODO system popup
                }
            }
        }
        else
        {
            if (!isVisible())
            {
                if (!IsRoot())
                {
                    setVisible(true);
                    myPlugin.myPanel.GetGoals().invalidate();
                    myPlugin.myPanel.GetGoals().revalidate();
                }
            }
        }
    }

    public void RefreshChildrenTarget()
    {
        // 0  -> no children
        // 2+ -> all children unbounded, no reason to refresh
        if (myMethods.size() != 1)
            return;

        myMethods.get(0).UpdateTargets();
    }

    public void RecalculateBanked()
    {
        int banked = 0;

        for (SelectedMethod selected : myMethods)
            banked += selected.CalculateBanked();

        if (myBanked != banked)
        {
            myBanked = banked;
            repaint();

            if (!Objects.isNull(myParent))
            {
                myParent.RecalculateBanked();
            }
        }
    }
}
