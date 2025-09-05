package com.mastercard.gateway.android.sampleapp.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mastercard.gateway.android.sampleapp.R

class PaymentOptionsSheet(
    context: Context,
    sheetContent: View
) {

    fun interface Listener {
        fun onOptionClicked(optionType: String)
    }

    private val dialog = BottomSheetDialog(context).apply { setContentView(sheetContent) }
    private val buttonContainer: LinearLayout =
        sheetContent.findViewById(R.id.dynamicButtonContainer)

    fun show(types: List<String>, listener: Listener) {
        buttonContainer.removeAllViews()
        types.forEach { type ->
            val btn = buildButton(buttonContainer.context, type).apply {
                setOnClickListener {
                    listener.onOptionClicked(type)
                    dialog.dismiss()
                }
            }
            buttonContainer.addView(btn)
        }
        dialog.show()
    }

    private fun buildButton(ctx: Context, type: String): Button {
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 10f, ctx.resources.displayMetrics
        ).toInt()

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, marginPx) }

        return Button(ctx).apply {
            layoutParams = lp
            setBackgroundResource(R.color.coral)
            setTextColor(Color.WHITE)
            tag = type
            text = PaymentOptionLabelResolver.labelFor(type)
        }
    }
}
