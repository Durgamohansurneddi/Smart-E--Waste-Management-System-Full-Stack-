package com.ewaste.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ewaste.entity.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
}