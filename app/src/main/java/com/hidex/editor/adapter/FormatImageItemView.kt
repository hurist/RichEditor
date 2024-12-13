package com.hidex.editor.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import com.hidex.editor.base.BaseFrameLayout
import com.hidex.editor.extension.loadImage
import com.hidex.editor.extension.setGone
import com.hidex.editor.format.ImageFormat
import com.hidex.editor.R

/**
 * @author Sergey Petrov
 */
class FormatImageItemView(
    context: Context
) : BaseFrameLayout(context) {

    override val layoutRes: Int
        get() = R.layout.item_format_image

    lateinit var progressBar: ProgressBar
    lateinit var previewImageView: ImageView
    lateinit var imageView: ImageView
    lateinit var captionEditText: EditText
    lateinit var blockMoreLayout: View


    override fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        super.init(context, attrs, defStyleAttr, defStyleRes)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        progressBar = findViewById(R.id.progressBar)
        previewImageView = findViewById(R.id.previewImageView)
        imageView = findViewById(R.id.imageView)
        captionEditText = findViewById(R.id.captionEditText)
        blockMoreLayout = findViewById(R.id.blockMoreLayout)
    }

    fun bind(format: ImageFormat) {
        progressBar.setGone(false)

        if (format.url.startsWith("file:")) {
            //presenter.uploadImage(format)
            showPreview(format.url)
        } else {
            showImage(format.getFullUrl())
        }

        if (format.caption.isNotEmpty()) {
            captionEditText.setText(format.caption)
        }
    }

    fun unbind() {
        previewImageView.setImageBitmap(null)
        imageView.setImageBitmap(null)
        captionEditText.text = null
    }


    private fun showPreview(url: String) {
        previewImageView.setGone(false)
        previewImageView.loadImage(context, url)
    }

    private fun showImage(url: String) {
        imageView.loadImage(context, url,
                onLoaded = {
                    previewImageView.setImageBitmap(null)
                    previewImageView.setGone(true)
                    progressBar.setGone(true)
                },
                onFailed = { error ->
                    //Timber.e(error)
                    showError("Failed to load image")
                }
        )
    }
}
