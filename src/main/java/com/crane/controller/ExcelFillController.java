package com.crane.controller;


import com.crane.domain.GenesisExcelFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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

        //插入标题列，并赋予样式
        CellStyle titleCellStyle = sheet.getRow(4).getCell(0).getCellStyle();
        //7列
        Cell c7 = sheet.getRow(4).createCell(7);
        c7.setCellValue("c7");
        c7.setCellStyle(titleCellStyle);
        //8列
        Cell c8 = sheet.getRow(4).createCell(8);
        c8.setCellValue("c8");
        c8.setCellStyle(titleCellStyle);
        //9列
        Cell c9 = sheet.getRow(4).createCell(9);
        c9.setCellValue("c9");
        c9.setCellStyle(titleCellStyle);

        //列宽
        sheet.setColumnWidth(7,256*50);
        sheet.setColumnWidth(8,256*50);
        sheet.setColumnWidth(9,256*50);

        //从第五行开始读取数据
        CellStyle contentCellStyle = sheet.getRow(5).getCell(0).getCellStyle();
        for (int rowIndex = 5; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            //拿到场景id
            XSSFCell sceneIdCell = row.getCell(0);
            long sceneId = Long.parseLong(sceneIdCell.getStringCellValue().trim());

            //插入列
            Cell contentC7 = row.createCell(7);
            contentC7.setCellStyle(contentCellStyle);
            contentC7.setCellValue(sceneId + "_7");

            Cell contentC8 = row.createCell(8);
            contentC8.setCellStyle(contentCellStyle);
            contentC8.setCellValue(sceneId + "_8");

            Cell contentC9 = row.createCell(9);
            contentC9.setCellStyle(contentCellStyle);
            contentC9.setCellValue(sceneId + "_9");

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
