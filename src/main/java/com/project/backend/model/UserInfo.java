package com.project.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_info")
@Data
@DynamicUpdate
@NoArgsConstructor
public class UserInfo {
    @Id
    private String userId;
    @NotBlank(message = "Name is required")
    @Column(name = "name")
    private String name;
    @Email(message = "Invalid email address")
    @Column(name = "email", unique = true)  // Ensures email is unique in the database
    @NotBlank(message = "Email is required")
    private String email;
    @Min(1)
    @Max(100)
    private int age;
    @NotBlank(message = "Gender is required")
    @Column(name = "gender", nullable = false)
    private String gender; // MALE, FEMALE, OTHER
    @Positive
    @Column(name="height",nullable = false)
    private double height;
    @Positive
    @Column(name = "curr_weight")
    private double currWeight;
    @Positive
    @Column(name="desired_weight")
    private double desiredWeight;
    @Positive
    @Column(name = "target_days",nullable = false)
    private int targetDays;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"user", "exerciseSets"})
    private List<ExercisePlan> exercisePlans = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"user"})
    private List<NutritionPlan> nutritionPlans = new ArrayList<>();

    @PrePersist
    protected void generateUserId() {
        if (userId == null || userId.isEmpty()) {
            userId = UUID.randomUUID().toString();
        }
    }

}
