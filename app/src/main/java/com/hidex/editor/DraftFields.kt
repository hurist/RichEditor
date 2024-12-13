package com.hidex.editor

import com.hidex.editor.format.Format

/**
 * @author Sergey Petrov
 */
data class DraftFields(
    val title: String,
    val authorName: String?,
    val authorUrl: String?,
    val formats: List<Format>
)
