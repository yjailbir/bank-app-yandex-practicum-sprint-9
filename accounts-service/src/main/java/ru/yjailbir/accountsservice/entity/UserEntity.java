package ru.yjailbir.accountsservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "login")
    private String login;
    @Column(name = "password")
    private String password;
    @Column(name = "surname")
    private String surname;
    @Column(name = "name")
    private String name;

    public UserEntity(String login, String password, String surname, String name) {
        this.login = login;
        this.password = password;
        this.surname = surname;
        this.name = name;
    }

    public UserEntity() {
    }

    public Integer getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }
}
