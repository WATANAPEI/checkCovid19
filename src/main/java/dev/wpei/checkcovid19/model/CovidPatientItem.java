package dev.wpei.checkcovid19.model;

import lombok.Data;

@Data
public class CovidPatientItem {
    String date;
    String name_jp;
    int npatients;
}
