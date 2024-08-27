package com.rl_todo;

import com.rl_todo.methods.Method;
import net.runelite.api.ChatMessageType;

import java.util.*;
import java.util.List;

public class Goal implements ProgressTracker
{
    protected TodoPlugin myPlugin;
    final private String myId;
    public String GetId() { return myId; };
    final private boolean myIsRoot;

    private final List<GoalSubscriber> mySubscribers;
    private final List<Goal> myChildren;

    private Method myMethod;

    private int myTarget = 1;
    private int myProgress = 0;
    private int myBanked = 0;
    public int GetTarget() { return  myTarget; }
    public int GetProgress() { return myProgress; }
    public int GetBanked() { return myBanked; }

    public float GetProgressFraction() { return (float)myProgress / (float)myTarget;}
    public float GetBankedFraction() { return (float)myBanked / (float)myTarget;}

    public boolean IsDone() { return myProgress == myTarget; }

    public boolean HasMethod() { return myMethod != null; }
    public Optional<String> GetMethodName()
    {
        if (myMethod == null)
            return Optional.empty();

        return Optional.of(myMethod.myName);
    }

    public boolean IsRoot() { return myIsRoot; }
    public boolean CanSetTarget()
    {
        return myIsRoot && MaxTarget() > 1;
    }

    public Goal(TodoPlugin aPlugin, String aId, int aTarget, boolean aIsRoot, List<GoalSubscriber> aInitialSubscribers)
    {
        myChildren = new ArrayList<>();
        mySubscribers = new ArrayList<>();

        if (aInitialSubscribers != null)
           mySubscribers.addAll(aInitialSubscribers);

        myIsRoot = aIsRoot;
        myTarget = aTarget;
        myPlugin = aPlugin;
        myId = aId;
        myMethod = null;

        Setup();
    }

    public void AddSubscriber(GoalSubscriber aSubscriber)
    {
        mySubscribers.add(aSubscriber);
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

    public boolean SetTarget(int aValue)
    {
        if (aValue == myTarget)
            return true;

        if (aValue > MaxTarget())
            return  false;

        myTarget = aValue;
        RecalculateBanked();

        mySubscribers.forEach(GoalSubscriber::OnTargetChanged);

        return false;
    }

    private void Setup()
    {
        RecalculateProgress();
        RecalculateBanked();
    }

    public void UnsetMethod()
    {
        myMethod = null;
        myChildren.clear();
    }

    public void SetMethod(Method aMethod)
    {
        UnsetMethod();

        myMethod = aMethod;

        myMethod.CalculateNeeded(myPlugin, myId, myTarget)
                .All()
                .stream()
                .filter((kvPair) -> kvPair.getValue() > 0)
                .forEach((kvPair) -> AddSubGoal(kvPair.getKey(), (int)Math.ceil(kvPair.getValue())));

        mySubscribers.forEach(GoalSubscriber::OnMethodChanged);

        //TODO reimplement
    }

    private void AddSubGoal(Goal aGoal)
    {
        myChildren.add(aGoal);

        mySubscribers.forEach((subscriber) -> subscriber.OnSubGoalAdded(aGoal));
    }

    private void AddSubGoal(String aId, int aTarget)
    {
        List<GoalSubscriber> subscribers = new ArrayList<>();

        subscribers.add(new GoalSubscriber() {
            @Override
            public void OnSubGoalAdded(Goal aSubGoal) {}

            @Override
            public void OnTargetChanged() {}

            @Override
            public void OnBankedChanged() {}

            @Override
            public void OnProgressChanged() {
                RecalculateBanked();
            }

            @Override
            public void OnMethodChanged() {}

            @Override
            public void OnCompleted() {}
        });

        AddSubGoal(new Goal(myPlugin, aId, aTarget, false, subscribers));
    }

    @Override
    public void CountUpdated(String aId)
    {
        assert aId.equals(myId);

        RecalculateProgress();
    }

    public void RecalculateProgress()
    {
        int aProgress = myPlugin.myProgressManager.GetProgress(myId);
        int clamped = Math.min(aProgress, myTarget);
        if (clamped != myProgress)
        {
            myProgress = clamped;
            CheckCompletion();

            TodoPlugin.debug("Progress on " + toString(), 1);

            mySubscribers.forEach(GoalSubscriber::OnProgressChanged);
        }
    }

    public void CheckCompletion()
    {
        if(myProgress >= myTarget)
        {
            mySubscribers.forEach(GoalSubscriber::OnCompleted);

            // TODO: latch something so this doesn't retrigger

            if (myPlugin.myConfig.messageOnCompletion())
            {
                myPlugin.myClientThread.invokeLater(() -> {
                    myPlugin.myClient.addChatMessage(ChatMessageType.GAMEMESSAGE, "Todo", "You completed a goal!", "Todo"); // TODO rephrase and include which goal
                });

                // TODO system popup
            }
        }
    }

    public void RecalculateBanked()
    {
        int banked = 0;

        if (HasMethod())
            banked = (int)Math.floor(myMethod.CalculateAvailable(myPlugin.myProgressManager, myId, myTarget).GetSpecific(myId));

        if (myBanked != banked)
        {
            myBanked = banked;
            mySubscribers.forEach(GoalSubscriber::OnBankedChanged);
        }
    }

    public void Serialize(List<String> aOutput)
    {
        String indent = "";

        aOutput.add(indent + "{");
        aOutput.add(indent + " id " + myId);

        if (myTarget != 1)
        {
            aOutput.add(indent + " target " + myTarget);
        }
        aOutput.add(indent + "}");
    }

    @Override
    public String toString() {
        return myId + ": " + myProgress + " + (" + myBanked + ")/" + myTarget;
    }
}
