package com.ewaste.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ewaste_request_items")
public class EwasteRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requestId;

    private Long itemId;

    private int quantity;

    /** Transient — populated by service layer for display */
    @Transient
    private String itemName;

    public Long getId()              { return id; }
    public void setId(Long id)       { this.id = id; }
    public Long getRequestId()       { return requestId; }
    public void setRequestId(Long r) { this.requestId = r; }
    public Long getItemId()          { return itemId; }
    public void setItemId(Long i)    { this.itemId = i; }
    public int  getQuantity()        { return quantity; }
    public void setQuantity(int q)   { this.quantity = q; }
    public String getItemName()      { return itemName; }
    public void setItemName(String n){ this.itemName = n; }
}
