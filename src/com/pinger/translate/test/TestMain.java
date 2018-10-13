package com.pinger.translate.test;

import com.pinger.translate.bean.TranslationBean;
import com.pinger.translate.main.LocalData;
import com.pinger.translate.net.NetWorkUtils;
import gherkin.deps.com.google.gson.Gson;
import org.apache.http.util.TextUtils;

/**
 * 专门用来测试的类
 */
public class TestMain {

    public static void main(String[] args) {

        String word = "中文";
        String localData = LocalData.INSTANCE.read(word);

        if (!TextUtils.isEmpty(localData)) {
            // TODO 还要判断一下本地数据的code是不是有问题
            TranslationBean bean = new Gson().fromJson(localData, TranslationBean.class);

        }
        NetWorkUtils.requestGet("中文");
    }


}
