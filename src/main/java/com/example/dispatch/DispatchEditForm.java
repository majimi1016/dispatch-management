package com.example.dispatch;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class DispatchEditForm {

    @NotNull(message = "配車日を入力してください")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dispatchDate;

    @NotNull(message = "昼か夜かを選んでください")
    private Shift shift;

    @NotNull(message = "会社を選んでください")
    private Company company;

    @NotNull(message = "品目を選んでください")
    private Item item;

    @NotBlank(message = "配送先を入力してください")
    private String destination;

    @NotNull(message = "車格を選んでください")
    private VehicleSize vehicleSize;

    @NotNull(message = "台数を入力してください")
    @Min(value = 1, message = "台数は1以上で入力してください")
    private Integer quantity;

    private String remarks;

    // 保存されている配達から、編集画面の入力内容を作る
    public static DispatchEditForm from(Dispatch d) {
        DispatchEditForm form = new DispatchEditForm();
        form.setDispatchDate(d.getDispatchDate());
        form.setShift(d.getShift());
        form.setCompany(d.getCompany());
        form.setItem(d.getItem());
        form.setDestination(d.getDestination());
        form.setVehicleSize(d.getVehicleSize());
        form.setQuantity(d.getQuantity());
        form.setRemarks(d.getRemarks());
        return form;
    }

    // 編集画面の入力内容を、配達に書き戻す
    public void applyTo(Dispatch d) {
        d.setDispatchDate(dispatchDate);
        d.setShift(shift);
        d.setCompany(company);
        d.setItem(item);
        d.setDestination(destination);
        d.setVehicleSize(vehicleSize);
        d.setQuantity(quantity);
        d.setRemarks(remarks);
    }

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
