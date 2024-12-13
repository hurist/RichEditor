package com.hidex.editor.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import com.hidex.editor.databinding.ItemFormatMediaBinding
import com.hidex.editor.R
import com.hidex.editor.extension.hideKeyboard
import com.hidex.editor.extension.layoutInflater
import com.hidex.editor.format.Format
import com.hidex.editor.format.FormatType
import com.hidex.editor.format.MediaFormat
import com.hidex.editor.format.VideoFormat

/**
 * @author Sergey Petrov
 */
class FormatMediaViewHolder(
    parent: ViewGroup,
    private val adapter: FormatAdapter
) : BaseFormatViewHolder<MediaFormat, ItemFormatMediaBinding>(
    ItemFormatMediaBinding.inflate(parent.layoutInflater, parent, false)
) {

    override fun bind(item: Format) {
        super.bind(item)

        binding.apply {
            root.setOnClickListener { requestViewHolderFocus() }

            configureWebView(webView)

            val webViewLayoutParams = webView.layoutParams
            if (format is VideoFormat || format.src.contains("youtube") || format.src.contains("vimeo")) {
                webViewLayoutParams.height = context.resources.getDimension(R.dimen.web_view_video_height).toInt()
            } else {
                webViewLayoutParams.height = MATCH_PARENT
            }

            webView.loadUrl(/*ServerManager.endPoint +*/ format.src) // TODO: 链接类型的媒体资源地址

            captionEditText.setText(format.caption)

            captionEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    adapter.focusedItem = item
                    captionEditText.setSelection(captionEditText.text.length)
                } else {
                    adapter.focusedItem = null
                }
            }

            captionEditText.removeTextChangedListener(textWatcher)
            captionEditText.addTextChangedListener(textWatcher)

            captionEditText.setOnKeyListener { _, _, event ->
                if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL) {
                    if (captionEditText.selectionStart == 0) {
                        adapter.removeBlockFormatItemByBackSpace(format)
                    }
                    true
                } else false
            }

            moreOverlay.blockMoreLayout.setOnClickListener {
                requestViewHolderFocus()
                showBlockMoreOptions(adapter, format)
            }

            moreOverlay.blockMoreLayout.setOnLongClickListener {
                (context as Activity).hideKeyboard()
                adapter.itemTouchHelper?.startDrag(this@FormatMediaViewHolder)
                adapter.focusedItem = format
                adapter.requestFocusForFormatItem(format)
                true
            }
        }
    }

    override fun requestViewHolderFocus() {
        super.requestViewHolderFocus()
        binding.captionEditText.requestFocus()
    }

    override fun showBlockMoreOptions(adapter: FormatAdapter, format: MediaFormat) {
        Toast.makeText(itemView.context, "媒体-链接 showBlockMoreOptions", Toast.LENGTH_SHORT).show()
        // TODO: 媒体-链接 showBlockMoreOptions
        /*MediaBlockMoreOptionsFragment.newInstance(format.getUrl()).apply {
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

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(webView: WebView) {
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        webView.settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            format.caption = binding.captionEditText.text.toString()
            adapter.onItemChanged.invoke(format)
        }

        override fun beforeTextChanged(source: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(source: CharSequence?, start: Int, before: Int, count: Int) {
            val s = source.toString()
            if (s.contains("\n")) {
                val text = s.replace("\n".toRegex(), "")
                binding.captionEditText.setText(text)
                adapter.addBlockFormatItem(adapterPosition + 1, Format(FormatType.PARAGRAPH))
            }
        }
    }
}
