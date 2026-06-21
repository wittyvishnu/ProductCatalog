package com.example.product.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.product.model.productModel;

public interface productRepo extends JpaRepository<productModel, Long> {

}