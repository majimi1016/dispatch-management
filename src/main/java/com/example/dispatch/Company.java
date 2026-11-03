package com.example.dispatch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "会社名を入力してください")
    @Size(max = 50, message = "会社名は50文字以内で入力してください")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @NotNull(message = "社内か社外かを選んでください")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyType type;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) {
        // 前後の空白を自動で消す(「 A運送 」→「A運送」)
        this.name = (name == null) ? null : name.strip();
    }

    public CompanyType getType() { return type; }
    public void setType(CompanyType type) { this.type = type; }
}