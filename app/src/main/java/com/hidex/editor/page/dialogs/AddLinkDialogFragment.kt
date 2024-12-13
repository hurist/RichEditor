package com.hidex.editor.page.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hidex.editor.base.BaseBottomSheetFragment
import com.hidex.editor.R
import com.hidex.editor.databinding.DialogAddLinkBinding
import com.hidex.editor.extension.toNetworkUrl

/**
 * @author Sergey Petrov
 */
class AddLinkDialogFragment : BaseBottomSheetFragment() {
    private var onAddClickListener: ((String) -> Unit)? = null
    private var onRemoveClickListener: (() -> Unit)? = null
    private var initialUrl: String? = null
    private var showRemoveButton: Boolean = false
    private var _binding: DialogAddLinkBinding? = null
    private val binding get() = _binding!!


    override val layout: Int
        get() = R.layout.dialog_add_link

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun setupView(dialog: Dialog, view: View) {
        _binding = DialogAddLinkBinding.bind(view)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            binding.urlEditText.requestFocus()
            (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        when {
            showRemoveButton -> {
                binding.titleTextView.text = "delete link"
                binding.submitButton.setText("delete")
                binding.submitButton.setOnClickListener {
                    onRemoveClickListener?.invoke()
                    dialog.dismiss()
                }
            }
            else -> {
                binding.titleTextView.setText("add link")
                binding.submitButton.setText("add")
                binding.submitButton.setOnClickListener {
                    val url = binding.urlTextInputLayout.editText?.text.toString()
                    if (binding.urlTextInputLayout.isInputValid()) {
                        onAddClickListener?.invoke(url.toNetworkUrl())
                        dialog.dismiss()
                    }
                }
            }
        }

        initialUrl?.let { binding.urlEditText.setText(initialUrl) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(initialUrl: String, onAddClickListener: (String) -> Unit, onRemoveClickListener: () -> Unit, showRemoveButton: Boolean): AddLinkDialogFragment {
            val fragment = AddLinkDialogFragment()
            fragment.initialUrl = initialUrl
            fragment.showRemoveButton = showRemoveButton
            fragment.onAddClickListener = onAddClickListener
            fragment.onRemoveClickListener = onRemoveClickListener
            return fragment
        }
    }
}
