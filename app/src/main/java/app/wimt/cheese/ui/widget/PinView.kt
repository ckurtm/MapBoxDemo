package app.wimt.cheese.ui.widget

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.tes.R

/**
 * nothing much...just a little pulsating view that makes it easier to debug current location.
 * i did not check performance hit on rendering, should be negligible though..hopefully
 */
class PinView : ConstraintLayout {

    private var _pulseColor: Int = Color.BLUE
    private var _pinColor: Int = Color.BLUE
    private var _pinStrokeColor: Int = Color.WHITE


    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PinView, defStyle, 0)
        _pinColor = a.getColor(R.styleable.PinView_pinColor, _pinColor)
        _pulseColor = a.getColor(R.styleable.PinView_pinPulseColor, _pulseColor)
        _pinStrokeColor = a.getColor(R.styleable.PinView_pinStrokeColor, _pinStrokeColor)
        a.recycle()
        setUp()
    }

    fun setUp() {
        View.inflate(context, R.layout.widget_pinview, this)
        val pin = findViewById<View>(R.id.pin_circle)
        val pulse = findViewById<View>(R.id.pulse_circle)
        val stroke = findViewById<View>(R.id.pin_stroke)

        pulse.background = createDrawable(_pulseColor)
        stroke.background = createDrawable(_pinStrokeColor)
        pin.background = createDrawable(_pinColor)

        val pvhX = PropertyValuesHolder.ofFloat("scaleX", 1.5f)
        val pvhY = PropertyValuesHolder.ofFloat("scaleY", 1.5f)
        val pvhA = PropertyValuesHolder.ofFloat("alpha", 0.1f)
        ObjectAnimator.ofPropertyValuesHolder(pulse, pvhX, pvhY, pvhA).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 1000
            start()
        }
    }

    private fun createDrawable(shapeColor: Int): Drawable {
        val drawable = ShapeDrawable(OvalShape())
        drawable.paint.color = shapeColor
        return drawable
    }


}
