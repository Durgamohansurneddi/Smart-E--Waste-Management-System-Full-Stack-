package com.ewaste.dto.request;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.*;

public class CreateRequestDTO {

	 @NotEmpty(message = "Items cannot be empty")
	 private List<ItemDTO> items;

	 @NotBlank(message = "Address is required")
	 private String address;

	 @NotNull(message = "Date is required")
	 private LocalDate preferredDate;

	 @NotNull(message = "Time slot is required")
	 private Long timeSlotId;
	 
	public List<ItemDTO> getItems() {
		return items;
	}
	public void setItems(List<ItemDTO> items) {
		this.items = items;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public LocalDate getPreferredDate() {
		return preferredDate;
	}
	public void setPreferredDate(LocalDate preferredDate) {
		this.preferredDate = preferredDate;
	}
	public Long getTimeSlotId() {
		return timeSlotId;
	}
	public void setTimeSlotId(Long timeSlotId) {
		this.timeSlotId = timeSlotId;
	}

   
}