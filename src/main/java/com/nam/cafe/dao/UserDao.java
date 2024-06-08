package com.nam.cafe.dao;

import com.nam.cafe.entity.User;
import com.nam.cafe.wrapper.UserWrapper;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserDao extends JpaRepository<User, Integer> {
    User findByEmail(@Param("email") String email);

    List<UserWrapper> getAllUsers();

    @Transactional
    @Modifying
    Integer updateStatus(@Param("status") String status, @Param("id") Integer id);

    List<String> getAllAdmins();

}
