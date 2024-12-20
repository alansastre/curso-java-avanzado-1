package com.certidevs.repository;

import com.certidevs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByCompanyId(Long id);
}