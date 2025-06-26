package com.example.personal_finance_manager.Exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeError{

    private LocalDateTime timestamp;
    private HttpStatus status;
    private String message;
}
