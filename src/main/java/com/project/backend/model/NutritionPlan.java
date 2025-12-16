package com.project.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nutrition_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
public class NutritionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"nutritionPlans", "exercisePlans"})
    private UserInfo user;

    @Column(name = "daily_calories_to_eat", nullable = false)
    private double dailyCaloriesToEat;

    @Column(name = "breakfast_calories", nullable = false)
    private double breakfastCalories;

    @Column(name = "lunch_calories", nullable = false)
    private double lunchCalories;

    @Column(name = "dinner_calories", nullable = false)
    private double dinnerCalories;

    @Column(name = "pre_workout_calories")
    private Double preWorkoutCalories;

    @Column(name = "post_workout_calories")
    private Double postWorkoutCalories;

    @ElementCollection
    @CollectionTable(name = "nutrition_plan_breakfast_foods", joinColumns = @JoinColumn(name = "nutrition_plan_id"))
    @Column(name = "food")
    private List<String> breakfastFoods = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "nutrition_plan_lunch_foods", joinColumns = @JoinColumn(name = "nutrition_plan_id"))
    @Column(name = "food")
    private List<String> lunchFoods = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "nutrition_plan_dinner_foods", joinColumns = @JoinColumn(name = "nutrition_plan_id"))
    @Column(name = "food")
    private List<String> dinnerFoods = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "nutrition_plan_pre_workout_foods", joinColumns = @JoinColumn(name = "nutrition_plan_id"))
    @Column(name = "food")
    private List<String> preWorkoutFoods = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "nutrition_plan_post_workout_foods", joinColumns = @JoinColumn(name = "nutrition_plan_id"))
    @Column(name = "food")
    private List<String> postWorkoutFoods = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
