package com.hidex.editor.page.dialogs

import android.app.Dialog
import android.view.View
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hidex.editor.R
import com.hidex.editor.base.BaseBottomSheetFragment
import com.hidex.editor.databinding.DialogImageUrlCaptionBinding
import com.hidex.editor.extension.toNetworkUrl

/**
 * @author Sergey Petrov
 */
class InsertImageUrlCaptionDialogFragment : BaseBottomSheetFragment() {
    var onAddClickListener: ((String, String) -> Unit)? = null
    private var imageUrl: String? = null

    override val layout: Int
        get() = R.layout.dialog_image_url_caption
    private var _binding: DialogImageUrlCaptionBinding? = null
    private val binding get() = _binding!!

    override fun setupView(dialog: Dialog, view: View) {
        _binding = DialogImageUrlCaptionBinding.bind(view)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            binding.urlEditText.requestFocus()
            (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        binding.submitButton.setOnClickListener {
            val caption = binding.captionEditText.text.toString().trim { it <= ' ' }
            val url = binding.urlTextInputLayout.editText?.text.toString()
            if (binding.urlTextInputLayout.isInputValid()) {
                onAddClickListener?.invoke(url.toNetworkUrl(), caption)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(imageUrl: String?, onAddClickListener: (String, String) -> Unit): InsertImageUrlCaptionDialogFragment {
            val fragment = InsertImageUrlCaptionDialogFragment()
            fragment.imageUrl = imageUrl
            fragment.onAddClickListener = onAddClickListener
            return fragment
        }
    }
}
