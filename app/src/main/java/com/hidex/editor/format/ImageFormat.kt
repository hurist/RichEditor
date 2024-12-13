package com.hidex.editor.format

import com.telex.base.presentation.page.adapter.ImageUploadStatus

/**
 * @author Sergey Petrov
 */
data class ImageFormat(
    var url: String,
    var caption: String
) : Format(FormatType.IMAGE) {

    var uploadStatus: ImageUploadStatus? = null

    override fun toHtml(): String {
        return "<figure><img src=\"$url\"/><figcaption>$caption</figcaption></figure>"
    }

    fun getFullUrl() = url// if (!url.isUrl()) ServerManager.endPoint + url else url // TODO:图片远程地址

}
