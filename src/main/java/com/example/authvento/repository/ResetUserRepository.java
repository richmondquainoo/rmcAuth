package com.example.authvento.repository;

import com.example.authvento.model.OtpModel;
import com.example.authvento.model.ResetUserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ResetUserRepository extends JpaRepository<ResetUserModel, Long> {

    @Query(nativeQuery = true, value = "" +
            "select * from otp e " +
            "where e.email = :email " +
            "order by e.id desc " +
            "limit 1")
    ResetUserModel findLatestOTPByEmail(String email);

}
