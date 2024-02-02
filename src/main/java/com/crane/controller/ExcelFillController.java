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

        //----------------------------------------------------------------------------------------------------|

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


        //----------------------------------------------------------------------------------------------------|

        //文本cell的样式
        CellStyle contentCellStyle = sheet.getRow(5).getCell(1).getCellStyle();

        //最初的总行数
        int oldSheetNum = sheet.getLastRowNum();
        //从第五行开始处理数据
        for (int rowIndex = 5; rowIndex <= oldSheetNum; rowIndex++) {

            XSSFRow oldRow = sheet.getRow(rowIndex);
            if (oldRow == null) {
                continue;
            }

            //如果是我们自己加的行，跳过
            if (oldRow.getCell(0) == null) {
                continue;
            }

            oldRow.removeCell(oldRow.getCell(4));
            oldRow.removeCell(oldRow.getCell(5));
            oldRow.removeCell(oldRow.getCell(6));

            //拿到场景id，并移除单元格值
            XSSFCell sceneIdCell = oldRow.getCell(0);
            long sceneId = Long.parseLong(sceneIdCell.getStringCellValue().trim());
            oldRow.removeCell(oldRow.getCell(0));

            //多个object
            for (int i = 0; i < 3; i++) {

                sheet.shiftRows(rowIndex, oldSheetNum, 1);

                GenesisExcelRow extraRow = new GenesisExcelRow();
                extraRow.setSceneId(String.valueOf(sceneId));
                extraRow.setOldIndex(rowIndex);
                extraRow.setObjectId(i + 1);
                extraRow.setType("Person_" + sceneId);
                extraRow.setAttr("Hat:No_hat;");

                Row newRow = sheet.createRow(rowIndex + i);
                newRow.setHeight(oldRow.getHeight());
                extraRow.setNewIndex(newRow.getRowNum());

                Cell newPicCell = newRow.createCell(1);
                newPicCell.setCellStyle(contentCellStyle);
                newPicCell.setCellValue("");

                Cell newTimeCell = newRow.createCell(2);
                newTimeCell.setCellStyle(contentCellStyle);
                newTimeCell.setCellValue(oldRow.getCell(2).getStringCellValue());

                Cell newCameraCell = newRow.createCell(3);
                newCameraCell.setCellStyle(contentCellStyle);
                newCameraCell.setCellValue(oldRow.getCell(3).getStringCellValue());

                extraRowList.add(extraRow);
                oldSheetNum = oldSheetNum + i;
            }


            //插入列
            Cell contentC4 = oldRow.createCell(4);
            contentC4.setCellStyle(contentCellStyle);
            contentC4.setCellValue(sceneId + "_4");

            Cell contentC5 = oldRow.createCell(5);
            contentC5.setCellStyle(contentCellStyle);
            contentC5.setCellValue(sceneId + "_5");
        }

        //将增加的行，切换到上面取
//        for (GenesisExcelRow extraRow : extraRowList) {
//            sheet.shiftRows(extraRow.getNewIndex(), extraRow.getOldIndex(), 1);
//        }

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
