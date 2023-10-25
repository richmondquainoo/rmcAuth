package com.example.authvento.repository;

import com.example.authvento.rabbitmq.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    List<Email> findBySender(String sender);
    List<Email> findByRecipientPhone(String phone);
    List<Email> findByRecipientEmail(String email);
    List<Email> findByRecipientPhoneAndDate(String phone, String date);
    List<Email> findByRecipientEmailAndDate(String email, String date);
    List<Email> findByRecipientPhoneAndDateAndDeliveryStatus(String phone, String date, int status);
    List<Email> findByRecipientEmailAndDateAndDeliveryStatus(String email, String date, int status);
    List<Email> findByRecipientPhoneAndDateBetween(String phone, String start, String end);
    List<Email> findByRecipientEmailAndDateBetween(String email, String start, String end);
    List<Email> findByRecipientPhoneAndDateBetweenAndDeliveryStatus(String phone, String start, String end, int status);
    List<Email> findByRecipientEmailAndDateBetweenAndDeliveryStatus(String email, String start, String end, int status);
    List<Email> findByDate(String date);
    List<Email> findByDateBetween(String start, String end);

    @Query(nativeQuery = true, value = "" +
            "select * from email e " +
            "where e.recipientEmail = :email " +
            "and e.messageType = :messageType " +
            "order by e.date desc " +
            "limit 1")
    Email findDataByEmailAndMessageType(String email, String messageType);

}
