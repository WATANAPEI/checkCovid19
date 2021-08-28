package dev.wpei.checkcovid19.controller;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LambdaResponse {
    @NonNull
    String savedFileName;
    String errorCode;
    String errorMessage;

}
