package com.hidex.editor.format

/**
 * @author Sergey Petrov
 */
data class VideoFormat(
    override val childHtml: String,
    override val src: String,
    override var caption: String,
    override var type: FormatType = FormatType.VIDEO
) : MediaFormat(childHtml, src, caption, type) {

    override fun getUrl(): String {
        return src//ServerManager.endPoint + src TODO: 远程视频地址
    }
}
