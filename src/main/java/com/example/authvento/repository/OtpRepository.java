package com.example.authvento.repository;

import com.example.authvento.model.OtpModel;
import com.example.authvento.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpModel, Long> {
    Optional<OtpModel> findByEmail(String email);

    @Query(nativeQuery = true, value = "" +
            "select * from otp e " +
            "where e.email = :email " +
            "order by e.id desc " +
            "limit 1")
    OtpModel findLatestOTPByEmail(String email);


}
