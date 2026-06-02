package com.ewaste.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ewaste.entity.EwasteRequestItem;

import java.util.*;

public interface EwasteRequestItemRepository extends JpaRepository<EwasteRequestItem, Long> {

    List<EwasteRequestItem> findByRequestId(Long requestId);
}
