package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.model.entity.Activity;
import com.red.team.taskvisionapp.repository.ActivityRepository;
import com.red.team.taskvisionapp.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    @Override
    public List<Activity> getAllActivitiesByProjectId(String projectId) {
        return activityRepository.findByProjectId(projectId);
    }

    @Override
    public Activity createActivity(Activity activity) {
        return activityRepository.save(activity);
    }
}
