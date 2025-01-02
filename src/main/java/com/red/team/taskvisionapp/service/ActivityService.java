package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.entity.Activity;

import java.util.List;

public interface ActivityService {
    List<Activity> getAllActivitiesByProjectId(String projectId);

    Activity createActivity(Activity activity);
}
