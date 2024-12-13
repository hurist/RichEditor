package com.hidex.editor.base

import com.hidex.editor.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

/**
 * @author Sergey Petrov
 */
abstract class BaseFragment : Fragment() {

    abstract val layoutRes: Int
    private var overlayDialog: Dialog? = null

    private var instanceStateSaved: Boolean = false

    lateinit var scopeName: String

    override fun getContext(): Context {
        return super.getContext() as Context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(layoutRes, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        instanceStateSaved = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        instanceStateSaved = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()

    }

    protected fun getToolbar(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }


    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(resourceId: Int) {
        Toast.makeText(context, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    protected fun showOverlay() {
        /*if (overlayDialog == null) {
            overlayDialog = Dialog(context)
            overlayDialog?.setContentView(R.layout.dialog_progress)
            overlayDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            overlayDialog?.setCancelable(false)
            overlayDialog?.show()
        }*/
    }

    protected fun hideOverlay() {
        overlayDialog?.hide()
        overlayDialog = null
    }

    companion object {
        private const val STATE_SCOPE_NAME = "state_scope_name"
        private const val STATE_SCOPE_WAS_CLOSED = "state_scope_was_closed"
    }
}
