package com.pinger.translate.main;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MarkDownGenerate {

    private static File mdFile = new File(System.getProperty("user.home") + "/翻译历史记录.md");

    /**
     * 是否保存过，防止重复保存
     */
    public static boolean isSaved(String Words) {
        StringBuilder mdWords = new StringBuilder();
        mdFile.setExecutable(true);
        mdFile.setReadable(true);
        mdFile.setWritable(true);
        if (mdFile.exists()) {  // 如果文件存在
            FileInputStream fis;
            try {
                fis = new FileInputStream(mdFile);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    mdWords.append(line);
                }
                if (mdWords.indexOf(Words) != -1) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * 存单词
     *
     * @param Words     原文
     * @param translate 译文
     */
    public static void saveWords(String Words, String translate) {
        try {
            mdFile.setExecutable(true);
            mdFile.setReadable(true);
            mdFile.setWritable(true);
            if (!mdFile.exists()) {// 如果文件不存在，则创建该文件
                mdFile.createNewFile();
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(mdFile, true), StandardCharsets.UTF_8);
                writer.write("# 翻译历史记录 \r\n");
                writer.write("这里我们翻译过后的结果集，建议用Markdown编辑器打开它，下方是项目地址。欢迎PR或Issues，喜欢的话给个Star，交个朋友吧。\r\n");
                writer.write("### [AITranslate](https://github.com/PingerOne/AITranslate)\r\n\r\n");

                writer.write("## History：\r\n\r\n");
                writer.write("---\r\n\r\n");
                writer.flush();// 清空缓冲区，立即将输出流里的内容写到文件里
                writer.close();// 关闭输出流，施放资源
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(mdFile, true), StandardCharsets.UTF_8);
            writer.write("\n- " + Words + "\r\n");
            writer.write("\n```\r\n");
            writer.write(translate);
            writer.write("```\r\n");
            writer.flush();// 清空缓冲区，立即将输出流里的内容写到文件里
            writer.close();// 关闭输出流，施放资源
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
