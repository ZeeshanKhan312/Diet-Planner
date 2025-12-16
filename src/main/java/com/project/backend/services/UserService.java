package com.project.backend.services;

import com.project.backend.model.*;
import com.project.backend.repositories.ExercisePlanRepository;
import com.project.backend.repositories.NutritionPlanRepository;
import com.project.backend.repositories.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ExercisePlanRepository exercisePlanRepository;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;


    // Save UserInfo
    public UserInfo saveUser(UserInfo userInfo) {
        return userInfoRepository.save(userInfo);
    }

    // Get a single user
    public UserInfo getUser(String userId) {
        return userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    // Get all users
    public List<UserInfo> getAllUsers() {
        return userInfoRepository.findAll();
    }
    // Delete a user
    public void deleteUser(String userId) {
        try {
            userInfoRepository.deleteById(userId);
        } catch (Exception e) {
            throw new RuntimeException("User not found: " + userId);
        }
    }
    // Generate Diet Plan Logic
    public DietPlanResponse generateDietPlan(String userId) {
        UserInfo user = getUser(userId);

        /* ================= GOAL DETECTION ================= */
        double weightDiff = user.getDesiredWeight() - user.getCurrWeight();

        String goal;
        if (Math.abs(weightDiff) < 0.5) {
            goal = "MAINTAIN";
        } else if (weightDiff < 0) {
            goal = "LOSE_WEIGHT";
        } else {
            goal = "GAIN_WEIGHT";
        }

        /* ================= CALORIE CALCULATION ================= */
        double totalCaloriesChange = 7700 * Math.abs(weightDiff);
        double dailyCalorieChange =
                user.getTargetDays() > 0 ? totalCaloriesChange / user.getTargetDays() : 0;

        if ("LOSE_WEIGHT".equals(goal)) {
            dailyCalorieChange = -dailyCalorieChange;
        }

        /* ================= EXERCISE PLAN ================= */
        ExercisePlan exercisePlan = new ExercisePlan();
        exercisePlan.setUser(user);
        exercisePlan.setGoal(goal);
        exercisePlan.setDailyCalorieChange(dailyCalorieChange);

        List<ExerciseSet> exerciseSets;

        if ("LOSE_WEIGHT".equals(goal)) {
            exerciseSets = Arrays.asList(
                    new ExerciseSet(null, exercisePlan, "Brisk Walking", "None", 30, 5),
                    new ExerciseSet(null, exercisePlan, "Jumping Jacks", "Bodyweight", 15, 4),
                    new ExerciseSet(null, exercisePlan, "Bodyweight Squats", "Bodyweight", 15, 4),
                    new ExerciseSet(null, exercisePlan, "Plank", "Mat", 5, 5)
            );
        } else if ("GAIN_WEIGHT".equals(goal)) {
            exerciseSets = Arrays.asList(
                    new ExerciseSet(null, exercisePlan, "Push Ups", "Bodyweight", 15, 4),
                    new ExerciseSet(null, exercisePlan, "Squats", "Bodyweight", 15, 4),
                    new ExerciseSet(null, exercisePlan, "Resistance Band Rows", "Band", 15, 3),
                    new ExerciseSet(null, exercisePlan, "Plank", "Mat", 5, 5)
            );
        } else {
            exerciseSets = Arrays.asList(
                    new ExerciseSet(null, exercisePlan, "Walking", "None", 30, 4),
                    new ExerciseSet(null, exercisePlan, "Stretching", "Mat", 15, 5)
            );
        }

        exercisePlan.setExerciseSets(exerciseSets);

        /* ================= NUTRITION PLAN ================= */
        NutritionPlan nutritionPlan = new NutritionPlan();
        nutritionPlan.setUser(user);

        double bmr;
        if ("MALE".equalsIgnoreCase(user.getGender())) {
            bmr = (10 * user.getCurrWeight())
                    + (6.25 * user.getHeight())
                    - (5 * user.getAge()) + 5;
        } else {
            bmr = (10 * user.getCurrWeight())
                    + (6.25 * user.getHeight())
                    - (5 * user.getAge()) - 161;
        }

        double dailyCaloriesToEat = bmr + dailyCalorieChange;
        dailyCaloriesToEat = Math.max(dailyCaloriesToEat, 1200); // safety limit

        nutritionPlan.setDailyCaloriesToEat(dailyCaloriesToEat);

        nutritionPlan.setBreakfastCalories(dailyCaloriesToEat * 0.25);
        nutritionPlan.setLunchCalories(dailyCaloriesToEat * 0.35);
        nutritionPlan.setDinnerCalories(dailyCaloriesToEat * 0.25);
        nutritionPlan.setPreWorkoutCalories(dailyCaloriesToEat * 0.075);
        nutritionPlan.setPostWorkoutCalories(dailyCaloriesToEat * 0.075);

        nutritionPlan.setBreakfastFoods(Arrays.asList(
                "Oats", "Boiled Eggs / Paneer", "Fruit"
        ));

        nutritionPlan.setLunchFoods(Arrays.asList(
                "Rice / Roti", "Dal / Chicken", "Vegetables", "Curd"
        ));

        nutritionPlan.setDinnerFoods(Arrays.asList(
                "Grilled Paneer / Chicken", "Salad"
        ));

        nutritionPlan.setPreWorkoutFoods(Arrays.asList(
                "Banana", "Black Coffee"
        ));

        nutritionPlan.setPostWorkoutFoods(Arrays.asList(
                "Protein Shake", "Milk / Boiled Eggs"
        ));

        // Save plans to database
        savePlansToDatabase(exercisePlan, nutritionPlan);
        
        return new DietPlanResponse(exercisePlan, nutritionPlan);
    }

    @Transactional
    private void savePlansToDatabase(ExercisePlan exercisePlan, NutritionPlan nutritionPlan) {
        // Save ExercisePlan (already has user and exercise sets set)
        exercisePlanRepository.save(exercisePlan);

        // Save NutritionPlan (already has user and food lists set)
        nutritionPlanRepository.save(nutritionPlan);
    }
}
