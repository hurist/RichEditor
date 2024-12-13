package com.hidex.editor.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.apply
import com.hidex.editor.R

/**
 * @author Sergey Petrov
 */
abstract class BaseBottomSheetFragment : DialogFragment() {

    protected var mBottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            this@BaseBottomSheetFragment.onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            this@BaseBottomSheetFragment.onSlide(bottomSheet, slideOffset)
        }
    }

    @get:LayoutRes
    abstract val layout: Int

    private var instanceStateSaved: Boolean = false

    override fun getContext(): Context {
        return super.getContext() as Context
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogStyle
    }

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

    // It will be valid only for 'onDestroy()' method
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogStyle)
        dialog.behavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
        return dialog
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, layout, null)
        dialog.setContentView(contentView)
        setupView(dialog, contentView)
    }

    abstract fun setupView(dialog: Dialog, view: View)

    protected fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_HIDDEN -> dismiss()
            BottomSheetBehavior.STATE_EXPANDED -> {
                val newMaterialShapeDrawable = createMaterialShapeDrawable(bottomSheet)
                ViewCompat.setBackground(bottomSheet, newMaterialShapeDrawable)
            }
        }
    }

    protected fun onSlide(bottomSheet: View, slideOffset: Float) {
    }

    fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(resourceId: Int) {
        Toast.makeText(context, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, tag)
    }

    private fun createMaterialShapeDrawable(bottomSheet: View): MaterialShapeDrawable? {
        val shapeAppearanceModel = ShapeAppearanceModel
            .builder(context, 0, R.style.ShapeAppearance)
            .build()
        val currentMaterialShapeDrawable: MaterialShapeDrawable = bottomSheet.background as MaterialShapeDrawable
        val newMaterialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        newMaterialShapeDrawable.initializeElevationOverlay(context)
        newMaterialShapeDrawable.fillColor = currentMaterialShapeDrawable.getFillColor()
        newMaterialShapeDrawable.setTintList(currentMaterialShapeDrawable.getTintList())
        newMaterialShapeDrawable.setElevation(currentMaterialShapeDrawable.getElevation())
        newMaterialShapeDrawable.setStrokeWidth(currentMaterialShapeDrawable.getStrokeWidth())
        newMaterialShapeDrawable.setStrokeColor(currentMaterialShapeDrawable.getStrokeColor())
        return newMaterialShapeDrawable
    }
}
