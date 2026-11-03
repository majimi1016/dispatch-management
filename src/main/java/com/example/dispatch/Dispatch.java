package com.example.dispatch;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
public class Dispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "配車日を入力してください")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dispatchDate;

    // ↓ 追加:会社(登録済みの会社から選ぶ)
    @NotNull(message = "会社を選んでください")
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    // ↓ 追加:車格(小型・中型・大型)
    @NotNull(message = "車格を選んでください")
    @Enumerated(EnumType.STRING)
    private VehicleSize vehicleSize;

    @NotBlank(message = "車両番号を入力してください")
    private String vehicleNo;

    @NotBlank(message = "ドライバー名を入力してください")
    private String driverName;

    @NotBlank(message = "配送先を入力してください")
    private String destination;

    // ↓ 変更:数量 → 台数
    @NotNull(message = "台数を入力してください")
    @Min(value = 1, message = "台数は1以上で入力してください")
    private Integer quantity;

    private String remarks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDispatchDate() { return dispatchDate; }
    public void setDispatchDate(LocalDate dispatchDate) { this.dispatchDate = dispatchDate; }

    // ↓ 追加
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    // ↓ 追加
    public VehicleSize getVehicleSize() { return vehicleSize; }
    public void setVehicleSize(VehicleSize vehicleSize) { this.vehicleSize = vehicleSize; }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}