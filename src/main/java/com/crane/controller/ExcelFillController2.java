package com.crane.controller;

import com.crane.domain.OutputData;
import com.crane.service.impl.TransServiceImpl;
import com.crane.utils.DataRouterConstant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/excel_download2")
public class ExcelFillController2 {

    private static Logger logger = LoggerFactory.getLogger(ExcelFillController2.class);

    @Value("${tag_agent_config.genesis.address}")
    private String genesisAddress;

    @Value("${tag_agent_config.genesis.utc}")
    private String genesisUtc;

    @Value("${tag_agent_config.excel.row_pic_height}")
    private String excelPicHeight;

    @Value("${tag_agent_config.excel.row_pic_fill}")
    private String excelPicScale;

    @Value("${tag_agent_config.excel.row_text_height}")
    private String excelTextHeight;

    @Value("${tag_agent_config.genesis_api_event}")
    private String apiEventLimit;

    @Value("${tag_agent_config.genesis_api_scene}")
    private String apiSceneLimit;

    @Value("${tag_agent_config.genesis.camera}")
    private String genesisCamId;

    @Value("${tag_agent_config.camera_rel.neuro_to_genesis}")
    private String megToGenesis;

    @Value("${tag_agent_config.neuro.address}")
    private String neuroAddress;

    @Value("${tag_agent_config.mag_cube.camera}")
    private String magCameras;

    private static final int PER_PAGE_REC = 100;


    @GetMapping("")
    public String forwardRequest(HttpServletRequest request) {

        return "excelFill2/index";
    }

    @GetMapping("/backup_page2")
    public String backup_page(HttpServletRequest request) {

        return "excelFill2/index2";
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
        //结束默认时间
        Calendar nowCal = Calendar.getInstance(TimeZone.getDefault());
        timeMap.put("e_default", sdf.format(nowCal.getTime()));
        //结束最大时间
        timeMap.put("e_max", sdf.format(nowCal.getTime()));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(timeMap);
    }

    @GetMapping("/go")
    public void goDownload(HttpServletResponse response, HttpServletRequest request) throws IOException {

        //获取请求的参数
        String inputSTime = request.getParameter("s_time").replace("T", " ");
        String inputETime = request.getParameter("e_time").replace("T", " ");
        String askType = request.getParameter("ask_type");

        //根据选中的类型，决定获取哪些数据
        String excelPath = "";
        if (askType.equals("object")) {

            List<OutputData> sceneUniList = new ArrayList<>();

            List<OutputData> bodyObjectList = getBodyDataFromMag(inputSTime, inputETime);

            if (bodyObjectList != null && !bodyObjectList.isEmpty()) {
                sceneUniList.addAll(bodyObjectList);
            }

            Comparator<OutputData> timeComparator = Comparator.comparing(OutputData::getTime);
            sceneUniList.sort(timeComparator);

            excelPath = makeExcel(sceneUniList, inputSTime, inputETime);

        } else if (askType.equals("event")) {

//            List<OutputData> uniList = new ArrayList<>();
//
//            List<OutputData> eventList = getEventDataFromGenesis(startTime, endTime);
//            List<OutputData> tagEventList = getEventByTag(inputSTime, inputETime);
//
//            if (eventList != null && !eventList.isEmpty()) {
//                uniList.addAll(eventList);
//            }
//            if (tagEventList != null && !tagEventList.isEmpty()) {
//                uniList.addAll(tagEventList);
//            }
//
//            Comparator<OutputData> timeComparator = Comparator.comparing(OutputData::getTime);
//            uniList.sort(timeComparator);
//
//            excelPath = makeExcel(uniList, inputSTime, inputETime);

        } else {

//            //查询两个接口的数据
//            List<OutputData> uniList = new ArrayList<>();
//
//            List<OutputData> objectList = getObjectDataFromGenesis(inputSTime, inputETime);
//
//            List<OutputData> eventList = getEventDataFromGenesis(startTime, endTime);
//            List<OutputData> tagEventList = getEventByTag(inputSTime, inputETime);
//
//            if (objectList != null && !objectList.isEmpty()) {
//                uniList.addAll(objectList);
//            }
//            if (eventList != null && !eventList.isEmpty()) {
//                uniList.addAll(eventList);
//            }
//            if (tagEventList != null && !tagEventList.isEmpty()) {
//                uniList.addAll(tagEventList);
//            }
//            //合并数据到一个集合
//            //排序
//            Comparator<OutputData> timeComparator = Comparator.comparing(OutputData::getTime);
//            uniList.sort(timeComparator);
//
//            excelPath = makeExcel(uniList, inputSTime, inputETime);
        }

        // 读到流中
        InputStream inputStream = Files.newInputStream(Paths.get(excelPath));// 文件的存放路径
        response.reset();
        response.setContentType("application/octet-stream");

        File finishedExcel = new File(excelPath);
        String filename = finishedExcel.getName();
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
        finishedExcel.delete();
    }

