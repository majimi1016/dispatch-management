package com.example.dispatch;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Entity
public class Dispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dispatchDate;

    @Enumerated(EnumType.STRING)
    private Shift shift;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private String destination;

    @Enumerated(EnumType.STRING)
    private VehicleSize vehicleSize;

    private Integer quantity;

    private String remarks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDispatchDate() { return dispatchDate; }
    public void setDispatchDate(LocalDate dispatchDate) { this.dispatchDate = dispatchDate; }

    public Shift getShift() { return shift; }
    public void setShift(Shift shift) { this.shift = shift; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public VehicleSize getVehicleSize() { return vehicleSize; }
    public void setVehicleSize(VehicleSize vehicleSize) { this.vehicleSize = vehicleSize; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}