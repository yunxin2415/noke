package com.example.blogbackend.service;

import com.example.blogbackend.entity.User;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    User getCurrentUser();
    Optional<User> findById(Long id);
    void followUser(User follower, User following);
    void unfollowUser(User follower, User following);
    boolean isFollowing(User follower, Long followingId);
    User save(User user);
} 