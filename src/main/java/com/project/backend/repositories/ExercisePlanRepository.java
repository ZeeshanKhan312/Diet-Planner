package com.project.backend.repositories;

import com.project.backend.model.ExercisePlan;
import com.project.backend.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExercisePlanRepository extends JpaRepository<ExercisePlan, Long> {
    List<ExercisePlan> findByUser(UserInfo user);
    List<ExercisePlan> findByUser_UserId(String userId);
    Optional<ExercisePlan> findFirstByUser_UserIdOrderByCreatedAtDesc(String userId);
}

