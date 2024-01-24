package com.crane.utils.http;

import com.alibaba.fastjson.JSON;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HttpPoolUtil {
    private static Logger logger = LoggerFactory.getLogger(HttpPoolUtil.class);
    // 池化管理
    private static PoolingHttpClientConnectionManager poolConnManager = null;

    private static CloseableHttpClient httpClient;// 它是线程安全的，所有的线程都可以使用它一起发送http请求

    static {
        try {

            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            // 配置同时支持 HTTP 和 HTPPS
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();
            //Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();
            // 初始化连接管理器
            poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            poolConnManager.setMaxTotal(60);// 同时最多连接数
            // 设置最大路由
            poolConnManager.setDefaultMaxPerRoute(30);
            // 此处解释下MaxtTotal和DefaultMaxPerRoute的区别：
            // 1、MaxtTotal是整个池子的大小；
            // 2、DefaultMaxPerRoute是根据连接到的主机对MaxTotal的一个细分；比如：
            // MaxtTotal=400 DefaultMaxPerRoute=200
            // 而我只连接到http://www.abc.com时，到这个主机的并发最多只有200；而不是400；
            // 而我连接到http://www.bac.com 和
            // http://www.ccd.com时，到每个主机的并发最多只有200；即加起来是400（但不能超过400）；所以起作用的设置是DefaultMaxPerRoute
            // 初始化httpClient
            httpClient = getConnection();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static CloseableHttpClient getConnection() {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(1200).setConnectionRequestTimeout(1200).setSocketTimeout(1200).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                // 设置连接池管理
                .setConnectionManager(poolConnManager)
                .setDefaultRequestConfig(config)
                // 设置重试次数
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();
        return httpClient;
    }

    public static String httpGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());
            int code = response.getStatusLine().getStatusCode();
            if (code > 199 && code < 300) {
                return result;
            } else {
                logger.error("请求{}返回错误码：{},{}", url, code, result);
                return null;
            }
        } catch (IOException e) {
            logger.error("http请求异常，{}", url, e);
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String httpGet(String url, Header... heads) {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;

        try {

            if (heads != null) {
                httpGet.setHeaders(heads);
            }

            response = httpClient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());
            int code = response.getStatusLine().getStatusCode();
            if (code > 199 && code < 300) {
                return result;
            } else {
                logger.error("请求{}返回错误码：{},{}", url, code, result);
                return null;
            }
        } catch (IOException e) {
            logger.error("http请求异常，{}", url, e);
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String httpDelete(String url, Header... heads) {

        HttpDelete httpDelete = new HttpDelete(url);

        CloseableHttpResponse response = null;

        try {

            if (heads != null) {
                httpDelete.setHeaders(heads);
            }

            response = httpClient.execute(httpDelete);
            String result = EntityUtils.toString(response.getEntity());
            int code = response.getStatusLine().getStatusCode();
            if (code > 199 && code < 300) {
                return result;
            } else {
                logger.error("请求{}返回错误码：{},{}", url, code, result);
                return null;
            }
        } catch (IOException e) {
            logger.error("http请求异常，{}", url, e);
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String post(String uri, Object params, Header... heads) {

        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpResponse response = null;
        try {
            StringEntity paramEntity = new StringEntity(JSON.toJSONString(params));
            paramEntity.setContentEncoding("UTF-8");
            paramEntity.setContentType("application/json");
            httpPost.setEntity(paramEntity);
            if (heads != null) {
                httpPost.setHeaders(heads);
            }
            response = httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());

            if (code > 199 && code < 300) {
                return result;
            } else {
                logger.error("请求{}返回错误码:{},请求参数:{},{}", uri, code, params, result);
                return null;
            }
        } catch (IOException e) {
            logger.error("收集服务配置http请求异常", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String noJsonPost(String uri, String params, Header... heads) {

        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpResponse response = null;
        try {
            StringEntity paramEntity = new StringEntity(params);
            paramEntity.setContentEncoding("UTF-8");
            paramEntity.setContentType("application/json");
            httpPost.setEntity(paramEntity);
            if (heads != null) {
                httpPost.setHeaders(heads);
            }
            response = httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());

            if (code > 199 && code < 300) {
                return result;
            } else {
                logger.error("请求{}返回错误码:{},请求参数:{},{}", uri, code, params, result);
                return null;
            }
        } catch (IOException e) {
            logger.error("收集服务配置http请求异常", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String forwardPost(String uri, String picFilePath, String sceneText, Header... heads) {

        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpResponse response = null;

        try {

            // 创建 MultipartEntityBuilder
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setCharset(StandardCharsets.UTF_8);

            // 添加字符串参数
            builder.addTextBody("scene", sceneText, ContentType.TEXT_PLAIN);

            // 添加图片文件
            File imageFile = new File(picFilePath);
            builder.addBinaryBody("file", imageFile, ContentType.MULTIPART_FORM_DATA, imageFile.getName());

            // 构建 HttpEntity
            HttpEntity multipartEntity = builder.build();

            // 设置请求的实体部分
            httpPost.setEntity(multipartEntity);

            if (heads != null) {
                httpPost.setHeaders(heads);
            }

            response = httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());
            if (code > 199 && code < 300) {
                return result;
            } else {

                return null;
            }
        } catch (IOException e) {
            logger.error("收集服务配置http请求异常", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}