package com.hidex.editor.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.TooltipCompat
import com.google.android.material.checkbox.MaterialCheckBox
import com.hidex.editor.R

/**
 * @author Sergey Petrov
 */
class FormatToggleButton : FrameLayout {

    private lateinit var checkBox: MaterialCheckBox
    private lateinit var iconImageView: ImageView

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    protected fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_format_toogle_button, this, true)
        checkBox = view.findViewById(R.id.checkBox)
        iconImageView = view.findViewById(R.id.iconImageView)
        val a = context.obtainStyledAttributes(attrs, R.styleable.FormatToggleButton, defStyleAttr, defStyleRes)

        val icon = a.getResourceId(R.styleable.FormatToggleButton_icon, -1)
        iconImageView.setImageResource(icon)

        a.recycle()
    }

    var isChecked: Boolean
        get() = checkBox.isChecked
        set(value) {
            checkBox.isChecked = value
        }

    override fun setOnClickListener(listener: OnClickListener?) {
        checkBox.setOnClickListener(listener)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        checkBox.isEnabled = enabled
    }

    override fun setTooltipText(tooltipText: CharSequence?) {
        TooltipCompat.setTooltipText(checkBox, tooltipText)
    }
}
