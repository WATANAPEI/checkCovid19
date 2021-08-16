package dev.wpei.checkcovid19.model;

import lombok.Data;

import java.util.List;

@Data
public class Response {
    ErrorInfo errorInfo;
    List<Item> itemList;
}
