package com.ardoq;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;

public class ExcelImport {

    private static final String ardoqUsername = System.getenv("ardoqUsername");
    private static final String host = System.getenv("ardoqHost");
    private static final String ardoqPassword = System.getenv("ardoqPassword");

    public static void main(String[] args) throws IOException {

        InputStream resourceAsStream = ExcelImport.class.getResourceAsStream("/data.xlsx");

        XSSFWorkbook xssfSheets = new XSSFWorkbook(resourceAsStream);

        ArdoqClient client = new ArdoqClient(host, ardoqUsername, ardoqPassword);
    }
}
