package com.crane.controller;

import com.crane.domain.OutputData;
import com.crane.service.impl.TransServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

@Controller
@RequestMapping("/excel_download")
public class ExcelFillController {

    private static Logger logger = LoggerFactory.getLogger(ExcelFillController.class);

    @Value("${tag_agent_config.genesis.address}")
    private String genesisAddress;

    @GetMapping("")
    public String forwardRequest(HttpServletRequest request) {

        return "excelFill/index";
    }

    @GetMapping("/time_map")
    @ResponseBody
    public String getTimeJson() throws Exception {

        Map<String, String> timeMap = new HashMap<>();

        //带时区的时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //开始最小时间
        Calendar threeDayBefore = Calendar.getInstance(TimeZone.getDefault());
        threeDayBefore.add(Calendar.HOUR, -72);
        timeMap.put("b_min", sdf.format(threeDayBefore.getTime()));
        //开始最大时间
        Calendar sixHourBefore = Calendar.getInstance(TimeZone.getDefault());
        sixHourBefore.add(Calendar.HOUR, -6);
        timeMap.put("b_max", sdf.format(sixHourBefore.getTime()));
        //开始默认时间
        timeMap.put("b_default", sdf.format(sixHourBefore.getTime()));

        //结束最小时间
        Calendar threeDayLittle = Calendar.getInstance(TimeZone.getDefault());
        threeDayLittle.add(Calendar.HOUR, -67);
        timeMap.put("e_min", sdf.format(threeDayLittle.getTime()));
        //结束最大时间
        Calendar todayEndTime = Calendar.getInstance(TimeZone.getDefault());
        todayEndTime.set(Calendar.HOUR_OF_DAY, 23);
        todayEndTime.set(Calendar.MINUTE, 59);
        todayEndTime.set(Calendar.SECOND, 59);
        timeMap.put("e_max", sdf.format(todayEndTime.getTime()));
        //结束默认时间
        Calendar nowCal = Calendar.getInstance(TimeZone.getDefault());
        timeMap.put("e_default", sdf.format(nowCal.getTime()));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(timeMap);
    }

    public static void main(String[] args) throws Exception {
        ZoneId zoneId = ZoneId.systemDefault();
        System.out.println(zoneId);
    }

    @GetMapping("/go")
    public void goDownload(HttpServletResponse response, HttpServletRequest request) throws IOException {

        //genesis需要时区
        SimpleDateFormat sdf = new SimpleDateFormat("Z");
        String utc = sdf.format(new Date());
        StringBuilder buffer = new StringBuilder(utc);
        buffer.insert(3, ':');
        utc = buffer.toString();

        //获取请求的参数
        String startTime = request.getParameter("s_time").concat(utc);
        String endTime = request.getParameter("e_time").concat(utc);
        String askType = request.getParameter("ask_type");

        //根据选中的类型，决定获取哪些数据
        if (askType.equals("object")) {

            List<OutputData> objectList = getObjectDataFromGenesis(startTime, endTime);

        } else if (askType.equals("event")) {

            List<OutputData> eventList = getEventDataFromGenesis(startTime, endTime);


        } else {

            //查询两个接口的数据
            List<OutputData> uniList = new ArrayList<>();
            List<OutputData> objectList = getObjectDataFromGenesis(startTime, endTime);
            List<OutputData> eventList = getObjectDataFromGenesis(startTime, endTime);
            //合并数据到一个集合
            uniList.addAll(objectList);
            uniList.addAll(eventList);
            //排序
            Comparator<OutputData> timeComparator = Comparator.comparing(OutputData::getTime);
            uniList.sort(timeComparator);
        }
    }

    private List<OutputData> getEventDataFromGenesis(String startTime, String endTime) {

        HttpClient httpClient = HttpClients.createDefault();
        //ask
        URIBuilder uriBuilder = null;
        try {

            uriBuilder = new URIBuilder("http://" + genesisAddress.concat("/ainvr/api/commonEvents"));

            //params
            List<NameValuePair> parList = new ArrayList<>();
            parList.add(new BasicNameValuePair("start",startTime));
            parList.add(new BasicNameValuePair("end",endTime));
            parList.add(new BasicNameValuePair("types","LOITERING,CROWD_DETECTION"));
            parList.add(new BasicNameValuePair("size","10000"));
            uriBuilder.addParameters(parList);

            HttpGet httpGet = new HttpGet(uriBuilder.build());
            //header
            httpGet.addHeader("X-Auth-Token", TransServiceImpl.genesisToken);

            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet);
            int code = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());
            if (code > 199 && code < 300) {
                return formatGenesisEvt(result);
            } else {
                return null;
            }
        }catch (Exception e){
            logger.error("ask genesis event http error: ", e);
            return null;
        }
    }

    private List<OutputData> formatGenesisEvt(String result) throws Exception{

        ObjectMapper objectMapper  = new ObjectMapper();
        JsonNode jsonNodes = objectMapper.readTree(result);

        for (JsonNode oneSceneNode:jsonNodes){
            if (oneSceneNode.has("eventType")){
                String eventName = oneSceneNode.get("eventType").toString().toLowerCase();
            }
        }

        List<OutputData> reList = new ArrayList<>();
        return reList;
    }

    private List<OutputData> getObjectDataFromGenesis(String startTime, String endTime) {
        return null;
    }

