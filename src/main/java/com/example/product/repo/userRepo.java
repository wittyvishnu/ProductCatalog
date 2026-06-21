package com.example.product.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.product.model.userModel;

public interface userRepo extends JpaRepository<userModel, Long> {

    Optional<userModel> findByEmail(String email);
}