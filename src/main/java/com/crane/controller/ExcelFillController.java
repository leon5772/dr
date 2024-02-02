package com.crane.controller;

import com.crane.domain.GenesisExcelFile;
import com.crane.domain.GenesisExcelRow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/excel_fill")
public class ExcelFillController {

    private static Logger logger = LoggerFactory.getLogger(ExcelFillController.class);

    @GetMapping("")
    public String forwardRequest(HttpServletRequest request) {

        return "excelFill/index";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String upload(GenesisExcelFile excelFile) throws IOException {

        MultipartFile inputExcelFile = excelFile.getExcelFile();

        System.out.println(inputExcelFile.getSize() / 1024);

        // 创建文件
        String savedPath = "./metadata/data/excel/";

        // 获取文件名
        String fileName = inputExcelFile.getOriginalFilename();
        fileName = "extra_" + System.currentTimeMillis() + "_" + fileName;

        // 创建文件
        File newFile = new File(savedPath + fileName);
        try {
            FileUtils.copyInputStreamToFile(inputExcelFile.getInputStream(), newFile);
        } catch (Exception e) {
            logger.error("save excel: ", e);
            return "error:".concat(e.getMessage());
        }

        //给apache poi处理
        String finalExcel = excelDataFill(savedPath, fileName);
        if (StringUtils.isBlank(finalExcel)) {
            return "error";
        }

        return "success: " + newFile.getAbsolutePath();
    }

    public String excelDataFill(String filePath, String fileName) {

        //File
        String inputFile = filePath + fileName;

        //读取这个文件
        XSSFWorkbook workbook;
        try {
            FileInputStream excelFileInputStream = new FileInputStream(inputFile);
            workbook = new XSSFWorkbook(excelFileInputStream);
            excelFileInputStream.close();
        } catch (Exception e) {
            logger.error("read excel: ", e);
            return null;
        }

        //拿第一个sheet单
        XSSFSheet sheet = workbook.getSheetAt(0);

        //更改sheet标题
        Row row1 = sheet.getRow(0);
        row1.getCell(0).setCellValue("");
        row1.getCell(1).setCellValue("Metadata");

        //删除r3文本
        Row row3 = sheet.getRow(2);
        String searchTime = row3.getCell(0).getStringCellValue();
        row3.getCell(1).setCellValue(searchTime);
        row3.getCell(0).setCellValue("");

        //删除指定的标题
        Row r4 = sheet.getRow(4);
        r4.removeCell(r4.getCell(0));
        r4.removeCell(r4.getCell(4));
        r4.removeCell(r4.getCell(5));
        r4.removeCell(r4.getCell(6));

        //插入标题列，并赋予样式
        CellStyle titleCellStyle = r4.getCell(1).getCellStyle();
        //4列
        Cell titleC4 = r4.createCell(4);
        titleC4.setCellValue("Type");
        titleC4.setCellStyle(titleCellStyle);
        //5列
        Cell titleC5 = r4.createCell(5);
        titleC5.setCellValue("attribute text");
        titleC5.setCellStyle(titleCellStyle);

        //列宽
        sheet.setColumnWidth(4, 256 * 20);
        sheet.setColumnWidth(5, 256 * 20 * 5);

        //每行scene都有多个object，我们先存储，后插入指定行
        List<GenesisExcelRow> extraRowList = new ArrayList<>();

        //从第五行开始读取数据
        CellStyle contentCellStyle = sheet.getRow(5).getCell(1).getCellStyle();
        for (int rowIndex = 5; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            //删除旧的列
            row.removeCell(row.getCell(4));
            row.removeCell(row.getCell(5));
            row.removeCell(row.getCell(6));

            //拿到场景id，并移除单元格值
            XSSFCell sceneIdCell = row.getCell(0);
            long sceneId = Long.parseLong(sceneIdCell.getStringCellValue().trim());
            row.removeCell(row.getCell(0));

            //多个object
            for (int i = 0; i < 3; i++) {
                GenesisExcelRow extraRow = new GenesisExcelRow();
                extraRow.setSceneId(sceneId + "_" + i + "_" + rowIndex);
                extraRow.setType("Person");
                extraRow.setAttr("Hat:No_hat;");
                extraRowList.add(extraRow);
            }

            //插入列
            Cell contentC4 = row.createCell(4);
            contentC4.setCellStyle(contentCellStyle);
            contentC4.setCellValue(sceneId + "_4");

            Cell contentC5 = row.createCell(5);
            contentC5.setCellStyle(contentCellStyle);
            contentC5.setCellValue(sceneId + "_5");
        }


        //输出最终的excel
        try {
            FileOutputStream fos = new FileOutputStream(filePath + fileName);
            workbook.write(fos);
            fos.close();
            workbook.close();
        } catch (Exception e) {
            logger.error("write final:", e);
            return null;
        }

        return "1";
    }

}
