package com.example.authvento.repository;

import com.example.authvento.model.OtpModel;
import com.example.authvento.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserModelRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);


    @Query(nativeQuery = true, value = "" +
            "select * from vento_users e " +
            "where e.email = :email " +
            "order by e.id desc " +
            "limit 1")
    UserModel findLatestByEmail(String email);
}
