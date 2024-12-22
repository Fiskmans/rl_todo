package com.rl_todo;

import com.rl_todo.methods.Method;
import com.rl_todo.serialization.SerializableGoal;
import com.rl_todo.serialization.SerializableRecursiveMethod;
import net.runelite.api.ChatMessageType;

import java.util.*;
import java.util.List;

public class Goal implements ProgressTracker
{
    protected TodoPlugin myPlugin;
    final private String myId;
    public String GetId() { return myId; }
    final private boolean myIsRoot;

    private final List<GoalSubscriber> mySubscribers;
    private final List<Goal> myChildren;

    public List<Goal> GetChildren() { return myChildren; }

    private Method myMethod;

    private int myTarget;
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

    public void Remove()
    {
        mySubscribers.forEach(GoalSubscriber::OnRemove);
    }

    private void SetChildMethodFromSerialized(SerializableRecursiveMethod aSerializedMethod)
    {
        assert !myChildren.isEmpty();
        assert HasMethod();

        for (Goal child : myChildren) {

            SerializableRecursiveMethod childMethod;

            if (aSerializedMethod.takes != null && aSerializedMethod.takes.containsKey(child.myId))
            {
                childMethod = aSerializedMethod.takes.get(child.myId);
            }
            else
            {
                assert aSerializedMethod.requires != null;
                assert aSerializedMethod.requires.containsKey(child.myId);

                childMethod = aSerializedMethod.requires.get(child.myId);
            }

            assert childMethod != null;


            if (childMethod.IsFullMethod())
            {
                Method.FromSerialized(myPlugin, childMethod, child.myId).ifPresentOrElse(
                    (method) ->
                    {
                        child.SetMethod(method);
                        child.SetChildMethodFromSerialized(childMethod);
                    },
                    () -> TodoPlugin.IgnorableError("Failed to load a method"));
            }
        }
    }

    public static Goal FromSerialized(TodoPlugin aPlugin, SerializableGoal aSerialized)
    {
        assert aSerialized.IsValid();

        Goal out = new Goal(aPlugin, aSerialized.id, aSerialized.target, true, null);

        if (aSerialized.from != null && aSerialized.from.IsFullMethod())
        {
            Method.FromSerialized(aPlugin, aSerialized.from, out.myId).ifPresentOrElse(
                (method) ->
                {
                    out.SetMethod(method);
                    out.SetChildMethodFromSerialized(aSerialized.from);
                },
                () -> TodoPlugin.IgnorableError("Failed to load a method")
            );
        }

        return out;
    }

    public void FillInMethod(SerializableRecursiveMethod aMethod)
    {
        if (!HasMethod())
            return;

        myMethod.SerializeInto(aMethod, myId);

        if (aMethod.takes != null)
        {
            aMethod.takes.forEach((id, subMethod) ->
                myChildren.forEach((child) ->
                {
                    if (child.myId.equals(id))
                        child.FillInMethod(subMethod);
                }));
        }

        if (aMethod.requires != null)
        {
            aMethod.requires.forEach((id, subMethod) ->
                myChildren.forEach((child) ->
                {
                    if (child.myId.equals(id))
                        child.FillInMethod(subMethod);
                }));
        }
    }

    public SerializableGoal Serialize()
    {
        assert IsRoot();

        SerializableGoal out = new SerializableGoal();

        out.id = myId;
        out.target = myTarget;

        if (HasMethod())
        {
            out.from = myMethod.SerializeSparse(myId);
            FillInMethod(out.from);
        }

        return out;
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

    public void SetTarget(int aValue)
    {
        int value = aValue;
        if (aValue == myTarget)
            return;

        if (value > MaxTarget())
            value = MaxTarget();

        if (value < 1)
            value = 1;

        myTarget = value;
        RecalculateBanked();

        if (HasMethod())
        {
            ResourcePool targets = myMethod.CalculateNeeded(myPlugin, myId, myTarget);
            for (Goal child : myChildren) {
                int target = (int)Math.ceil(targets.GetSpecific(child.myId));
                child.SetTarget(target);
            }
        }

        mySubscribers.forEach(GoalSubscriber::OnTargetChanged);
    }

    private void Setup()
    {
        RecalculateProgress();
        RecalculateBanked();

        myPlugin.myProgressManager.AddTracker(this, myId);
    }

    public void UnsetMethod()
    {
        myMethod = null;
        myChildren.clear();

        mySubscribers.forEach(GoalSubscriber::OnSubGoalsCleared);
    }

    public void SetMethod(Method aMethod)
    {
        UnsetMethod();

        myMethod = aMethod.Copy();

        myMethod.CalculateNeeded(myPlugin, myId, myTarget)
                .All()
                .stream()
                .filter((kvPair) -> kvPair.getValue() > 0)
                .forEach((kvPair) -> AddSubGoal(kvPair.getKey(), (int)Math.ceil(kvPair.getValue())));

        mySubscribers.forEach(GoalSubscriber::OnMethodChanged);

        myPlugin.RequestSave();
    }

    private void AddSubGoal(String aId, int aTarget)
    {
        Goal subGoal = new Goal(myPlugin, aId, aTarget, false, null);

        subGoal.AddSubscriber(new GoalSubscriber() {
            @Override
            public void OnSubGoalAdded(Goal aSubGoal) {}

            @Override
            public void OnProgressChanged() {
                RecalculateBanked();
            }

            @Override
            public void OnRemove() {
                if (myMethod != null)
                {
                    myMethod.myTakes.Remove(subGoal.GetId());
                    myMethod.myRequires.Remove(subGoal.GetId());
                }

                myChildren.remove(subGoal);

                myPlugin.RequestSave();
            }
        });

        myChildren.add(subGoal);

        mySubscribers.forEach((sub) -> sub.OnSubGoalAdded(subGoal));
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

            TodoPlugin.debug("Progress on " + this, 1);

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

    @Override
    public String toString() {
        return myId + ": " + myProgress + " + (" + myBanked + ")/" + myTarget;
    }
}
