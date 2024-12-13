package com.hidex.editor.adapter

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hidex.editor.extension.showKeyboard
import com.hidex.editor.format.Format
import com.hidex.editor.format.FormatType
import org.wordpress.aztec.AztecText
import com.hidex.editor.R

/**
 * @author Sergey Petrov
 */
abstract class BaseFormatViewHolder<T : Format, V: ViewBinding>: RecyclerView.ViewHolder {

    internal lateinit var format: T
    val context get() = itemView.context
    lateinit var binding: V
    open val editText: AztecText? get() {
        return runCatching {
            itemView.findViewById<AztecText>(R.id.editText)
        }.getOrNull()
    }

    constructor(binding: V): super(binding.root) {
        this.binding = binding
    }
    constructor(itemView: View): super(itemView)

    @Suppress("UNCHECKED_CAST")
    open fun bind(item: Format) {
        format = item as T
    }

    open fun requestViewHolderFocus() {}

    open fun clearViewHolderFocus() {
        itemView.clearFocus()
    }

    open fun toggleFormat(formatType: FormatType) {}

    open fun unbind() {}

    fun getFormat(): T = format

    fun showKeyboard() {
        itemView.postDelayed({ (itemView.context as Activity).showKeyboard() }, 50)
    }

    protected open fun showBlockMoreOptions(adapter: FormatAdapter, format: T) {
        // TODO: 展示块操作弹窗。如删除、复制整个块等
        Toast.makeText(itemView.context, "块点击更多选项", Toast.LENGTH_SHORT).show()
        /*BlockMoreOptionsFragment().apply {
            deleteOption.onClick = {
                AnalyticsHelper.logDeleteBlock()
                adapter.removeBlockFormatItem(adapterPosition)
            }
            duplicateOption.onClick = {
                AnalyticsHelper.logDuplicateBlock()
                adapter.duplicateBlockFormatItem(adapterPosition)
            }
            show((itemView.context as AppCompatActivity).supportFragmentManager)
        }*/
    }
}
