package com.example.dispatch;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ReservationLine {

    @NotNull(message = "品目を選んでください")
    private Item item;

    @NotNull(message = "数量を入力してください")
    @DecimalMin(value = "0.1", message = "数量は0.1t以上で入力してください")
    @Digits(integer = 6, fraction = 1, message = "数量は小数第1位まで入力してください")
    private BigDecimal quantity;

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}