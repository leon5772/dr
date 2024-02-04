package com.crane.controller;

import com.crane.domain.GenesisExcelFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    public void upload(GenesisExcelFile excelFile, HttpServletResponse response) throws IOException {

        MultipartFile inputExcelFile = excelFile.getExcelFile();

        // 创建临时文件
        String savedPath = "./metadata/data/excel/";

        // 获取文件名
        String fileName = inputExcelFile.getOriginalFilename();
        fileName = "Extra_" + System.currentTimeMillis() + "_" + fileName;

        // 创建文件
        File newFile = new File(savedPath + fileName);
        try {
            FileUtils.copyInputStreamToFile(inputExcelFile.getInputStream(), newFile);
        } catch (Exception e) {
            logger.error("save excel: ", e);
            return;
        }

        //给apache poi处理
        String downloadExcel = excelDataFill(newFile);
        if (StringUtils.isBlank(downloadExcel)) {
            return;
        }

        // 读到流中
        InputStream inputStream = Files.newInputStream(Paths.get(downloadExcel));// 文件的存放路径
        response.reset();
        response.setContentType("application/octet-stream");
        String filename = new File(downloadExcel).getName();
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        byte[] b = new byte[3072];
        int len;
        //从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
        while ((len = inputStream.read(b)) > 0) {
            outputStream.write(b, 0, len);
        }
        inputStream.close();

        //删除临时文件
        newFile.delete();
    }

    public static void main(String[] args) throws Exception {

        ExcelFillController e = new ExcelFillController();
        File oldF = new File("E:\\work_temp2\\sceneList.xlsx");
        File newF = new File("E:\\work_temp2\\" + System.currentTimeMillis() + "_sceneList.xlsx");

        FileUtils.copyFile(oldF, newF);

        e.excelDataFill(newF);
    }

    public String excelDataFill(File excelfile) {

        //读取这个文件
        XSSFWorkbook workbook;
        try {
            FileInputStream excelFileInputStream = new FileInputStream(excelfile);
            workbook = new XSSFWorkbook(excelFileInputStream);
            excelFileInputStream.close();
        } catch (Exception e) {
            logger.error("read excel: ", e);
            return null;
        }

        //拿第一个sheet单
        XSSFSheet sheet = workbook.getSheetAt(0);

        //----------------------------------------------------------------------------------------------------|

        //更改第1行的标题
        Row row1 = sheet.getRow(0);
        row1.getCell(0).setCellValue("Metadata");
        //减少单元格合并范围
        sheet.removeMergedRegion(0);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        //减少第3行的单元格合并范围
        sheet.removeMergedRegion(0);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));

        //删除第4行的数据，并移除它的合并单元格
        Row row4 = sheet.getRow(3);
        row4.getCell(0).setCellValue("");
        sheet.removeMergedRegion(0);

        //修改第5行标题的名称
        Row row5 = sheet.getRow(4);
        row5.getCell(0).setCellValue("Result");
        row5.getCell(1).setCellValue("Time");
        row5.getCell(2).setCellValue("Camera");
        row5.getCell(3).setCellValue("Type");
        row5.getCell(4).setCellValue("Attribute Text");
        //删除第5行的第6,7个标题
        row5.removeCell(row5.getCell(5));
        row5.removeCell(row5.getCell(6));

        //调整列宽数据
        int col2Wid = sheet.getColumnWidth(1);
        sheet.setColumnWidth(0, col2Wid);
        int col9Wid = sheet.getColumnWidth(8);
        sheet.setColumnWidth(4, col9Wid * 8);
        sheet.setColumnWidth(5, col9Wid);
        sheet.setColumnWidth(6, col9Wid);

        //----------------------------------------------------------------------------------------------------|

        // 遍历形状获取图片和对象
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        List<XSSFShape> shapes = drawing.getShapes();

        int i = 5;
        for (XSSFShape shape : shapes) {
            // 获取图片
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;

                //删除第一列的数据
                Row dataRowN = sheet.getRow(i);
                String sceneId = dataRowN.getCell(0).getStringCellValue();
                dataRowN.getCell(0).setCellValue("");
                // 移动图片到第一列
                XSSFClientAnchor anchor = picture.getClientAnchor();
                anchor.setCol1(0);
                i++;

                //第3列数据前移
                dataRowN.getCell(1).setCellValue(dataRowN.getCell(2).getStringCellValue());

                //第4列数据前移
                dataRowN.getCell(2).setCellValue(dataRowN.getCell(3).getStringCellValue());

                //删除5，6行
                dataRowN.removeCell(dataRowN.getCell(5));
                dataRowN.removeCell(dataRowN.getCell(6));
            }
        }

        //----------------------------------------------------------------------------------------------------|

        //将增加的行，切换到上面取
//        for (GenesisExcelRow extraRow : extraRowList) {
//            sheet.shiftRows(extraRow.getNewIndex(), extraRow.getOldIndex(), 1);
//        }

        //----------------------------------------------------------------------------------------------------|


        //输出最终的excel
        try {
            FileOutputStream fos = new FileOutputStream(excelfile);
            workbook.write(fos);
            fos.close();
            workbook.close();
        } catch (Exception e) {
            logger.error("write final:", e);
            return null;
        }

        return excelfile.getPath();
    }

}
