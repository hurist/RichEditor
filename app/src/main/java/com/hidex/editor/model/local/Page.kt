package com.hidex.editor.model.local

/**
 * @author Sergey Petrov
 */
data class Page(
    var id: Long,
    var userId: String,
    var number: Int?,
    var path: String?,
    var url: String?,
    var title: String?,
    var imageUrl: String?,
    var views: Int,
    var authorName: String?,
    var authorUrl: String?,
    var nodes: Nodes,
    var visible: Boolean?,
    var draft: Boolean,
    var deleted: Boolean
) {

    constructor(userId: String) : this(0, userId, null, null, null, "", null, 0, null, null,
        Nodes(arrayListOf()), null, false, false)

    fun isEmpty(): Boolean {
        return title.isNullOrBlank() && imageUrl.isNullOrBlank() && nodes.content.isEmpty()
    }

    companion object {

        const val OLD_DELETED_TITLE = "DЕLЕТЕD"
        const val DELETED_TITLE = "DELETED"
    }
}

/*fun Page.populate(data: PageData): Page {
    path = data.path
    url = data.url
    data.number?.let { number = data.number }
    deleted = data.title == Page.DELETED_TITLE && data.authorName == Page.DELETED_TITLE && data.description.isEmpty() || data.title == Page.OLD_DELETED_TITLE
    if (data.views > 0) { // it's api.telegra.ph bug
        views = data.views
    }
    if (!draft) {
        title = data.title
        imageUrl = data.imageUrl
        authorName = data.authorName
        authorUrl = data.authorUrl
        data.content?.let { nodes = Nodes(it) }
    }
    return this
}*/
