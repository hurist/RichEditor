package com.hidex.editor.adapter

import android.text.SpannableStringBuilder
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hidex.editor.R

import com.hidex.editor.Constants.END_OF_BUFFER_MARKER_STRING
import com.hidex.editor.Constants.PAGE_TITLE_LIMIT
import com.hidex.editor.EditorMode
import com.hidex.editor.format.Format
import com.hidex.editor.format.FormatType
import com.hidex.editor.format.FormatType.*
import com.hidex.editor.format.*
import java.util.Collections

/**
 * @author Sergey Petrov
 */
class FormatAdapter(
    val mode: EditorMode,
    val onFocusItemChanged: (Format?) -> Unit,
    val onTextSelected: (Boolean, format: Format?, List<FormatType>) -> Unit,
    val onPaste: (html: String) -> List<Format>,
    val onItemChanged: (Format?) -> Unit,
    var items: ArrayList<Format> = arrayListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemTouchHelper: ItemTouchHelper? = null
    private var recyclerView: RecyclerView? = null

    var focusedItem: Format? = null
        set(value) {
            if (field != value) clearFocusForFocusedFormatItem()
            field = value
            onFocusItemChanged.invoke(field)
        }

    var pageTitle: String? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
        itemTouchHelper = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            return PageHeaderViewHolder(mode, parent, this)
        } else {
            return when (val formatType = FormatType.getByOrdinal(viewType)) {
                BREAK_LINE,
                PARAGRAPH,
                ORDERED_LIST,
                UNORDERED_LIST,
                STRONG,
                STRIKETHROUGH,
                LINK,
                ITALIC,
                BOLD,
                UNDERLINE -> FormatTextViewHolder(R.layout.item_format_text, parent, this, onTextSelected, onPaste)

                PREFORMAT -> FormatTextViewHolder(R.layout.item_format_preformat, parent, this, onTextSelected, onPaste)

                QUOTE -> FormatTextViewHolder(R.layout.item_format_quote, parent, this, onTextSelected, onPaste)

                ASIDE -> FormatTextViewHolder(R.layout.item_format_aside, parent, this, onTextSelected, onPaste)

                SUB_HEADING,
                HEADING -> FormatTextViewHolder(R.layout.item_format_heading, parent, this, onTextSelected, onPaste)

                HORIZONTAL_RULE -> FormatHorizontalRuleViewHolder(parent, this)

                IMAGE -> FormatImageViewHolder(parent, this)

                IFRAME,
                VIDEO -> FormatMediaViewHolder(parent, this)

                else -> throw IllegalArgumentException("formatType=$formatType is not implemented.")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == TYPE_HEADER && holder is PageHeaderViewHolder) {
            holder.bind(pageTitle)
        } else if (holder is BaseFormatViewHolder<*, *>) {
            holder.bind(getFormatItem(position))
        } else throw IllegalArgumentException("View holder type=$holder is not implemented.")
    }

    override fun getItemCount(): Int {
        return items.size + 1 // '+ 1' because we have title at header
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> TYPE_HEADER
            else -> getFormatItem(position).type.ordinal
        }
    }

    fun submitList(newItems: List<Format>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun isPageTitleValid(): Boolean {
        val pageTitle = pageTitle
        return pageTitle != null && pageTitle.trim().length in PAGE_TITLE_LIMIT
    }

    fun getPageHeaderViewHolder(): PageHeaderViewHolder? {
        return getViewHolderAtPosition(0) as? PageHeaderViewHolder?
    }

    fun getPositionForFocusedItem(): Int {
        val item = focusedItem
        if (item != null) {
            return getPositionForItem(item)
        }
        return RecyclerView.NO_POSITION
    }

    fun toggleFormat(formatType: FormatType) {
        val holder = getFocusedItemViewHolder()
        if (holder != null && holder is FormatTextViewHolder) {
            holder.toggleFormat(formatType)
        } else {
            addBlockFormatItem(Format(formatType))
        }

        (holder as? BaseFormatViewHolder<*, *>)?.showKeyboard()
    }

    fun duplicateBlockFormatItem(position: Int) {
        val item =
            when (val format = getFormatItem(position)) {
                is ImageFormat -> ImageFormat(format.url, format.caption)
                is VideoFormat -> VideoFormat(format.childHtml, format.src, format.caption)
                is MediaFormat -> MediaFormat(format.childHtml, format.src, format.caption)
                else -> Format(format.type, format.toHtml())
            }

        addItem(position + 1, item)
        onFormatItemAdded(position + 1, item)
    }

    fun replaceBlockFormatItem(item: Format, newItems: List<Format>) {
        if (newItems.isNotEmpty()) {
            val position = getPositionForItem(item)
            val viewHolder = getViewHolderAtPosition(position) as? BaseFormatViewHolder<*, *>
            viewHolder?.unbind()
            items.removeAt(position - 1)
            items.addAll(position - 1, newItems)
            notifyItemChanged(position)
            if (newItems.size > 1) {
                notifyItemRangeInserted(position + 1, newItems.size)
            }
            onFormatItemAdded(position, newItems.first())
        }
    }

    fun removeBlockFormatItem(item: Format) {
        val positionToRemove = getPositionForItem(item)
        removeBlockFormatItem(positionToRemove)
    }

    fun removeBlockFormatItem(position: Int) {
        val viewHolder = getViewHolderAtPosition(position) as? BaseFormatViewHolder<*, *>
        viewHolder?.unbind()

        removeItem(position)

        focusedItem = null
        getPageHeaderViewHolder()?.showContentPlaceholder(items.isEmpty())
    }

    fun removeBlockFormatItemByBackSpace(format: Format) {
        var prevItemHtml = ""
        try {
            val position = getPositionForItem(format)
            val viewHolder = getViewHolderAtPosition(position) as? BaseFormatViewHolder<*, *>
            val prevViewHolder = getViewHolderAtPosition(position - 1) as? BaseFormatViewHolder<*, *>
            val spannableString = SpannableStringBuilder(viewHolder?.editText?.text ?: END_OF_BUFFER_MARKER_STRING)
            when (prevViewHolder) {
                is FormatTextViewHolder -> {
                    val isNotListFormatOrEmpty = format.type != UNORDERED_LIST && format.type != ORDERED_LIST || spannableString.toString() == END_OF_BUFFER_MARKER_STRING
                    if (isNotListFormatOrEmpty) {
                        prevItemHtml = prevViewHolder.format.toHtml()
                        prevViewHolder.appendText(spannableString)
                        removeBlockFormatItem(format)
                    }

                    prevViewHolder.requestViewHolderFocus()
                }
                is FormatImageViewHolder, is FormatMediaViewHolder, is FormatHorizontalRuleViewHolder -> {
                    if (spannableString.toString() == END_OF_BUFFER_MARKER_STRING) {
                        removeBlockFormatItem(format)
                    }
                    prevViewHolder.requestViewHolderFocus()
                }
                else -> {
                    removeBlockFormatItem(format)
                    prevViewHolder?.requestViewHolderFocus()
                }
            }
        } catch (e: Exception) {
            val error = IllegalStateException("Error on removeBlockFormatItemByBackSpace for prevItemHtml=$prevItemHtml", e)
            error.printStackTrace()
        }
    }

    fun addBlockFormatItem(format: Format) {
        addBlockFormatItems(listOf(format))
    }

    fun addBlockFormatItem(index: Int, format: Format, ignoreSplit: Boolean = false) {
        addBlockFormatItems(index, listOf(format), ignoreSplit)
    }

    fun addBlockFormatItems(formats: List<Format>) {
        var position = getPositionForFocusedItem()
        if (position == RecyclerView.NO_POSITION) {
            position = itemCount // add to the end of list
        } else {
            position += 1 // add after focused item
        }

        addBlockFormatItems(position, formats)
    }

    fun addBlockFormatItems(index: Int, formats: List<Format>, ignoreSplit: Boolean = false) {
        if (formats.isNotEmpty()) {
            getPageHeaderViewHolder()?.showContentPlaceholder(false)

            var position = index
            var tailFormatItem: Format? = null

            if (!ignoreSplit) {
                val focusedViewHolder = getFocusedItemViewHolder() as? BaseFormatViewHolder<*, *>
                with(focusedViewHolder) {
                    if (this is FormatTextViewHolder && getText().toString() != END_OF_BUFFER_MARKER_STRING) {
                        if (isSelectionEmpty()) {
                            when {
                                isCursorPositionAtStart() -> position -= 1
                                isCursorPositionAtEnd() -> position = index
                                else -> tailFormatItem = splitFormatItemByCursorPosition()
                            }
                        } else {
                            tailFormatItem = splitFormatItemByCursorPosition()
                        }
                    }
                }
            }

            addItems(position, formats)
            if (tailFormatItem != null) {
                addItem(position + formats.size, requireNotNull(tailFormatItem))
            }

            onFormatItemAdded(position, formats.first())
        }
    }

    fun moveBlockFormatItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i - 1, i)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i - 1, i - 2)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getFormatItem(position: Int): Format {
        return items[position - 1] // '- 1' because we have title at header
    }

    fun requestFocusForFormatItem(item: Format?) {
        if (item != null) {
            recyclerView?.postDelayed({
                val holder = getViewHolderAtPosition(getPositionForItem(item)) as? BaseFormatViewHolder<*, *>
                holder?.requestViewHolderFocus()
            }, 50)
        }
    }

    private fun clearFocusForFocusedFormatItem() {
        val holder = getFocusedItemViewHolder()
        holder?.clearViewHolderFocus()
    }

    private fun getFocusedItemViewHolder(): BaseFormatViewHolder<*, *>? {
        val item = focusedItem
        if (item != null) {
            return getViewHolderAtPosition(getPositionForItem(item)) as? BaseFormatViewHolder<*, *>
        }
        return null
    }

    private fun onFormatItemAdded(position: Int, format: Format) {
        focusedItem = format
        requestFocusForFormatItem(format)
        recyclerView?.post {
            val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
            if (layoutManager != null) {
                if (position > layoutManager.findLastCompletelyVisibleItemPosition() - 1) {
                    layoutManager.scrollToPosition(position)
                }
            }
        }
    }

    private fun getPositionForItem(item: Format): Int {
        items.forEachIndexed { index, format ->
            if (item.id == format.id) return index + 1 // '+ 1' because we have title at header
        }
        return RecyclerView.NO_POSITION
    }

    private fun getViewHolderAtPosition(position: Int): RecyclerView.ViewHolder? {
        return recyclerView?.findViewHolderForAdapterPosition(position)
    }

    private fun removeItem(position: Int) {
        try {
            items.removeAt(position - 1)
            notifyItemRemoved(position)
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun addItem(position: Int, item: Format) {
        try {
            items.add(position - 1, item)
            notifyItemInserted(position)
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun addItems(position: Int, formats: List<Format>) {
        try {
            items.addAll(position - 1, formats)
            notifyItemRangeInserted(position, formats.size)
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    companion object {
        const val TYPE_HEADER = 465678
    }
}
