package com.ewaste.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "time_slot_master")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slotLabel;

    private LocalTime startTime;

    private LocalTime endTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSlotLabel() {
		return slotLabel;
	}

	public void setSlotLabel(String slotLabel) {
		this.slotLabel = slotLabel;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

    
}