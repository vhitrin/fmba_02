package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Controller {

    public static final int TYPE_STRING = 0;
    public static final int TYPE_NUM = 1;
    public static final int TYPE_DATE = 2;

    @FXML
    public static Stage STAGE;

    public TableView<DataInputString> dataInputTable;
    public TableColumn<DataInputString, String> D1Column;
    public TableColumn<DataInputString, String> P1Column;
    public TableColumn<DataInputString, Float> V1Column;
    public TableColumn<DataInputString, Float> V2Column;
    public TableColumn<DataInputString, Float> V3Column;
    public TableColumn<DataInputString, Float> V4Column;

    public TableView<DataCalculateInputString> dataTable;
    public TableColumn<DataCalculateInputString, String> P1DataColumn;
    public TableColumn<DataCalculateInputString, Float> V5DataColumn;
    public TableColumn<DataCalculateInputString, Float> V6DataColumn;
    public TableColumn<DataCalculateInputString, Float> V7DataColumn;
    public TableColumn<DataCalculateInputString, Float> V8DataColumn;
    public TableColumn<DataCalculateInputString, Float> V9DataColumn;
    public TableColumn<DataCalculateInputString, Float> V10DataColumn;

    private ObservableList<DataInputString> dataInputStringObservableList = FXCollections.observableArrayList();
    private ObservableList<DataCalculateInputString> dataCalculateInputStringObservableList = FXCollections.observableArrayList();

    private Map<String, String> fieldsName = new LinkedHashMap<>();

    public void ImportFromExcelOnAction() {
        try {
            String excelFileName = hndlOpenFile(true);
            if (!excelFileName.isEmpty()) {
                readFromExcel(excelFileName);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void readFromExcel(String file) throws Exception {

        Connection conn = DBHelper.getConnection();
        Statement statement = conn.createStatement();
        statement.execute("DELETE FROM 'OriginalData';");
        statement.execute("DELETE FROM 'FieldsName';");
        System.out.println("Deleting table complete!");

        Map<Integer, Integer> rowTypes = new HashMap<>();
        rowTypes.put(0, TYPE_STRING);
        rowTypes.put(1, TYPE_STRING);
        rowTypes.put(2, TYPE_NUM);
        rowTypes.put(3, TYPE_NUM);
        rowTypes.put(4, TYPE_NUM);
        rowTypes.put(5, TYPE_NUM);

        XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);

        int rowCount = myExcelSheet.getLastRowNum();
        int colCount = myExcelSheet.getRow(0).getLastCellNum();

        XSSFRow row0 = myExcelSheet.getRow(0);
        XSSFRow row1 = myExcelSheet.getRow(1);

        for (int i = 0; i < colCount; i++) {

            String nameField = row0.getCell(i).getStringCellValue();
            String displayField = row1.getCell(i).getStringCellValue();

            fieldsName.put(nameField, displayField);

            String query = "INSERT INTO 'FieldsName' ('nameField', 'displayField') VALUES ('" + nameField + "', '" + displayField + "');";
            statement.execute(query);
        }

        setColumnsName();

        dataInputStringObservableList.clear();

        colCount = 6;

        for (int i = 2; i <= rowCount; i++) {

            XSSFRow row = myExcelSheet.getRow(i);

            String query = "INSERT INTO 'OriginalData' ('D1', 'P1', 'V1', 'V2', 'V3', 'V4') VALUES (";

            String D1 = row.getCell(0).getStringCellValue();
            String P1 = row.getCell(1).getStringCellValue();
            float V1 = (float)row.getCell(2).getNumericCellValue();
            float V2 = (float)row.getCell(3).getNumericCellValue();
            float V3 = (float)row.getCell(4).getNumericCellValue();
            float V4 = (float)row.getCell(5).getNumericCellValue();

            for (int j = 0; j < colCount; j++) {

                if (rowTypes.get(j) == TYPE_STRING) {
                    query += "'" + row.getCell(j).getStringCellValue() + "'";
                } else if (rowTypes.get(j) == TYPE_NUM) {
                    query += row.getCell(j).getNumericCellValue();
                } else if (rowTypes.get(j) == TYPE_DATE) {
                    System.out.println(row.getCell(j).getDateCellValue());
                }

                if (j != colCount - 1) {
                    query += ", ";
                } else {
                    query += ");";
                }
            }

            statement.execute(query);
            dataInputStringObservableList.add(new DataInputString(D1, P1, V1, V2, V3, V4));
        }

        System.out.println("All data will be added!");

        conn.close();
        myExcelBook.close();
    }

    public void CalculateOnAction() throws Exception {

        Connection conn = DBHelper.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT DISTINCT OD.P1, SD.V1, SD.V2, 1000 * SD.V2 / SD.V1 AS V3, OD.V4, SD.V5, SD.V2 * OD.V4 / SD.V5 AS V6, (SD.V5 * 1000 / SD.V1) AS V7" +
                ", OD.V4 / (SD.V5 * 1000 / SD.V1) AS V8, 100 * SD.V2 / SD.V5 AS V9, (1000 * SD.V2 / SD.V1) * (OD.V4 / (SD.V5 * 1000 / SD.V1))  AS V10 FROM OriginalData AS OD INNER JOIN " +
                "(SELECT P1, SUM(V1) AS V1, SUM(V2) AS V2, SUM(V3 * V1 / 1000) AS V5 FROM OriginalData GROUP BY P1) AS SD " +
                "ON OD.P1 = SD.P1");
        System.out.println("Reading data complete!");

        dataCalculateInputStringObservableList.clear();

        while (rs.next()) {
            dataCalculateInputStringObservableList.add(new DataCalculateInputString(rs.getString("P1"),
                    rs.getFloat("V5"),
                    rs.getFloat("V6"),
                    rs.getFloat("V7"),
                    rs.getFloat("V8"),
                    rs.getFloat("V9"),
                    rs.getFloat("V10")));
        }
    }

    public void ExportTableToExcelOnAction() {

        String excelFileName = hndlOpenFile(false);
        if (!excelFileName.isEmpty()) {
            try {
                writeToExcel(excelFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeToExcel(String file) throws IOException {

        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet myExcelSheet = book.createSheet("Данные из программы");

        int rowNumberSecondTable = dataInputStringObservableList.size() + 2 + 2;
        XSSFRow rowSecondTable = myExcelSheet.createRow(rowNumberSecondTable);
        XSSFRow rowSecondTable1 = myExcelSheet.createRow(rowNumberSecondTable + 1);

        XSSFRow row = myExcelSheet.createRow(0);
        XSSFRow row1 = myExcelSheet.createRow(1);

        int elementNumber = 0;
        boolean secondTable = false;
        for (Map.Entry<String, String> entry : fieldsName.entrySet()) {
            if (elementNumber < 6 && !secondTable) {
                row.createCell(elementNumber).setCellValue(entry.getKey());
                row1.createCell(elementNumber).setCellValue(entry.getValue());
            } else {
                if (!secondTable) {
                    secondTable = true;
                    elementNumber = 0;
                    rowSecondTable.createCell(elementNumber).setCellValue("P1");
                    rowSecondTable1.createCell(elementNumber).setCellValue(fieldsName.get("P1"));
                    elementNumber++;
                }
                rowSecondTable.createCell(elementNumber).setCellValue(entry.getKey());
                rowSecondTable1.createCell(elementNumber).setCellValue(entry.getValue());
            }
            elementNumber++;
        }

        int rowNumber = 2;
        for(DataInputString dataInputString : dataInputStringObservableList) {
            row = myExcelSheet.createRow(rowNumber);
            row.createCell(0).setCellValue(dataInputString.getD1());
            row.createCell(1).setCellValue(dataInputString.getP1());
            row.createCell(2).setCellValue(dataInputString.getV1());
            row.createCell(3).setCellValue(dataInputString.getV2());
            row.createCell(4).setCellValue(dataInputString.getV3());
            row.createCell(5).setCellValue(dataInputString.getV4());
            rowNumber++;
        }

        rowNumberSecondTable += 2;
        for(DataCalculateInputString dataCalculateInputString : dataCalculateInputStringObservableList) {
            row = myExcelSheet.createRow(rowNumberSecondTable);
            row.createCell(0).setCellValue(dataCalculateInputString.getP1());
            row.createCell(1).setCellValue(dataCalculateInputString.getV5());
            row.createCell(2).setCellValue(dataCalculateInputString.getV6());
            row.createCell(3).setCellValue(dataCalculateInputString.getV7());
            row.createCell(4).setCellValue(dataCalculateInputString.getV8());
            row.createCell(5).setCellValue(dataCalculateInputString.getV9());
            row.createCell(6).setCellValue(dataCalculateInputString.getV10());
            rowNumberSecondTable++;
        }

        book.write(new FileOutputStream(file));
        book.close();
        System.out.println("Writing data complete!");
    }

    public void ReadDataFromDBOnAction() {
        try {
            fillTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Начальное заполнение таблицы на основании сохраненных в базе данных
    private void fillTable() throws Exception {
        Connection conn = DBHelper.getConnection();
        Statement statement = conn.createStatement();

        ResultSet setFieldsName = statement.executeQuery("SELECT * FROM FieldsName");

        while (setFieldsName.next()) {
            fieldsName.put(setFieldsName.getString("nameField"), setFieldsName.getString("displayField"));
        }

        setColumnsName();

        ResultSet rs = statement.executeQuery("SELECT * FROM 'OriginalData'");
        System.out.println("Reading data complete!");

        dataInputStringObservableList.clear();
        dataCalculateInputStringObservableList.clear();

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString(rs.getString("D1"),
                    rs.getString("P1"),
                    rs.getInt("V1"),
                    rs.getInt("V2"),
                    rs.getFloat("V3"),
                    rs.getFloat("V4")));
        }
    }

    public ObservableList<DataInputString> getDataInputStringObservableList() {
        return dataInputStringObservableList;
    }

    public ObservableList<DataCalculateInputString> getDataCalculateInputStringObservableList() {
        return dataCalculateInputStringObservableList;
    }

    @FXML
    private void initialize() {
        D1Column.setCellValueFactory(cellData -> cellData.getValue().D1Property());
        P1Column.setCellValueFactory(cellData -> cellData.getValue().P1Property());
        V1Column.setCellValueFactory(cellData -> cellData.getValue().V1Property().asObject());
        V2Column.setCellValueFactory(cellData -> cellData.getValue().V2Property().asObject());
        V3Column.setCellValueFactory(cellData -> cellData.getValue().V3Property().asObject());
        V4Column.setCellValueFactory(cellData -> cellData.getValue().V4Property().asObject());
        dataInputTable.setItems(getDataInputStringObservableList());

        try {
            fillTable();
        } catch (Exception e) {
            e.printStackTrace();
        }

        P1DataColumn.setCellValueFactory(cellData -> cellData.getValue().P1Property());
        V5DataColumn.setCellValueFactory(cellData -> cellData.getValue().V5Property().asObject());
        V6DataColumn.setCellValueFactory(cellData -> cellData.getValue().V6Property().asObject());
        V7DataColumn.setCellValueFactory(cellData -> cellData.getValue().V7Property().asObject());
        V8DataColumn.setCellValueFactory(cellData -> cellData.getValue().V8Property().asObject());
        V9DataColumn.setCellValueFactory(cellData -> cellData.getValue().V9Property().asObject());
        V10DataColumn.setCellValueFactory(cellData -> cellData.getValue().V10Property().asObject());
        dataTable.setItems(getDataCalculateInputStringObservableList());
    }

    // Устанавливает заголовки столбцам таблицы согласно ранее сохраненным данным
    private void setColumnsName() {

        D1Column.setText(fieldsName.get("D1"));
        P1Column.setText(fieldsName.get("P1"));
        V1Column.setText(fieldsName.get("V1"));
        V2Column.setText(fieldsName.get("V2"));
        V3Column.setText(fieldsName.get("V3"));
        V4Column.setText(fieldsName.get("V4"));

        P1DataColumn.setText(fieldsName.get("P1"));
        V5DataColumn.setText(fieldsName.get("V5"));
        V6DataColumn.setText(fieldsName.get("V6"));
        V7DataColumn.setText(fieldsName.get("V7"));
        V8DataColumn.setText(fieldsName.get("V8"));
        V9DataColumn.setText(fieldsName.get("V9"));
        V10DataColumn.setText(fieldsName.get("V10"));
    }

    // Открывает диалог выбора файла для указания источника данных для базы
    @FXML
    private String hndlOpenFile(boolean isOpenFileDialog) {

        FileChooser fileChooser = new FileChooser();//Класс работы с диалогом выборки и сохранения
        fileChooser.setTitle(isOpenFileDialog ? "Открытие файла" : "Сохранение файла");//Заголовок диалога
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MS Excel files (*.xlsx)", "*.xlsx");//Расширение
        fileChooser.getExtensionFilters().add(extFilter);

        File file;

        if (isOpenFileDialog) {
            file = fileChooser.showOpenDialog(STAGE);
        } else {
            file = fileChooser.showSaveDialog(STAGE);
        }

        if (file != null) {
            return file.getPath();
        }

        return "";
    }
}