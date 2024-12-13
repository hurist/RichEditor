package com.hidex.editor.model

/**
 * @author Sergey Petrov
 */
data class NodeElementData(
    var tag: String? = null,
    var attrs: MutableMap<String, String>? = mutableMapOf(),
    var children: ArrayList<NodeElementData>? = arrayListOf(),
    var text: String? = null
)
