package com.example.masandaza

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

object BottomNavigationHelper {

    fun attach(activity: Activity, selected: String) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return

        if (content.childCount == 0) return

        if (content.getTag(R.id.bottom_nav_attached) == true) return

        val screen = content.getChildAt(0)
        content.removeView(screen)

        val wrapper = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        wrapper.addView(
            screen,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        val bar = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            elevation = 8f
        }
        val items = listOf(
            Triple("Home", "🏠", DasboardActivity::class.java),
            Triple("Bookings", "🧹", BookingActivity::class.java),
            Triple("Settings", "⚙️", SettingsActivity::class.java)
        )
        items.forEach { (label, symbol, destination) ->
            val item = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(4, 8, 4, 8)
                setBackgroundColor(Color.WHITE)
            }
            val icon = TextView(activity).apply {
                text = symbol
                textSize = 22f
                gravity = Gravity.CENTER
                setTextColor(
                    if (label == selected) Color.rgb(23, 105, 170)
                    else Color.DKGRAY
                )
            }
            val text = TextView(activity).apply {
                this.text = label
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(
                    if (label == selected) Color.rgb(23, 105, 170)
                    else Color.DKGRAY
                )
            }
            item.addView(icon)
            item.addView(text)

            item.setOnClickListener {
                if (label != selected) {
                    val intent = Intent(activity, destination)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    activity.startActivity(intent)
                }
            }
            bar.addView(
                item,
                LinearLayout.LayoutParams(0, dp(activity, 60), 1f)
            )
        }
        wrapper.addView(
            bar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(activity, 60)
            )
        )
        content.addView(
            wrapper,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        content.setTag(R.id.bottom_nav_attached, true)
    }

    private fun dp(activity: Activity, value: Int): Int {
        return (value * activity.resources.displayMetrics.density).toInt()
    }
}