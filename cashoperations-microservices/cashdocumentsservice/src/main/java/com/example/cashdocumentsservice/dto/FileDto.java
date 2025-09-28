package com.example.cashdocumentsservice.dto;

import lombok.Data;

@Data
public class FileDto {
    private Long id;

    private String fileGroup;

    private String fileName;
}
