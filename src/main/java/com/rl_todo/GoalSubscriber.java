package com.rl_todo;

public interface GoalSubscriber {
    void OnSubGoalAdded(Goal aSubGoal);
    void OnSubGoalsCleared();
    void OnTargetChanged();
    void OnBankedChanged();
    void OnProgressChanged();
    void OnMethodChanged();
    void OnCompleted();
}
