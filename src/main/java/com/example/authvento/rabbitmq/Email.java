package com.example.authvento.rabbitmq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;



@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "email")
public class Email {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    private String date;
    private String time;
    private String sender;
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private String recipientCountry;
    private String messageType;
    private String subject;
    private String message;
    private int deliveryStatus;
    private String deliveryMessage;
    private String url;
}
