package dev.wpei.checkcovid19.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Response {
    ErrorInfo errorInfo;
    List<CovidPatientItem> itemList;
}