    private List<OutputData> getEventByTag(String startTime, String endTime) {

        HttpClient httpClient = HttpClients.createDefault();
        //ask
        URIBuilder uriBuilder = null;
        try {

            uriBuilder = new URIBuilder("http://" + genesisAddress.concat("/ainvr/api/scenes"));

            //params
            List<NameValuePair> parList = new ArrayList<>();
            parList.add(new BasicNameValuePair("start", startTime));
            parList.add(new BasicNameValuePair("end", endTime));
            parList.add(new BasicNameValuePair("size", apiEventLimit));
            parList.add(new BasicNameValuePair("cameraIds", getAllCameras()));
            uriBuilder.addParameters(parList);

            HttpGet httpGet = new HttpGet(uriBuilder.build());
            //header
            httpGet.addHeader("X-Auth-Token", TransServiceImpl.genesisToken);

            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet);
            int code = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());
            if (code > 199 && code < 300) {
                return formatGenesisTag(result);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("ask genesis event http error: ", e);
            return null;
        }

    }

    private List<OutputData> formatGenesisTag(String result) throws Exception {

        //读取响应结果
        List<OutputData> reList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode mainNode = objectMapper.readTree(result);

        //转为excel实体类格式
        JsonNode contentNodes = mainNode.get("content");
        for (JsonNode oneSceneNode : contentNodes) {

            //拿到事件的id
            String sceneID = oneSceneNode.get("sceneId").asText();
            //拿到事件的图片链接
            String sceneImgUrl = oneSceneNode.get("thumbnail").asText();
            //拿到相机的名称
            String cameraName = oneSceneNode.get("cameraName").asText();
            //拿到事件时间
            String sceneTime = oneSceneNode.get("datetime").asText().replace("T", " ").replace(genesisUtc, "");
            //拿到具体的信息
            String detailJson = getSceneDetail(sceneID);

            JsonNode detailNode = objectMapper.readTree(detailJson);

            //如果是幻方的，就判断它的hashtag
            if (detailNode.has("hashtags")) {

                JsonNode tagsNode = detailNode.get("hashtags");
                if (!tagsNode.isEmpty()) {
                    String firstTag = tagsNode.get(0).asText();

                    //是幻方的行为
                    if (firstTag.equals("fighting")) {
                        OutputData magFight = new OutputData();
                        magFight.setResult(sceneImgUrl);
                        magFight.setTime(sceneTime);
                        magFight.setCamera(cameraName);
                        magFight.setType("Person");
                        magFight.setAttribute("fight");
                        reList.add(magFight);
                    } else if (firstTag.equals("running")) {
                        OutputData magRun = new OutputData();
                        magRun.setResult(sceneImgUrl);
                        magRun.setTime(sceneTime);
                        magRun.setCamera(cameraName);
                        magRun.setType("Person");
                        magRun.setAttribute("Running");
                        reList.add(magRun);
                    }
                }
            }
        }
        return reList;
    }

    private String makeExcel(List<OutputData> eventList, String sTime, String eTime) {

        //用新型的excel
        XSSFWorkbook workbook = new XSSFWorkbook();

        //sheet单
        XSSFSheet sheet = workbook.createSheet("History");

        //列的宽度设置
        int colWid = 20 * 256;
        sheet.setColumnWidth(0, colWid);
        sheet.setColumnWidth(1, colWid);
        sheet.setColumnWidth(2, colWid);
        sheet.setColumnWidth(3, colWid);
        sheet.setColumnWidth(4, colWid * 3);
        float colWidPix = sheet.getColumnWidthInPixels(0);

        //大点的字体
        XSSFFont biggerFont = workbook.createFont();
        biggerFont.setFontHeightInPoints((short) 15);

        //正常的字体
        XSSFFont normalFont = workbook.createFont();
        normalFont.setFontHeightInPoints((short) 11);

        //行1
        XSSFRow row1 = sheet.createRow(0);
        row1.setHeightInPoints(20);
        XSSFCell r1c1 = row1.createCell(0);
        //字体
        CellStyle r1c1CellStyle = workbook.createCellStyle();
        r1c1CellStyle.setFont(biggerFont);
        //赋值
        r1c1.setCellValue("Metadata");
        r1c1.setCellStyle(r1c1CellStyle);

        //行2
        XSSFRow row2 = sheet.createRow(1);
        XSSFCell r2c1 = row2.createCell(0);
        XSSFCell r2c2 = row2.createCell(1);
        XSSFCell r2c3 = row2.createCell(2);
        //长度需要跨列
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));
        r2c1.setCellValue("Time: " + sTime + " - " + eTime);
        //字体
        CellStyle r2c1CellStyle = workbook.createCellStyle();
        r2c1CellStyle.setFont(normalFont);
        r2c1.setCellStyle(r2c1CellStyle);

        //标题行
        XSSFRow row4 = sheet.createRow(3);
        row4.setHeightInPoints(16);
        //黑边框样式
        CellStyle titleCellStyle = workbook.createCellStyle();
        titleCellStyle.setBorderLeft(BorderStyle.THIN);
        titleCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        titleCellStyle.setBorderTop(BorderStyle.THIN);
        titleCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        titleCellStyle.setBorderRight(BorderStyle.THIN);
        titleCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        titleCellStyle.setBorderBottom(BorderStyle.THIN);
        titleCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        //左右上下居中
        titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //字体
        titleCellStyle.setFont(normalFont);
        //背景
        titleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        //标题列需先填文本后赋值
        //声明标题并赋予样式
        XSSFCell r4c1 = row4.createCell(0);
        r4c1.setCellValue("Result");
        r4c1.setCellStyle(titleCellStyle);

        XSSFCell r4c2 = row4.createCell(1);
        r4c2.setCellValue("Time");
        r4c2.setCellStyle(titleCellStyle);

        XSSFCell r4c3 = row4.createCell(2);
        r4c3.setCellValue("Camera");
        r4c3.setCellStyle(titleCellStyle);

        XSSFCell r4c4 = row4.createCell(3);
        r4c4.setCellValue("Type");
        r4c4.setCellStyle(titleCellStyle);

        XSSFCell r4c5 = row4.createCell(4);
        r4c5.setCellValue("attribute text");
        r4c5.setCellStyle(titleCellStyle);

        //-----------------------------------------------------------------------------------------|

        //内容行的边框
        CellStyle contentCellStyle = workbook.createCellStyle();
        contentCellStyle.setBorderLeft(BorderStyle.THIN);
        contentCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        contentCellStyle.setBorderTop(BorderStyle.THIN);
        contentCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        contentCellStyle.setBorderRight(BorderStyle.THIN);
        contentCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        contentCellStyle.setBorderBottom(BorderStyle.THIN);
        contentCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        //字体，以及自动折行
        contentCellStyle.setFont(normalFont);
        contentCellStyle.setWrapText(true);
        //内容行的居中策略
        contentCellStyle.setAlignment(HorizontalAlignment.LEFT);
        contentCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        //数据从5行开始
        int i = 4;
        Drawing<XSSFShape> drawing = sheet.createDrawingPatriarch();
        for (OutputData oneEv : eventList) {

            //创建行
            XSSFRow rowN = sheet.createRow(i);
            i = i + 1;
            if (i % 100 == 0) {
                (workbook).getSheet("History");
            }

            //每行第1列为图片，动态的行高
            XSSFCell rnc1 = rowN.createCell(0);
            rnc1.setCellValue(oneEv.getResult());
            rnc1.setCellStyle(contentCellStyle);
            //填充图片到位置
            if (StringUtils.isBlank(oneEv.getResult())) {
                rowN.setHeightInPoints(Float.parseFloat(excelTextHeight));
            } else {
                //开始下载图片
                byte[] picBts = downloadSnapshot(oneEv.getResult());
                if (picBts == null) {
                    continue;
                } else {

                    rowN.setHeightInPoints(Float.parseFloat(excelPicHeight));
                    //定位图片位置
                    XSSFCreationHelper helper = workbook.getCreationHelper();
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(0);
                    anchor.setRow1(i - 1);
                    anchor.setCol2(1);
                    anchor.setRow2(i);

                    //绘制图片数据
                    int picIdx = workbook.addPicture(picBts, Workbook.PICTURE_TYPE_PNG);
                    Picture excelPic = drawing.createPicture(anchor, picIdx);
                    String[] scaleArr = excelPicScale.split("x");
                    double scaleX = Double.parseDouble(scaleArr[0]);
                    double scaleY = Double.parseDouble(scaleArr[1]);
                    excelPic.resize(scaleX, scaleY);
                }

            }

            //每行第2列为时间
            XSSFCell rnc2 = rowN.createCell(1);
            rnc2.setCellValue(oneEv.getTime());
            rnc2.setCellStyle(contentCellStyle);

            //每行第3列为相机
            XSSFCell rnc3 = rowN.createCell(2);
            rnc3.setCellValue(oneEv.getCamera());
            rnc3.setCellStyle(contentCellStyle);

            //每行第4列为类型
            XSSFCell rnc4 = rowN.createCell(3);
            rnc4.setCellValue(oneEv.getType());
            rnc4.setCellStyle(contentCellStyle);

            //每行第5列为时间
            XSSFCell rnc5 = rowN.createCell(4);
            rnc5.setCellValue(oneEv.getAttribute());
            rnc5.setCellStyle(contentCellStyle);
        }

        // 设置Excel文件路径
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String downloadTime = sdf.format(new Date());
        String outputPath = "./metadata/data/excel/" + downloadTime + ".xlsx";
        File file = new File(outputPath);

        try {
            // 创建指向该路径的输出流
            FileOutputStream stream = new FileOutputStream(file);

            // 将数据导出到Excel表格
            workbook.write(stream);
            workbook.close();

            // 关闭输出流
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputPath;
    }

    public byte[] downloadSnapshot(String sourceUrl) {

        try {
            URL url = new URL(sourceUrl);
            // 打开连接
            URLConnection con = url.openConnection();
            //设置请求超时为5s
            con.setConnectTimeout(3 * 1000);
            // 输入流
            InputStream is = con.getInputStream();
            return IOUtils.toByteArray(is);
        } catch (Exception e) {
            logger.error("download snapshot :", e);
            return null;
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
            parList.add(new BasicNameValuePair("start", startTime));
            parList.add(new BasicNameValuePair("end", endTime));
            parList.add(new BasicNameValuePair("types", "LOITERING,CROWD_DETECTION"));
            parList.add(new BasicNameValuePair("size", apiEventLimit));
            parList.add(new BasicNameValuePair("cameraIds", getAllCameras()));
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
        } catch (Exception e) {
            logger.error("ask genesis event http error: ", e);
            return null;
        }
    }

    private List<OutputData> formatGenesisEvt(String result) throws Exception {

        //读取响应结果
        List<OutputData> reList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode mainNode = objectMapper.readTree(result);

        //转为excel实体类格式
        JsonNode contentNodes = mainNode.get("content");
        for (JsonNode oneSceneNode : contentNodes) {

            OutputData newEv = new OutputData();
            newEv.setResult(oneSceneNode.get("snapshot").asText());
            newEv.setTime(oneSceneNode.get("datetime").asText().replace("T", " ").replace(genesisUtc, ""));
            newEv.setCamera(oneSceneNode.get("camera").get("name").asText());
            newEv.setType("Event");
            newEv.setSceneType(1);
            newEv.setResolution(oneSceneNode.get("camera").get("resolution").asText());

            //事件的属性
            String eventType = oneSceneNode.get("eventType").asText();
            if (eventType.equals("LOITERING")) {
                newEv.setAttribute("loiter");
            } else if (eventType.equals("CROWD_DETECTION")) {
                String crowdNum = oneSceneNode.get("metadata").get("crowdNumber").asText();
                newEv.setAttribute("Crowd detection:".concat(crowdNum));
            }

            reList.add(newEv);
        }

        return reList;
    }

    private List<OutputData> getBodyDataFromMag(String startTime, String endTime) {

        //result
        List<OutputData> finalBodyData = new ArrayList<>();

        //apache http
        HttpClient httpClient = HttpClients.createDefault();
        //ask
        URIBuilder uriBuilder = null;
        CloseableHttpResponse response = null;

        //jackson
        ObjectMapper objectMapper = new ObjectMapper();

        try {

            String url = "http://" + neuroAddress + DataRouterConstant.NEURO_API + "/v1/event/record/pedestrian/list";
            uriBuilder = new URIBuilder(url);

            //params
            Map<String, Object> paramsMap = new HashMap<>();
            //trans mills
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            //start
            String magStart = String.valueOf(sdf.parse(startTime.concat(".000")).getTime());
            paramsMap.put("startTime", magStart);
            //end
            String magEnd = String.valueOf(sdf.parse(endTime.concat(".000")).getTime());
            paramsMap.put("endTime", magEnd);
            //page size
            paramsMap.put("pageSize", PER_PAGE_REC);
            //page num
            paramsMap.put("pageNum", 1);
            //camera
            paramsMap.put("channelUuids", magCameras.split(","));

            HttpPost httpPost = new HttpPost(uriBuilder.build());
            //header
            httpPost.addHeader("Content-Type", "application/json");
            //body
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(paramsMap)));

            response = (CloseableHttpResponse) httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            String res = EntityUtils.toString(response.getEntity());
            if (code > 199 && code < 300) {
                JsonNode firstResNode = objectMapper.readTree(res);
                int totalRec = firstResNode.get("data").get("total").asInt();
                if (totalRec >= PER_PAGE_REC) {
                    int loopNum = (totalRec / PER_PAGE_REC) + 1;
                    for (int i = 0; i < loopNum; i++) {

                    }
                } else {
                    finalBodyData.addAll(formatMagBody(res));
                }
            }
        } catch (Exception e) {
            logger.error("ask genesis scene http error: ", e);
            return null;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    logger.error("get mag body data close res error");
                }
            }
        }
        return finalBodyData;
    }

    private List<OutputData> getBodyDataFromMagPage(String startMills, String endMills, int pageTh) {

        //apache http
        HttpClient httpClient = HttpClients.createDefault();
        //ask
        URIBuilder uriBuilder = null;
        CloseableHttpResponse response = null;

        try {

            ObjectMapper objectMapper = new ObjectMapper();

            String url = "http://" + neuroAddress + DataRouterConstant.NEURO_API + "/v1/event/record/pedestrian/list";
            uriBuilder = new URIBuilder(url);

            //params
            Map<String, Object> paramsMap = new HashMap<>();
            //start
            paramsMap.put("startTime", startMills);
            //end
            paramsMap.put("endTime", endMills);
            //page size
            paramsMap.put("pageSize", PER_PAGE_REC);
            //page num
            paramsMap.put("pageNum", pageTh);
            //camera
            paramsMap.put("channelUuids", magCameras.split(","));

            HttpPost httpPost = new HttpPost(uriBuilder.build());
            //header
            httpPost.addHeader("Content-Type", "application/json");
            //body
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(paramsMap)));

            response = (CloseableHttpResponse) httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            String res = EntityUtils.toString(response.getEntity());
            if (code > 199 && code < 300) {

            }

        } catch (Exception e) {
            logger.error("ask genesis scene http error(page): ", e);
            return null;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    logger.error("get mag body data close res error(page):");
                }
            }
        }
        return null;
    }

    private List<OutputData> formatMagBody(String result) throws Exception {

        //读取响应结果
        List<OutputData> reList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode mainNode = objectMapper.readTree(result);

        //转为excel实体类格式
        JsonNode contentNodes = mainNode.get("content");
        for (JsonNode oneSceneNode : contentNodes) {

            //拿到事件的id
            String sceneID = oneSceneNode.get("sceneId").asText();
            //拿到事件的图片链接
            String sceneImgUrl = oneSceneNode.get("thumbnail").asText();
            //拿到相机的名称
            String cameraName = oneSceneNode.get("cameraName").asText();
            //拿到事件时间
            String sceneTime = oneSceneNode.get("datetime").asText().replace("T", " ").replace(genesisUtc, "");
            //拿到具体的信息
            String oneDetailJson = getSceneDetail(sceneID);

            //判断具体信息，是不是有tag信息，有的话是幻方给的
            JsonNode detailNode = objectMapper.readTree(oneDetailJson);
            if (detailNode.has("hashtags")) {

                //拿到tag数组，判断里面是否有动作标签
                JsonNode tagArrNode = detailNode.get("hashtags");

                //如果没有tag标签，是genesis自己的数据,拿到所有子类
                String objectsJson = getSceneObject(sceneID);
                JsonNode sceneObjectsNode = objectMapper.readTree(objectsJson);

                for (JsonNode oneObjNode : sceneObjectsNode) {

                    int putFlag = 1;

                    OutputData oneGenesisOD = new OutputData();

                    //第一个赋予图片
                    oneGenesisOD.setResult(sceneImgUrl);

                    //时间
                    oneGenesisOD.setTime(sceneTime);
                    //相机名称
                    oneGenesisOD.setCamera(cameraName);
                    //model 类型
                    oneGenesisOD.setType(oneObjNode.get("objectType").asText());

                    //属性
                    StringBuilder aText = new StringBuilder();
                    JsonNode metaDataNode = oneObjNode.get("metadata");
                    //颜色
                    if (metaDataNode.has("colors")) {
                        JsonNode colorNode = metaDataNode.get("colors");
                        if (!colorNode.isEmpty()) {
                            aText.append("Colors:");
                            for (JsonNode oneColor : colorNode) {
                                aText.append(oneColor.asText()).append(",");
                            }
                            aText.append(". ");
                        }
                    }
                    //车牌
                    if (metaDataNode.has("licensePlate")) {
                        putFlag = 2;
                        JsonNode lpNode = metaDataNode.get("licensePlate");
                        if (lpNode.has("number")) {
                            aText.append("LPR:").append(lpNode.get("number").asText()).append(". ");
                        }
                    }
                    //车制造型号
                    if (metaDataNode.has("makeModel")) {
                        JsonNode mmNode = metaDataNode.get("makeModel");
                        if (mmNode.has("make")) {
                            aText.append("Make:").append(mmNode.get("make").asText()).append(". ");
                        }
                        if (mmNode.has("model")) {
                            aText.append("Model:").append(mmNode.get("model").asText()).append(". ");
                        }
                    }
                    if (metaDataNode.has("face")) {

                        putFlag = 2;
                        JsonNode faceNode = metaDataNode.get("face");

                        if (faceNode.has("targetName")) {
                            aText.append("Name:").append(faceNode.get("targetName").asText()).append(". ");
                        }
                    }
                    oneGenesisOD.setAttribute(aText.toString());

                    if (putFlag == 1) {
                        reList.add(oneGenesisOD);
                    }
                }

            }
        }

        return reList;
    }

    private String getSceneObject(String sceneID) {

        HttpClient httpClient = HttpClients.createDefault();
        //ask
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder("http://" + genesisAddress.concat("/ainvr/api/scenes/").concat(sceneID).concat("/objects"));

            HttpGet httpGet = new HttpGet(uriBuilder.build());
            //header
            httpGet.addHeader("X-Auth-Token", TransServiceImpl.genesisToken);

            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet);
            int code = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());
            if (code > 199 && code < 300) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("ask genesis event http error: ", e);
            return null;
        }
    }

    private String getSceneDetail(String sceneID) {

        HttpClient httpClient = HttpClients.createDefault();
        //ask
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder("http://" + genesisAddress.concat("/ainvr/api/scenes/").concat(sceneID));

            HttpGet httpGet = new HttpGet(uriBuilder.build());
            //header
            httpGet.addHeader("X-Auth-Token", TransServiceImpl.genesisToken);

            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet);
            int code = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());
            if (code > 199 && code < 300) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("ask genesis event http error: ", e);
            return null;
        }
    }

    public String getAllCameras() {

        Set<String> cidSet = new HashSet<>();
        String[] genesisCamArr = genesisCamId.split(",");
        for (String oneCid : genesisCamArr) {
            cidSet.add(oneCid);
        }

        String[] mirArr = megToGenesis.split(",");
        for (String oneMapper : mirArr) {
            cidSet.add(oneMapper.split("@")[1]);
        }

        StringBuilder resultCid = new StringBuilder();
        for (String oneCam : cidSet) {
            resultCid.append(oneCam).append(",");
        }

        if (resultCid.length() > 1) {
            resultCid.deleteCharAt(resultCid.length() - 1);
        }

        return resultCid.toString();

    }

    @GetMapping("/backup_page1")
    @ResponseBody
    public String backup_page1(HttpServletRequest request) {

        return getAllCameras();
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