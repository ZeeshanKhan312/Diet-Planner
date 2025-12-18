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
import java.util.Optional;

/**
 * Service class for handling user-related business logic including
 * user CRUD operations and diet plan generation.
 */
@Service
public class UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ExercisePlanRepository exercisePlanRepository;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;


    /**
     * Saves a user to the database. If the user already exists (same userId),
     * it will update the existing record. Otherwise, it creates a new user.
     * 
     * @param userInfo The user information to be saved
     * @return The saved user with generated userId if it was null
     */
    public UserInfo saveUser(UserInfo userInfo) {
        return userInfoRepository.save(userInfo);
    }

    /**
     * Retrieves a user by their unique user ID.
     * 
     * @param userId The unique identifier of the user
     * @return The user information
     * @throws RuntimeException if user is not found
     */
    public UserInfo getUser(String userId) {
        return userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    /**
     * Retrieves all users from the database.
     * 
     * @return A list of all users
     */
    public List<UserInfo> getAllUsers() {
        return userInfoRepository.findAll();
    }
    
    /**
     * Deletes a user from the database by their unique user ID.
     * Due to cascade configuration, all associated exercise plans and nutrition plans
     * will also be deleted automatically.
     * 
     * @param userId The unique identifier of the user to be deleted
     * @throws RuntimeException if user is not found
     */
    public void deleteUser(String userId) {
        try {
            userInfoRepository.deleteById(userId);
        } catch (Exception e) {
            throw new RuntimeException("User not found: " + userId);
        }
    }
    
    /**
     * Generates or retrieves a diet plan for a user.
     * 
     * Strategy:
     * 1. First checks if a diet plan already exists for the user
     * 2. If found, returns the existing plan (caching mechanism)
     * 3. If not found, creates a new personalized diet plan based on:
     *    - User's current weight vs desired weight (goal detection)
     *    - Target days to achieve the goal
     *    - User's age, gender, height (for BMR calculation)
     * 4. Saves the new plan to database and returns it
     * 
     * @param userId The unique identifier of the user
     * @return DietPlanResponse containing both exercise plan and nutrition plan
     */
    public DietPlanResponse generateDietPlan(String userId) {
        UserInfo user = getUser(userId);

        // Check if diet plan already exists for this user (most recent one)
        Optional<ExercisePlan> existingExercisePlan = exercisePlanRepository
                .findFirstByUser_UserIdOrderByCreatedAtDesc(userId);
        Optional<NutritionPlan> existingNutritionPlan = nutritionPlanRepository
                .findFirstByUser_UserIdOrderByCreatedAtDesc(userId);

        // If both plans exist, return the existing ones (avoid regenerating)
        if (existingExercisePlan.isPresent() && existingNutritionPlan.isPresent()) {
            return new DietPlanResponse(existingExercisePlan.get(), existingNutritionPlan.get());
        }

        // Otherwise, create new plans based on user's profile and goals
        /* ================= GOAL DETECTION ================= */
        // Calculate the difference between desired and current weight
        double weightDiff = user.getDesiredWeight() - user.getCurrWeight();

        // Determine the fitness goal based on weight difference
        // If difference is less than 0.5kg, maintain current weight
        String goal;
        if (Math.abs(weightDiff) < 0.5) {
            goal = "MAINTAIN";
        } else if (weightDiff < 0) {
            goal = "LOSE_WEIGHT"; // Want to lose weight (desired < current)
        } else {
            goal = "GAIN_WEIGHT"; // Want to gain weight (desired > current)
        }

        /* ================= CALORIE CALCULATION ================= */
        // 1 kg of body weight = approximately 7700 calories
        // Calculate total calories needed to change weight
        double totalCaloriesChange = 7700 * Math.abs(weightDiff);
        
        // Calculate daily calorie change needed (divide total by target days)
        double dailyCalorieChange =
                user.getTargetDays() > 0 ? totalCaloriesChange / user.getTargetDays() : 0;

        // For weight loss, calorie change should be negative (deficit)
        // For weight gain, calorie change should be positive (surplus)
        if ("LOSE_WEIGHT".equals(goal)) {
            dailyCalorieChange = -dailyCalorieChange;
        }

        /* ================= EXERCISE PLAN ================= */
        // Create exercise plan based on the determined goal
        ExercisePlan exercisePlan = new ExercisePlan();
        exercisePlan.setUser(user);
        exercisePlan.setGoal(goal);
        exercisePlan.setDailyCalorieChange(dailyCalorieChange);

        // Define exercise sets based on the user's goal
        // Each exercise set contains: name, equipment, duration (minutes), sessions per week
        List<ExerciseSet> exerciseSets;

        if ("LOSE_WEIGHT".equals(goal)) {
            // Cardio-focused exercises for weight loss
            exerciseSets = Arrays.asList(
                    new ExerciseSet(null, exercisePlan, "Brisk Walking", "None", 30, 5),
                    new ExerciseSet(null, exercisePlan, "Jumping Jacks", "Bodyweight", 15, 4),
                    new ExerciseSet(null, exercisePlan, "Bodyweight Squats", "Bodyweight", 15, 4),
                    new ExerciseSet(null, exercisePlan, "Plank", "Mat", 5, 5)
            );
        } else if ("GAIN_WEIGHT".equals(goal)) {
            // Strength-focused exercises for muscle gain
            exerciseSets = Arrays.asList(
                    new ExerciseSet(null, exercisePlan, "Push Ups", "Bodyweight", 15, 4),
                    new ExerciseSet(null, exercisePlan, "Squats", "Bodyweight", 15, 4),
                    new ExerciseSet(null, exercisePlan, "Resistance Band Rows", "Band", 15, 3),
                    new ExerciseSet(null, exercisePlan, "Plank", "Mat", 5, 5)
            );
        } else {
            // Light exercises for maintaining current weight
            exerciseSets = Arrays.asList(
                    new ExerciseSet(null, exercisePlan, "Walking", "None", 30, 4),
                    new ExerciseSet(null, exercisePlan, "Stretching", "Mat", 15, 5)
            );
        }

        exercisePlan.setExerciseSets(exerciseSets);

        /* ================= NUTRITION PLAN ================= */
        // Create nutrition plan with personalized calorie recommendations
        NutritionPlan nutritionPlan = new NutritionPlan();
        nutritionPlan.setUser(user);

        // Calculate BMR (Basal Metabolic Rate) using Mifflin-St Jeor Equation
        // BMR represents the number of calories the body burns at rest
        double bmr;
        if ("MALE".equalsIgnoreCase(user.getGender())) {
            // BMR formula for males: (10 × weight in kg) + (6.25 × height in cm) - (5 × age) + 5
            bmr = (10 * user.getCurrWeight())
                    + (6.25 * user.getHeight())
                    - (5 * user.getAge()) + 5;
        } else {
            // BMR formula for females: (10 × weight in kg) + (6.25 × height in cm) - (5 × age) - 161
            bmr = (10 * user.getCurrWeight())
                    + (6.25 * user.getHeight())
                    - (5 * user.getAge()) - 161;
        }

        // Calculate total daily calories: BMR + calorie change needed for goal
        double dailyCaloriesToEat = bmr + dailyCalorieChange;
        // Safety limit: minimum 1200 calories per day (below this is unsafe)
        dailyCaloriesToEat = Math.max(dailyCaloriesToEat, 1200);

        nutritionPlan.setDailyCaloriesToEat(dailyCaloriesToEat);

        // Distribute calories across meals:
        // Breakfast: 25% of daily calories
        // Lunch: 35% of daily calories (largest meal)
        // Dinner: 25% of daily calories
        // Pre-workout: 7.5% of daily calories
        // Post-workout: 7.5% of daily calories
        nutritionPlan.setBreakfastCalories(dailyCaloriesToEat * 0.25);
        nutritionPlan.setLunchCalories(dailyCaloriesToEat * 0.35);
        nutritionPlan.setDinnerCalories(dailyCaloriesToEat * 0.25);
        nutritionPlan.setPreWorkoutCalories(dailyCaloriesToEat * 0.075);
        nutritionPlan.setPostWorkoutCalories(dailyCaloriesToEat * 0.075);

        // Set recommended food items for each meal time
        // These are general recommendations that align with the calorie distribution
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

        // Save both plans to database
        savePlansToDatabase(exercisePlan, nutritionPlan);
        
        return new DietPlanResponse(exercisePlan, nutritionPlan);
    }

    /**
     * Saves both exercise plan and nutrition plan to the database in a single transaction.
     * Uses @Transactional to ensure both saves succeed or both fail (atomicity).
     * 
     * @param exercisePlan The exercise plan to be saved
     * @param nutritionPlan The nutrition plan to be saved
     */
    @Transactional
    private void savePlansToDatabase(ExercisePlan exercisePlan, NutritionPlan nutritionPlan) {
        // Save ExercisePlan with all associated exercise sets (cascade save)
        exercisePlanRepository.save(exercisePlan);

        // Save NutritionPlan with all associated food lists (element collections)
        nutritionPlanRepository.save(nutritionPlan);
    }
}
