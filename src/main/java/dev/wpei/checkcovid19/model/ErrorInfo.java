package dev.wpei.checkcovid19.model;

import lombok.Data;

@Data
public class ErrorInfo {
    String errorFlag;
    String errorCode;
    String errorMessage;
}
