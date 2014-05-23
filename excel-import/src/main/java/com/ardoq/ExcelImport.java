package com.ardoq;

import com.ardoq.model.*;
import com.ardoq.service.ComponentService;
import org.apache.poi.xssf.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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
        XSSFSheet componentSheet = workbook.getSheet("Components");
        int parentColumn = 0;
        int childColumn = 1;

        HashMap<String, String> components = new HashMap<String, String>(); // name -> id
        XSSFRow typeRow = componentSheet.getRow(0);
        int rowIndex = 1;
        XSSFRow row = componentSheet.getRow(rowIndex);
        XSSFCell parentCell = null;
        XSSFCell childCell;
        Component parent = null;
        Component child;
        while(row != null)
        {
            if (components.get(row.getCell(parentColumn).toString()) == null) {
                parentCell = row.getCell(parentColumn);
                parent = componentService.createComponent(new Component(parentCell.toString(), workspace.getId(), "Description"));
                components.put(parent.getName(), parent.getId());
            }
            childCell = row.getCell(childColumn);
            if (childCell != null && parent != null) {
                String typeId = model.getComponentTypeByName(typeRow.getCell(childColumn).toString());
                child = componentService.createComponent(new Component(childCell.toString(), workspace.getId(), "Description", null, parent.getId()));
                components.put(parent.getName() + "::" + child.getName(), child.getId());
            }
            rowIndex++;
            row = componentSheet.getRow(rowIndex);
        }

        // Create references
        rowIndex = 1;
        row = componentSheet.getRow(rowIndex);
        while (row != null) {
            int referenceIndex = 2;
            XSSFCell referenceCell = row.getCell(referenceIndex);
            while (referenceCell != null) {
                // Get the source and target
                parentCell = row.getCell(parentColumn);
                childCell = row.getCell(childColumn);
                String sourcePath = (childCell != null) ? parentCell.toString() + "::" + childCell.toString() : parentCell.toString();
                String source = components.get(sourcePath);
                String target = components.get(referenceCell.toString());

                // Create integration
                Reference reference = new Reference(workspace.getId(), "Description", source, target, model.getReferenceTypeByName("Synchronous"));
                client.reference().createReference(reference);

                referenceIndex++;
                referenceCell = row.getCell(referenceIndex);
            }
            rowIndex++;
            row = componentSheet.getRow(rowIndex);
        }

        XSSFSheet tags = workbook.getSheet("Tags");
        XSSFSheet fields = workbook.getSheet("Fields");
    }
}
