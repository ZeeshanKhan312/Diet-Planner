package com.project.backend.controllers;

import com.project.backend.model.DietPlanResponse;
import com.project.backend.model.UserInfo;
import com.project.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for handling user-related API endpoints.
 * All endpoints are prefixed with "/api/user"
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Creates a new user or updates an existing user.
     * 
     * @param userInfo The user information to be saved
     * @return ResponseEntity containing the saved user information
     */
    @PostMapping("/save")
    public ResponseEntity<UserInfo> saveUser(@RequestBody UserInfo userInfo){
        return ResponseEntity.ok(userService.saveUser(userInfo));
    }

    /**
     * Retrieves a user by their unique user ID.
     * 
     * @param userId The unique identifier of the user
     * @return ResponseEntity containing the user information
     * @throws RuntimeException if user is not found
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfo> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    /**
     * Retrieves all users from the database.
     * 
     * @return ResponseEntity containing a list of all users
     */
    @GetMapping()
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Deletes a user by their unique user ID.
     * This will also cascade delete all associated exercise plans and nutrition plans.
     * 
     * @param userId The unique identifier of the user to be deleted
     * @return ResponseEntity with no content (204 status)
     * @throws RuntimeException if user is not found
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Generates or retrieves a diet plan for a specific user.
     * If a diet plan already exists for the user, it returns the existing plan.
     * Otherwise, it creates a new personalized diet plan (exercise and nutrition) based on user's goals.
     * 
     * @param userId The unique identifier of the user
     * @return ResponseEntity containing the diet plan (exercise plan and nutrition plan)
     */
    @GetMapping("/{userId}/diet-plan")
    public ResponseEntity<DietPlanResponse> getDietPlan(@PathVariable String userId) {
        return ResponseEntity.ok(userService.generateDietPlan(userId));
    }
}
