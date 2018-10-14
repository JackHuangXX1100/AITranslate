package com.pinger.translate.main

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.pinger.translate.net.NetWorkUtils

/**
 * Created by Pinger on 2016/12/10.
 * 翻译选中的单词
 * 需要完成的逻辑有三段：
 * 第一：获取选中的单词
 * 第二：联网查询选中的单词意思，返回json，然后解析
 * 第三：弹出PopupWindow，显示结果
 */
class TranslateAction : AnAction() {

    private lateinit var editor: Editor
    private var latestClickTime = 0L  // 上一次的点击时间

    override fun actionPerformed(e: AnActionEvent) {
        if (!isFastClick(1000)) {
            // 获取动作编辑器
            editor = e.getData(PlatformDataKeys.EDITOR) ?: return

            // 获取选择模式对象
            val model = editor.selectionModel

            // 选中文字
            val selectedText = model.selectedText ?: return
            if (selectedText.isBlank()) return

            // API查询
            NetWorkUtils.requestGet(editor,selectedText)
        }
    }


    /**
     * 屏蔽多次选中
     */
    private fun isFastClick(timeMillis: Long): Boolean {
        val begin = System.currentTimeMillis()
        val end = begin - latestClickTime
        if (end in 1..(timeMillis - 1)) return true
        latestClickTime = begin
        return false
    }
}

