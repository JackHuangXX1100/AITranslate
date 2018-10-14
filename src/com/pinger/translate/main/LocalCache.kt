package com.pinger.translate.main

import org.apache.commons.lang.StringUtils
import org.jetbrains.annotations.NonNls
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

/**
 * Created by ice1000 on 2017/4/13.
 * 本地缓存工具类，将翻译过的单词存入到本地的properties文件中
 */
object LocalData {

    private val p = Properties()
    private val f = File(System.getProperty("user.home") + "/AITranslate_Cache.properties")

    init {
        if (!f.exists()) f.createNewFile()
        p.load(FileReader(f))
    }

    private fun save() = p.store(FileWriter(f), "save translate worlds")

    /**
     * 读取保存过的记录
     */
    fun read(@NonNls key: String): String? = p.getProperty(StringUtils.uncapitalize(key))

    /**
     * 以键值对的形式保存查询单词的记录
     */
    fun store(@NonNls key: String, @NonNls value: String) {
        p[StringUtils.uncapitalize(key)] = value
        save()
    }

    fun clear() {
        p.clear()
        save()
    }

}
