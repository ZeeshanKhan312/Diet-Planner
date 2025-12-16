package com.project.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercise_set")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "exercisePlan"})
public class ExerciseSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_plan_id", nullable = false)
    @JsonIgnoreProperties({"exerciseSets"})
    private ExercisePlan exercisePlan;

    @Column(name = "name", nullable = false)
    private String name; // e.g. "Push Ups"

    @Column(name = "equipment")
    private String equipment; // e.g. "Bodyweight", "Resistance band"

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes; // per session

    @Column(name = "sessions_per_week", nullable = false)
    private int sessionsPerWeek; // frequency
}

