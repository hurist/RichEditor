package com.hidex.editor

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hidex.editor.format.Format
import com.hidex.editor.format.FormatType
import com.hidex.editor.format.ImageFormat
import com.hidex.editor.format.MediaFormat
import com.hidex.editor.format.VideoFormat
import com.hidex.editor.model.NodeElementData
import com.hidex.editor.util.TelegraphContentConverter
import com.hidex.editor.model.local.Page

class PageEditViewModel: ViewModel() {

    private val telegraphContentConverter = TelegraphContentConverter()
    private var page: Page? = null
    val pageLiveData = MutableLiveData<Pair<Page, List<Format>>>()

    fun openPage(pageId: Long?) {
        val page = Page("10000")
        page.draft = true
        val id = 10000L
        page.id = id
        val pair = Pair(page, convertNodes(page.nodes.content))
        this.page = pair.first
        pageLiveData.postValue(pair)
    }

    fun convertHtml(html: String): List<Format> {
        val nodes = telegraphContentConverter.htmlToNodes(html)
        return convertNodes(nodes)
    }

    private fun convertNodes(nodes: List<NodeElementData>): List<Format> {
        val items = arrayListOf<Format>()
        nodes.forEach { node ->
            val formatType =
                if (node.tag == null && node.text != null) {
                    FormatType.PARAGRAPH
                } else {
                    FormatType.getByTag(node.tag)
                }

            try {
                when (formatType) {
                    FormatType.FIGURE -> items.add(convertFigure(node))
                    FormatType.IMAGE -> items.add(convertImage(node, ""))
                    FormatType.VIDEO -> items.add(convertVideo(node, ""))
                    FormatType.IFRAME -> items.add(convertIframe(node, ""))
                    else -> {
                        if (formatType != null) {
                            val html = telegraphContentConverter.nodesToHtml(arrayListOf(node))
                            items.add(Format(formatType, html = html))
                        } else {
                            throw IllegalArgumentException("formatType is missing for tag=${node.tag}")
                        }
                    }
                }
            } catch (error: java.lang.Exception) {
                error.printStackTrace()
            }
        }
        return items
    }

    private fun convertFigure(node: NodeElementData): Format {
        val children = node.children ?: throw IllegalArgumentException("children can't be null for figure")

        var figureChildren: NodeElementData? = null
        var captionChildren: NodeElementData? = null

        when {
            children[0].tag == "figcaption" -> {
                if (children.size > 1) {
                    figureChildren = children[1]
                }
                captionChildren = children[0].children?.firstOrNull()
            }
            else -> {
                figureChildren = children[0]
                if (children.size > 1) {
                    captionChildren = children[1].children?.firstOrNull()
                }
            }
        }

        val caption = captionChildren?.text.orEmpty()

        if (figureChildren == null) {
            throw IllegalArgumentException("figure can't be null for node=$node")
        } else {
            return when (figureChildren.tag) {
                FormatType.IMAGE.tag -> convertImage(figureChildren, caption)
                FormatType.IFRAME.tag -> convertIframe(figureChildren, caption)
                FormatType.VIDEO.tag -> convertVideo(figureChildren, caption)
                else -> throw IllegalArgumentException("unsupported tag=${figureChildren.tag}")
            }
        }
    }

    private fun convertImage(node: NodeElementData, caption: String): ImageFormat {
        val attrs = node.attrs ?: throw IllegalArgumentException("attrs can't be null for image")
        val src = attrs["src"] ?: throw IllegalArgumentException("src can't be null for image")
        return ImageFormat(src, caption)
    }

    private fun convertIframe(node: NodeElementData, caption: String): MediaFormat {
        val attrs = node.attrs ?: throw IllegalArgumentException("attrs can't be null for iframe")
        val src = attrs["src"] ?: throw IllegalArgumentException("src can't be null for iframe")
        val html = telegraphContentConverter.nodesToHtml(arrayListOf(node))
        return MediaFormat(html, src, caption)
    }

    private fun convertVideo(node: NodeElementData, caption: String): VideoFormat {
        val attrs = node.attrs ?: throw IllegalArgumentException("attrs can't be null for video")
        val src = attrs["src"] ?: throw IllegalArgumentException("src can't be null for video")
        val html = telegraphContentConverter.nodesToHtml(arrayListOf(node))
        return VideoFormat(html, src, caption)
    }
}