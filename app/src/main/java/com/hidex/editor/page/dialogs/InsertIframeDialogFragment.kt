package com.hidex.editor.page.dialogs

import android.app.Dialog
import android.view.View
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hidex.editor.base.BaseBottomSheetFragment
import com.hidex.editor.R
import com.hidex.editor.databinding.DialogAddLinkBinding
import com.hidex.editor.databinding.DialogIframeBinding
import com.hidex.editor.format.MediaFormat

/**
 * @author Sergey Petrov
 */
class InsertIframeDialogFragment : BaseBottomSheetFragment() {

    override val layout: Int
        get() = R.layout.dialog_iframe

    var onAddClickListener: ((MediaFormat) -> Unit)? = null
    private var _binding: DialogIframeBinding? = null
    private val binding get() = _binding!!

    override fun setupView(dialog: Dialog, view: View) {
        _binding = DialogIframeBinding.bind(view)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            binding.urlEditText.requestFocus()
            (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        binding.submitButton.setOnClickListener {
            var url = binding.urlTextInputLayout.editText?.text.toString()
            var isValid = false
            when {
                url.matches(Regex("^(https?://)(www.)?(youtube.com/watch.*v=([a-zA-Z0-9_-]+))\$")) -> {
                    url = "/youtube?url=$url"
                    isValid = true
                }
                url.matches(Regex("^(https?://)(www.)?(youtu.?be)/([a-zA-Z0-9_-]+)\$")) -> {
                    url = "/youtube?url=$url"
                    isValid = true
                }
                url.matches(Regex("^(https?://)(www.)?(vimeo.com)/(\\d+)\$")) -> {
                    url = "/vimeo?url=$url"
                    isValid = true
                }
                url.matches(Regex("^(https?://)(www.|mobile.)?twitter.com/(.+)/status/(\\d+)\$")) -> {
                    url = "/twitter?url=$url"
                    isValid = true
                }
                url.matches(Regex("^(https?://)(t.me|telegram.me|telegram.dog)/([a-zA-Z0-9_]+)/(\\d+)\$")) -> {
                    url = "/telegram?url=$url"
                    isValid = true
                }
            }

            if (binding.urlTextInputLayout.isInputValid { isValid }) {
                val src = "/embed$url"
                val format =
                        MediaFormat(
                                childHtml = "<iframe src=\"$src\" width=\"640\" height=\"360\" allowfullscreen=\"true\" allowtransparency=\"true\" frameborder=\"0\" scrolling=\"no\" />",
                                src = src,
                                caption = ""
                        )
                onAddClickListener?.invoke(format)
                dismiss()
            } else {
                binding.urlTextInputLayout.isErrorEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(onAddClickListener: (MediaFormat) -> Unit): InsertIframeDialogFragment {
            val fragment = InsertIframeDialogFragment()
            fragment.onAddClickListener = onAddClickListener
            return fragment
        }
    }
}
