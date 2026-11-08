package com.example.dispatch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DispatchForm {

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

    // 車格と台数の行(何行でも)
    @Valid
    @NotEmpty(message = "車格と台数を1行以上入力してください")
    private List<DispatchLine> lines = new ArrayList<>();

    private String remarks;

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

    public List<DispatchLine> getLines() { return lines; }
    public void setLines(List<DispatchLine> lines) { this.lines = lines; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
