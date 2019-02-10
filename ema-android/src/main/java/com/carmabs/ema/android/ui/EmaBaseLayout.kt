package com.carmabs.ema.android.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein


/**
 *
 * Abstract base class to implement custom layouts. It handles dependency injection as well.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaBaseLayout : FrameLayout, KodeinAware {

    override val kodein: Kodein by closestKodein()

    constructor(context: Context) : super(context) {
        onCreateView(context)
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        onCreateView(context, attrs)
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr) {
        onCreateView(context, attrs)
    }


    private fun onCreateView(context: Context, attrs: AttributeSet? = null) {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(getLayout(), this) as ViewGroup
        setup(v.getChildAt(0))
        attrs?.let {
            val attributes = getAttributes()
            attributes?.let { at ->
                val ta = context.obtainStyledAttributes(attrs, at, 0, 0)
                try {
                    setupAttributes(ta!!)
                } finally {
                    ta.recycle()
                }
            }

        }

    }

    /**
     * Method called once the custom attributes has been set by [EmaBaseLayout.getAttributes]
     * @param ta are the custom attributes inflated
     */
    abstract fun setupAttributes(ta: TypedArray)

    /**
     * @return the custom attributes than can be used for the custom layout
     */
    abstract fun getAttributes(): IntArray?

    /**
     * Method called once the layout has been inflated implementing the methods [EmaBaseLayout.getLayout]
     * @param mainLayout is the layout inflated instance
     */
    abstract fun setup(mainLayout: View)


    /**
     * @return the layout of the fragment to be inflated in the [EmaBaseLayout.onCreateView]
     */
    protected abstract fun getLayout(): Int
}