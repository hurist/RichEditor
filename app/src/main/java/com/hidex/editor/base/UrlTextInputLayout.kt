package com.hidex.editor.base

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import com.hidex.editor.extension.colorStateList
import com.hidex.editor.extension.isUrl
import kotlin.let
import kotlin.text.isNullOrEmpty
import com.hidex.editor.R

/**
 * @author Sergey Petrov
 */
class UrlTextInputLayout : TextInputLayout {

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setErrorTextColor(context.colorStateList(R.color.error))
        addOnEditTextAttachedListener {
            if (editText?.hint.isNullOrEmpty()) {
                editText?.hint = "https://"
            }
        }
    }

    fun isInputValid(condition: (() -> Boolean)? = null): Boolean {
        editText?.let {
            val value = it.text.toString()
            val isValid = value.isUrl() && (condition == null || condition.invoke())
            error = "Invalid URL"
            isErrorEnabled = !isValid
            return isValid
        }
        return false
    }
}
