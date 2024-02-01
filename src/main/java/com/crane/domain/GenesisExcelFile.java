package com.crane.domain;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class GenesisExcelFile {

    private MultipartFile excelFile;

    public MultipartFile getExcelFile() {
        return excelFile;
    }

    public void setExcelFile(MultipartFile excelFile) {
        this.excelFile = excelFile;
    }
}
