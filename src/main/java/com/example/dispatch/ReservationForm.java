package com.example.dispatch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationForm {

    @NotNull(message = "引取日を入力してください")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate pickupDate;

    @NotNull(message = "昼か夜かを選んでください")
    private Shift shift;

    @NotBlank(message = "引取先を入力してください")
    private String customerName;

    @Valid
    @NotEmpty(message = "品目と数量を1行以上入力してください")
    private List<ReservationLine> lines = new ArrayList<>();

    private String remarks;

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public Shift getShift() { return shift; }
    public void setShift(Shift shift) { this.shift = shift; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) {
        this.customerName = (customerName == null) ? null : customerName.strip();
    }

    public List<ReservationLine> getLines() { return lines; }
    public void setLines(List<ReservationLine> lines) { this.lines = lines; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}