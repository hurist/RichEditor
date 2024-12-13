package com.hidex.editor.page.option

import android.app.Dialog
import android.view.View
import com.hidex.editor.base.BaseOptionsFragment
import com.hidex.editor.R

class InsertImageOptionsFragment : BaseOptionsFragment() {

    override val titleResId: Int = R.string.insert_image

    val fromGalleryOption = Option(R.drawable.ic_image, R.string.from_gallery)
    val byUrlOption = Option(R.drawable.ic_insert_link, R.string.by_url)

    override fun setupView(dialog: Dialog, view: View) {
        super.setupView(dialog, view)

        addOptions(fromGalleryOption, byUrlOption)
    }
}
