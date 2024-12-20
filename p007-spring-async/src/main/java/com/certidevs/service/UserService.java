package com.certidevs.service;

import com.certidevs.entity.User;
import com.certidevs.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private UserRepository userRepository;

    public List<User> findAllByCompanyId(Long id) {
        return userRepository.findAllByCompanyId(id);
    }
}
