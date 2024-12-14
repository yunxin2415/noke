package com.example.blogbackend.service.impl;

import com.example.blogbackend.entity.User;
import com.example.blogbackend.entity.UserFollow;
import com.example.blogbackend.repository.UserFollowRepository;
import com.example.blogbackend.repository.UserRepository;
import com.example.blogbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserFollowRepository userFollowRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username)
            .orElseThrow(() -> new RuntimeException("当前用户不存在"));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void followUser(User follower, User following) {
        if (!userFollowRepository.existsByFollowerAndFollowing(follower, following)) {
            UserFollow userFollow = new UserFollow(follower, following);
            userFollowRepository.save(userFollow);
        }
    }

    @Override
    public void unfollowUser(User follower, User following) {
        userFollowRepository.deleteByFollowerAndFollowing(follower, following);
    }

    @Override
    public boolean isFollowing(User follower, Long followingId) {
        User following = findById(followingId)
            .orElseThrow(() -> new RuntimeException("目标用户不存在"));
        return userFollowRepository.existsByFollowerAndFollowing(follower, following);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
} 