package com.iorlova.diploma.UI

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemListener: RecyclerView.OnItemTouchListener {

    private var listener: RecyclerTouchListener
    private var detector: GestureDetectorCompat

    interface RecyclerTouchListener {
        fun onClickItem(view: View, position: Int)
        fun onLongClickItem(view: View, position: Int)
    }

    constructor(ctx: Context, rv: RecyclerView, listener: RecyclerTouchListener) {
        this.listener = listener
        detector =  GestureDetectorCompat(ctx, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                val v: View = rv.findChildViewUnder(e!!.x, e!!.y)!!
                listener.onClickItem(v, rv.getChildAdapterPosition(v))
                return true
            }

            override fun onLongPress(e: MotionEvent?) {
                val v: View = rv.findChildViewUnder(e!!.x, e!!.y)!!
                listener.onLongClickItem(v, rv.getChildAdapterPosition(v))
            }
        })
    }


    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val child = rv.findChildViewUnder(e.getX(), e.getY())
        return (child != null && detector.onTouchEvent(e))

    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
