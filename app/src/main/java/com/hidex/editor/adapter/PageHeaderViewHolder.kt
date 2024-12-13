package com.hidex.editor.adapter

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.hidex.editor.Constants
import com.hidex.editor.Constants.PAGE_TITLE_LIMIT
import com.hidex.editor.EditorMode
import com.hidex.editor.R
import com.hidex.editor.extension.showKeyboard
import com.hidex.editor.format.Format
import com.hidex.editor.format.FormatType
import com.hidex.editor.util.CharacterCountErrorWatcher

/**
 * @author Sergey Petrov
 */
class PageHeaderViewHolder(
    private val mode: EditorMode,
    parent: ViewGroup,
    private val adapter: FormatAdapter
) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_page_header, parent, false)) {

    private var titleTextWatcher: TextWatcher? = null
    private val titleEditText: TextInputEditText = itemView.findViewById(R.id.titleEditText)
    private val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
    private val emptyContentTextView: TextView = itemView.findViewById(R.id.emptyContentTextView)
    private val titleLimitTextView: TextView = itemView.findViewById(R.id.titleLimitTextView)

    fun bind(pageTitle: String?) {
        with(itemView) {
            if (!pageTitle.isNullOrEmpty() && titleEditText.text.toString() != pageTitle) {
                titleEditText.setText(pageTitle)
            }
            if (titleTextWatcher == null) {
                titleTextWatcher = getTitleTextWatcher()
            }
            titleEditText.removeTextChangedListener(titleTextWatcher)
            titleEditText.addTextChangedListener(titleTextWatcher)
            showContentPlaceholder(mode == EditorMode.Create)
            contentTextView.setOnClickListener {
                adapter.addBlockFormatItem(Format(FormatType.PARAGRAPH))
                postDelayed({ (context as Activity).showKeyboard() }, 100)
            }
        }
    }

    fun showContentPlaceholder(isVisible: Boolean) {
        with(itemView) {
            if (isVisible) {
                contentTextView.visibility = VISIBLE
            } else {
                contentTextView.visibility = GONE
                emptyContentTextView.visibility = GONE
            }
        }
    }

    fun isValid(): Boolean {
        var isValid = true

        with(itemView) {
            if (!adapter.isPageTitleValid()) {
                isValid = false
                titleEditText.removeTextChangedListener(characterCountErrorWatcher)
                titleEditText.addTextChangedListener(characterCountErrorWatcher)
            }

            if (adapter.items.isEmpty()) {
                isValid = false
                emptyContentTextView.visibility = VISIBLE
            }
            return isValid
        }
    }

    private fun getTitleTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.pageTitle = s.toString()
                adapter.onItemChanged.invoke(null)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (before == 0 && count == 1 && s[start] == Constants.NEWLINE) {
                    titleEditText.text?.delete(start, start + 1)

                    if (adapter.items.firstOrNull() == null) {
                        adapter.addBlockFormatItem(Format(FormatType.PARAGRAPH))
                    } else {
                        adapter.focusedItem = adapter.items.firstOrNull()
                        adapter.requestFocusForFormatItem(adapter.focusedItem)
                    }
                }
            }
        }
    }

    private val characterCountErrorWatcher by lazy { CharacterCountErrorWatcher(titleEditText, titleLimitTextView, PAGE_TITLE_LIMIT) }
}
