package com.hidex.editor

import org.wordpress.aztec.Constants

object Constants {
    fun isDebug() = true
    fun isRelease() = false

    const val SHARED_DATA = "AUTH_DATA" // don't change this name because user will be unauthorized

    const val ERROR_CODE_UNAUTHORIZED = 401

    const val ERROR_ACCESS_TOKEN_INVALID = "ACCESS_TOKEN_INVALID"
    const val ERROR_AUTHOR_URL_INVALID = "AUTHOR_URL_INVALID"
    const val ERROR_SHORT_NAME_REQUIRED = "SHORT_NAME_REQUIRED"
    const val ERROR_CONTENT_TEXT_REQUIRED = "CONTENT_TEXT_REQUIRED"
    const val ERROR_PAGE_ACCESS_DENIED = "PAGE_ACCESS_DENIED"
    const val ERROR_PAGE_SAVE_FAILED = "PAGE_SAVE_FAILED"

    const val PAGE_LIST_LIMIT = 200

    val PAGE_TITLE_LIMIT = 1..256
    val ACCOUNT_NAME_LIMIT = 1..32
    val AUTHOR_NAME_LIMIT = 0..128
    val AUTHOR_URL_LIMIT = 0..512

    val END_OF_BUFFER_MARKER_STRING = Constants.END_OF_BUFFER_MARKER_STRING
    val END_OF_BUFFER_MARKER = Constants.END_OF_BUFFER_MARKER
    val NEWLINE = Constants.NEWLINE
}