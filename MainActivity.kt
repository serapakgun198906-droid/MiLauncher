package com.example.milauncher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// ---- Tema renkleri: buradan değiştirin ----
const val BG_TOP = 0xFF0B0B14.toInt()
const val BG_BOTTOM = 0xFF2A1038.toInt()
const val ACCENT = 0xFFFF4F9A.toInt()   // vurgu rengi (pembe)
const val CARD = 0x22FFFFFF

data class App(val label: String, val icon: Drawable, val intent: Intent)

class MainActivity : Activity() {
    private lateinit var list: RecyclerView

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(48), dp(32), dp(48), dp(32))
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(BG_TOP, BG_BOTTOM))
        }
        root.addView(TextClock(this).apply {
            format12Hour = "HH:mm"; format24Hour = "HH:mm"
            textSize = 44f; setTextColor(Color.WHITE); typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        })
        root.addView(TextClock(this).apply {
            format12Hour = "d MMMM EEEE"; format24Hour = "d MMMM EEEE"
            textSize = 16f; setTextColor(ACCENT)
        })
        root.addView(View(this), LinearLayout.LayoutParams(1, 0, 1f))
        root.addView(TextView(this).apply {
            text = "Uygulamalar"; textSize = 18f; setTextColor(Color.WHITE)
            setPadding(dp(8), 0, 0, dp(12))
        })
        list = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            clipToPadding = false; clipChildren = false
            setPadding(0, dp(16), 0, dp(16))
        }
        root.addView(list, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(View(this), LinearLayout.LayoutParams(1, 0, 1f))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        list.adapter = Adapter(loadApps())
        list.post { list.getChildAt(0)?.requestFocus() }
    }

    private fun loadApps(): List<App> {
        val pm = packageManager
        var apps = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0)
        val all = apps + pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        return all.distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName }
            .map {
                val i = pm.getLeanbackLaunchIntentForPackage(it.activityInfo.packageName)
                    ?: pm.getLaunchIntentForPackage(it.activityInfo.packageName)
                App(it.loadLabel(pm).toString(), it.loadIcon(pm), i ?: Intent())
            }.sortedBy { it.label.lowercase() }
    }

    // Home tuşunda uygulamanın kapanmaması için geri tuşunu yut
    @Deprecated("") override fun onBackPressed() {}

    inner class Adapter(private val items: List<App>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount() = items.size
        override fun onCreateViewHolder(p: ViewGroup, t: Int): RecyclerView.ViewHolder {
            val card = LinearLayout(p.context).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                layoutParams = RecyclerView.LayoutParams(dp(150), dp(150)).apply { setMargins(dp(10), dp(10), dp(10), dp(10)) }
                isFocusable = true; isClickable = true
                background = GradientDrawable().apply { cornerRadius = dp(20).toFloat(); setColor(CARD) }
                addView(ImageView(context).apply { id = 1 }, LinearLayout.LayoutParams(dp(72), dp(72)))
                addView(TextView(context).apply {
                    id = 2; textSize = 13f; setTextColor(Color.WHITE); maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END; gravity = Gravity.CENTER
                    setPadding(dp(8), dp(10), dp(8), 0)
                })
                setOnFocusChangeListener { v, f ->
                    v.animate().scaleX(if (f) 1.15f else 1f).scaleY(if (f) 1.15f else 1f).setDuration(120).start()
                    (v.background as GradientDrawable).setColor(if (f) ACCENT else CARD)
                    v.elevation = if (f) dp(12).toFloat() else 0f
                }
            }
            return object : RecyclerView.ViewHolder(card) {}
        }
        override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
            val app = items[pos]
            h.itemView.findViewById<ImageView>(1).setImageDrawable(app.icon)
            h.itemView.findViewById<TextView>(2).text = app.label
            h.itemView.setOnClickListener { runCatching { startActivity(app.intent) } }
        }
    }
}
