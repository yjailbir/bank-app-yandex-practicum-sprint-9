package ru.yjailbir.accountsservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "currency")
    private String currency;
    @Column(name = "name")
    private String name;
    @Column(name = "balance")
    private Double balance;
    @Column(name = "active")
    private Boolean active;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    public AccountEntity(String currency, String name, UserEntity user) {
        this.currency = currency;
        this.name = name;
        this.balance = 0.0;
        this.active = false;
        this.user = user;
    }

    public AccountEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
