package com.ewaste.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ewaste.entity.EwasteItem;


public interface EwasteItemRepository extends JpaRepository<EwasteItem, Long> {
}