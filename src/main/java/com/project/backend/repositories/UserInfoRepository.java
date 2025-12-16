package com.project.backend.repositories;

import com.project.backend.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    public UserInfo findByUserId(String userId);
    UserInfo findByEmail(String email);
}