//    @PostMapping("/upload")
//    public void upload(GenesisExcelFile excelFile, HttpServletResponse response) throws IOException {
//
//        MultipartFile inputExcelFile = excelFile.getExcelFile();
//
//        // 创建临时文件
//        String savedPath = "./metadata/data/excel/";
//
//        // 获取文件名
//        String fileName = inputExcelFile.getOriginalFilename();
//        fileName = "Extra_" + System.currentTimeMillis() + "_" + fileName;
//
//        // 创建文件
//        File newFile = new File(savedPath + fileName);
//        try {
//            FileUtils.copyInputStreamToFile(inputExcelFile.getInputStream(), newFile);
//        } catch (Exception e) {
//            logger.error("save excel: ", e);
//            return;
//        }
//
//        //给apache poi处理
//        String downloadExcel = excelDataFill(newFile);
//        if (StringUtils.isBlank(downloadExcel)) {
//            return;
//        }
//
//        // 读到流中
//        InputStream inputStream = Files.newInputStream(Paths.get(downloadExcel));// 文件的存放路径
//        response.reset();
//        response.setContentType("application/octet-stream");
//        String filename = new File(downloadExcel).getName();
//        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
//        ServletOutputStream outputStream = response.getOutputStream();
//        byte[] b = new byte[3072];
//        int len;
//        //从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
//        while ((len = inputStream.read(b)) > 0) {
//            outputStream.write(b, 0, len);
//        }
//        inputStream.close();
//
//        //删除临时文件
//        newFile.delete();
//    }


//    public String excelDataFill(File excelfile) {
//
//        //读取这个文件
//        XSSFWorkbook workbook;
//        try {
//            FileInputStream excelFileInputStream = new FileInputStream(excelfile);
//            workbook = new XSSFWorkbook(excelFileInputStream);
//            excelFileInputStream.close();
//        } catch (Exception e) {
//            logger.error("read excel: ", e);
//            return null;
//        }
//
//        //拿第一个sheet单
//        XSSFSheet sheet = workbook.getSheetAt(0);
//
//        //----------------------------------------------------------------------------------------------------|
//
//        //更改第1行的标题
//        Row row1 = sheet.getRow(0);
//        row1.getCell(0).setCellValue("Metadata");
//        //减少单元格合并范围
//        sheet.removeMergedRegion(0);
//        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
//
//        //减少第3行的单元格合并范围
//        sheet.removeMergedRegion(0);
//        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 4));
//
//        //删除第4行的数据，并移除它的合并单元格
//        Row row4 = sheet.getRow(3);
//        row4.getCell(0).setCellValue("");
//        sheet.removeMergedRegion(0);
//
//        //修改第5行标题的名称
//        Row row5 = sheet.getRow(4);
//        row5.getCell(0).setCellValue("Result");
//        row5.getCell(1).setCellValue("Time");
//        row5.getCell(2).setCellValue("Camera");
//        row5.getCell(3).setCellValue("Type");
//        row5.getCell(4).setCellValue("Attribute Text");
//        //删除第5行的第6,7个标题
//        row5.removeCell(row5.getCell(5));
//        row5.removeCell(row5.getCell(6));
//
//        //调整列宽数据
//        int col2Wid = sheet.getColumnWidth(1);
//        sheet.setColumnWidth(0, col2Wid);
//        int col9Wid = sheet.getColumnWidth(8);
//        sheet.setColumnWidth(4, col9Wid * 8);
//        sheet.setColumnWidth(5, col9Wid);
//        sheet.setColumnWidth(6, col9Wid);
//
//        //----------------------------------------------------------------------------------------------------|
//
//        // 遍历形状获取图片和对象
//        XSSFDrawing drawing = sheet.createDrawingPatriarch();
//        List<XSSFShape> shapes = drawing.getShapes();
//
//        int i = 5;
//        for (XSSFShape shape : shapes) {
//            // 获取图片
//            if (shape instanceof XSSFPicture) {
//                XSSFPicture picture = (XSSFPicture) shape;
//
//                //删除第一列的数据
//                Row dataRowN = sheet.getRow(i);
//                String sceneId = dataRowN.getCell(0).getStringCellValue();
//                dataRowN.getCell(0).setCellValue("");
//                // 移动图片到第一列
//                XSSFClientAnchor anchor = picture.getClientAnchor();
//                anchor.setCol1(0);
//                i++;
//
//                //第3列数据前移
//                dataRowN.getCell(1).setCellValue(dataRowN.getCell(2).getStringCellValue());
//
//                //第4列数据前移
//                dataRowN.getCell(2).setCellValue(dataRowN.getCell(3).getStringCellValue());
//
//                //删除5，6行
//                dataRowN.removeCell(dataRowN.getCell(5));
//                dataRowN.removeCell(dataRowN.getCell(6));
//            }
//        }
//
//        //----------------------------------------------------------------------------------------------------|
//
//        //将增加的行，切换到上面取
////        for (GenesisExcelRow extraRow : extraRowList) {
////            sheet.shiftRows(extraRow.getNewIndex(), extraRow.getOldIndex(), 1);
////        }
//
//        //----------------------------------------------------------------------------------------------------|
//
//
//        //输出最终的excel
//        try {
//            FileOutputStream fos = new FileOutputStream(excelfile);
//            workbook.write(fos);
//            fos.close();
//            workbook.close();
//        } catch (Exception e) {
//            logger.error("write final:", e);
//            return null;
//        }
//
//        return excelfile.getPath();
//    }

}
