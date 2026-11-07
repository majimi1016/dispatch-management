package com.example.dispatch;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DispatchLine {

    @NotNull(message = "車格を選んでください")
    private VehicleSize vehicleSize;

    @NotNull(message = "台数を入力してください")
    @Min(value = 1, message = "台数は1以上で入力してください")
    private Integer quantity;

    public VehicleSize getVehicleSize() { return vehicleSize; }
    public void setVehicleSize(VehicleSize vehicleSize) { this.vehicleSize = vehicleSize; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}