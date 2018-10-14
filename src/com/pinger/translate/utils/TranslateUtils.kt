package com.pinger.translate.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.JBColor
import java.awt.Color


/**
 * 翻译插件中的工具类
 */
object TranslateUtils {

    /**
     * 弹出对话框
     * @param result 要展示的文本
     */
    fun showPopupWindow(editor: Editor, result: String) {
        ApplicationManager.getApplication().invokeLater {
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(result, null, JBColor(Color(238, 172, 62), Color(73, 117, 73)), null)
                    .setFadeoutTime(15000)
                    .setHideOnAction(true)
                    .createBalloon()
                    .show(JBPopupFactory.getInstance().guessBestPopupLocation(editor), Balloon.Position.below)
        }
    }

}