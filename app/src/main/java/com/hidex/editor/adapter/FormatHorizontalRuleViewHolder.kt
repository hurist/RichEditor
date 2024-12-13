package com.hidex.editor.adapter

import android.app.Activity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hidex.editor.R
import com.hidex.editor.databinding.ItemFormatHorizontalRuleBinding
import com.hidex.editor.extension.hideKeyboard
import com.hidex.editor.format.Format
import com.hidex.editor.format.FormatType.PARAGRAPH

/**
 * @author Sergey Petrov
 */
class FormatHorizontalRuleViewHolder(
    parent: ViewGroup,
    private val adapter: FormatAdapter
) : BaseFormatViewHolder<Format, ItemFormatHorizontalRuleBinding>(ItemFormatHorizontalRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)) {

    override fun bind(item: Format) {
        format = item
        binding.apply {
            root.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    adapter.focusedItem = item
                    root.background = context.getDrawable(R.drawable.background_hr)
                } else {
                    adapter.focusedItem = null
                    root.background = null
                }
            }
            root.setOnKeyListener { _, _, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_ENTER -> adapter.addBlockFormatItem(adapterPosition + 1, Format(PARAGRAPH))
                        KeyEvent.KEYCODE_DEL -> adapter.removeBlockFormatItemByBackSpace(format)
                    }
                }
                false
            }
            blockMoreLayout.root.setOnClickListener {
                it.requestFocus()
                showBlockMoreOptions(adapter, item)
            }

            blockMoreLayout.root.setOnLongClickListener {
                it.requestFocus()
                (context as Activity).hideKeyboard()
                adapter.itemTouchHelper?.startDrag(this@FormatHorizontalRuleViewHolder)
                true
            }
        }
    }

    override fun requestViewHolderFocus() {
        super.requestViewHolderFocus()
        itemView.requestFocus()
    }
}
