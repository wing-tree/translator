package com.wing.tree.bruni.inPlaceTranslate.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.wing.tree.bruni.inPlaceTranslate.R
import com.wing.tree.bruni.inPlaceTranslate.databinding.IconButtonBinding

class IconButton : FrameLayout {
    private val viewBinding = IconButtonBinding.bind(inflate(context, R.layout.icon_button, this))

    constructor(context: Context) : super(context) {
        getAttrs(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        getAttrs(attrs)
    }

    init {
        with(viewBinding) {
            clipChildren = false
            clipToPadding = false

            root.setOnClickListener {
                imageButton.isPressed = true
                imageButton.performClick()
            }
        }
    }

    private fun getAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.IconButton)

        val src = typedArray.getDrawable(R.styleable.IconButton_src)
        val tint = typedArray.getColorStateList(R.styleable.IconButton_tint)

        with(viewBinding) {
            imageButton.setImageDrawable(src)
            imageButton.imageTintList = tint
        }

        typedArray.recycle()
    }

    fun setOnIconClickListener(l: OnClickListener?) {
        with(viewBinding.imageButton) {
            val onClick = {
                l?.onClick(this)
                isPressed = false
            }

            setOnClickListener {
                onClick.invoke()
            }
        }
    }
}
