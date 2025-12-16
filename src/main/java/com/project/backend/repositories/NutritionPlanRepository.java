package com.project.backend.repositories;

import com.project.backend.model.NutritionPlan;
import com.project.backend.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, Long> {
    List<NutritionPlan> findByUser(UserInfo user);
    List<NutritionPlan> findByUser_UserId(String userId);
    Optional<NutritionPlan> findFirstByUser_UserIdOrderByCreatedAtDesc(String userId);
}

