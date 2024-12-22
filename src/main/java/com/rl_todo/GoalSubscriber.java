package com.rl_todo;

public interface GoalSubscriber {
    default void OnSubGoalAdded(Goal aSubGoal) {}
    default void OnSubGoalsCleared() {}
    default void OnTargetChanged() {}
    default void OnBankedChanged() {}
    default void OnProgressChanged() {}
    default void OnMethodChanged() {}
    default void OnCompleted() {}
    default void OnRemove() {}
}
