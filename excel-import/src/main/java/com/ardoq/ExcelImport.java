package com.ardoq;

import com.ardoq.model.*;
import com.ardoq.service.ComponentService;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.io.InputStream;

public class ExcelImport {

    private static final String token = System.getenv("ardoqToken");
    private static final String host = System.getenv("ardoqHost");

    public static void main(String[] args) throws IOException {

        InputStream resourceAsStream = ExcelImport.class.getResourceAsStream("/data.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook(resourceAsStream);

        ArdoqClient client = new ArdoqClient(host, token);
        ComponentService componentService = client.component();

        Model model = client.model().getModelByName("Application service");
        Workspace workspace = client.workspace().createWorkspace(new Workspace("excel-import", model.getId(), "Description"));

        // Import components
        XSSFSheet components = workbook.getSheet("Components");
        int applicationColumn = 0;
        int serviceColumn = 1;

        int rowIndex = 1;
        XSSFRow row = components.getRow(rowIndex);
        XSSFCell applicationCell = null;
        XSSFCell serviceCell = null;
        Component parent = null;
        while(row != null)
        {
            if (applicationCell != row.getCell(applicationColumn)) {
                applicationCell = row.getCell(applicationColumn);
                parent = componentService.createComponent(new Component(applicationCell.toString(), workspace.getId(), ""));
            }
            serviceCell = row.getCell(serviceColumn);
            if (serviceCell != null && parent != null) {
                String type = model.getComponentTypeByName("Service");
                componentService.createComponent(new Component(serviceCell.toString(), workspace.getId(), type, parent.getId()));
            }
            rowIndex++;
            row = components.getRow(rowIndex);
        }

        XSSFSheet tags = workbook.getSheet("Tags");
        XSSFSheet fields = workbook.getSheet("Fields");
    }
}
