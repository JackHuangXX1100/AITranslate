@file:JvmName("TranslateUtils")

package org.a8sport.translate.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.a8sport.translate.bean.EMPTY
import org.a8sport.translate.bean.TranslationBean
import org.a8sport.translate.main.LocalData
import org.a8sport.translate.net.TranslateCallBack
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

private const val DEBUG = true

private const val BASE_URL = "http://fanyi.youdao.com/openapi.do?keyfrom=Skykai521&key=977124034&type=data&doctype=json&version=1.1&q="


/**
 * 打印日志
 * @param msg 日志内容
 */
fun log(msg: String?) {
    if (DEBUG) {
        println(msg)
    }
}

fun log(msg:Int){
    log(msg.toString())
}

/**
 * 将输入流转换成字符串
 *
 * @param ins input stream
 * @return utf8 string
 */
fun getStringFromStream(ins: InputStream): String {
    return try {
        // 内层流读取数据
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)

        var len = ins.read(buffer)
        // 写入数据到输出流
        while (len != -1) {
            outputStream.write(buffer, 0, len)
            len = ins.read(buffer)
            log(len)
        }
        // 返回字符串
        val data = String(outputStream.toByteArray(), Charset.forName("UTF-8"))
        log(data)
        data
    } catch (e: Exception) {
        log(e.message)
        ""
    }
}


/**
 * 请求网络数据
 * @param queryWord 查询的单词
 * @param callBack 网络回调
 */
fun requestNetData(queryWord: String, callBack: TranslateCallBack<TranslationBean>) {
    try {
        // 先查询本地是否有数据
//        LocalData.read(queryWord)?.let {
//            callBack.onSuccess(Gson().fromJson<TranslationBean>(it, callBack.type))
//            return
//        }

        val url = URL("$BASE_URL$queryWord")
        log("$BASE_URL$queryWord")

        val conn = url.openConnection() as HttpURLConnection

        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        conn.requestMethod = conn.requestMethod

        // 连接成功
        if (conn.responseCode == 200) {
            val ins = conn.inputStream

            // 获取到Json字符串
            val content = getStringFromStream(ins)
            log(content)
            if (content.isNotBlank()) {
                callBack.onSuccess(Gson().fromJson<TranslationBean>(content, callBack.type))
                LocalData.store(queryWord, content)
            } else callBack.onFailure(EMPTY)
        } else callBack.onFailure("错误码：${conn.responseCode}\n错误信息：\n${conn.responseMessage}")
    } catch (e: IOException) {
        callBack.onFailure("无法访问:\n${e.message}")
    } catch (e: JsonSyntaxException) {
        callBack.onFailure("请求格式错误:\n${e.message}")
    }
}
