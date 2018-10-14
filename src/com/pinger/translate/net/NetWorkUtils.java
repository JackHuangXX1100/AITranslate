package com.pinger.translate.net;

import com.fungo.baselib.manager.ThreadManager;
import com.intellij.openapi.editor.Editor;
import com.pinger.translate.bean.TranslationBean;
import com.pinger.translate.main.LocalData;
import com.pinger.translate.main.MarkDownGenerate;
import com.pinger.translate.utils.TranslateUtils;
import gherkin.deps.com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetWorkUtils {

    private static String BASE_URL = "http://openapi.youdao.com/api";

    private static String YOU_DAO_APP_ID = "06a9384d6dbd6c79";
    private static String YOU_DAO_APP_KEY = "SLNv6EmmhWOE6MUA7pyDMuxB273FqPfh";

    private static String DATA_EMPTY = "返回数据为空";

    /**
     * 发起Get请求，同步请求后，返回Json数据
     * 对Json数据进行重新组合，生成正确格式的String返回
     */
    public static void requestGet(Editor editor, String query) {
        ThreadManager.INSTANCE.runOnSubThread(() -> {

            // 先查询本地是否有数据，如果有数据直接返回
            String localData = LocalData.INSTANCE.read(query);
            if (!TextUtils.isEmpty(localData)) {
                TranslationBean bean = new Gson().fromJson(localData, TranslationBean.class);
                if (bean.isSuccess()) {
                    System.out.println("查询历史纪录成功：" + bean.toString());
                    TranslateUtils.INSTANCE.showPopupWindow(editor, bean.toString());
                    return;
                }
            }

            // 本地没有数据话再查询服务器
            String salt = String.valueOf(System.currentTimeMillis());
            String from = "auto";
            String to = "auto";
            String sign = md5(YOU_DAO_APP_ID + query + salt + YOU_DAO_APP_KEY);
            Map<String, String> params = new HashMap<>();
            params.put("q", query);
            params.put("from", from);
            params.put("to", to);
            params.put("sign", sign);
            params.put("salt", salt);
            params.put("appKey", YOU_DAO_APP_ID);

            String result = requestForHttp(BASE_URL, params);
            System.out.println("请求结果：" + result);

            if (!TextUtils.isEmpty(result)) {
                // 保存数据到本地
                LocalData.INSTANCE.store(query, result);

                result = new Gson().fromJson(result, TranslationBean.class).toString();

                // 保存数据到本地历史纪录
                if (!MarkDownGenerate.isSaved(query)) {
                    MarkDownGenerate.saveWords(query, result);
                }
            } else {
                result = DATA_EMPTY;
            }
            TranslateUtils.INSTANCE.showPopupWindow(editor, result);
        });
    }

    /**
     * 封装Http请求
     *
     * @param url           翻译的接口
     * @param requestParams 接口的参数
     */
    private static String requestForHttp(String url, Map<String, String> requestParams) {
        List<BasicNameValuePair> params = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse httpResponse = null;
        try {
            for (Map.Entry<String, String> en : requestParams.entrySet()) {
                String key = en.getKey();
                String value = en.getValue();
                if (value != null) {
                    params.add(new BasicNameValuePair(key, value));
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            httpResponse = httpClient.execute(httpPost);

            HttpEntity httpEntity = httpResponse.getEntity();
            String content = EntityUtils.toString(httpEntity, "utf-8");
            EntityUtils.consume(httpEntity);
            return content;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 生成32位MD5摘要
     */
    private static String md5(String string) {
        if (string == null) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};

        try {
            byte[] btInput = string.getBytes(StandardCharsets.UTF_8);
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 根据api地址和参数生成请求URL
     */
    private static String getUrlWithQueryString(String url, Map<String, String> params) {
        if (params == null) {
            return url;
        }

        StringBuilder builder = new StringBuilder(url);
        if (url.contains("?")) {
            builder.append("&");
        } else {
            builder.append("?");
        }

        int i = 0;
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null) {
                continue;
            }

            if (i != 0) {
                builder.append('&');
            }

            builder.append(key);
            builder.append('=');
            builder.append(encode(value));

            i++;
        }

        return builder.toString();
    }

    /**
     * 进行URL编码
     */
    private static String encode(String input) {
        if (input == null) {
            return "";
        }
        try {
            return URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return input;
    }
}
