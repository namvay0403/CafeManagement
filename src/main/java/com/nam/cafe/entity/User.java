package com.nam.cafe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@NamedQuery(name="User.getAllUsers", query="SELECT new com.nam.cafe.wrapper.UserWrapper(u.id, u.name, u.email, u.contactNumber, u.status) FROM User u where u.role = 'USER'")
@NamedQuery(name="User.updateStatus", query="UPDATE User u SET u.status = :status WHERE u.id = :id")
@NamedQuery(name="User.getAllAdmins", query="SELECT u.email FROM User u where u.role = 'ADMIN'")

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "user")
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "name")
  private String name;

  @Column(name = "contactNumber")
  private String contactNumber;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "status")
  private String status;

  @Column(name = "role")
  private String role;
}
