package com.example.dispatch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "品目名を入力してください")
    @Size(max = 50, message = "品目名は50文字以内で入力してください")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) {
        // 前後の空白を自動で消す
        this.name = (name == null) ? null : name.strip();
    }
}