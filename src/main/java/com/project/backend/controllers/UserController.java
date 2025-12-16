package com.project.backend.controllers;

import com.project.backend.model.DietPlanResponse;
import com.project.backend.model.UserInfo;
import com.project.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/save")
    public ResponseEntity<UserInfo> saveUser(@RequestBody UserInfo userInfo){
        return ResponseEntity.ok(userService.saveUser(userInfo));
    }

    // Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfo> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    //Gel all Users
    @GetMapping()
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // Diet plan API
    @GetMapping("/{userId}/diet-plan")
    public ResponseEntity<DietPlanResponse> getDietPlan(@PathVariable String userId) {
        return ResponseEntity.ok(userService.generateDietPlan(userId));
    }
}
