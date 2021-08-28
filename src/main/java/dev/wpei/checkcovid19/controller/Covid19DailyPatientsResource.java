package dev.wpei.checkcovid19.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.wpei.checkcovid19.model.DailyPatient;
import dev.wpei.checkcovid19.model.ErrorInfo;
import lombok.Data;

import java.util.List;

@Data
public class Covid19DailyPatientsResource {
    ErrorInfo errorInfo;
    @JsonProperty("itemList")
    List<DailyPatient> dailyPatientList;
}
