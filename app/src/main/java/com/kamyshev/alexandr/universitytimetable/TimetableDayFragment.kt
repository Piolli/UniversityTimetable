package com.kamyshev.alexandr.universitytimetable


import android.os.Bundle
import android.support.annotation.UiThread
import android.support.v4.app.Fragment
import android.util.LayoutDirection
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import kotlinx.android.synthetic.main.fragment_timetable_day.*
import android.widget.TextView
import kotlin.concurrent.thread


/**
 * A simple [Fragment] subclass.
 */
class TimetableDayFragment : Fragment() {

    private lateinit var day: Timetable.Week.Day

    private lateinit var adapter: TimetableListViewAdapter

    companion object {
        fun createInstance(day: Timetable.Week.Day): TimetableDayFragment {
            val fragment = TimetableDayFragment()
            fragment.day  = day
            fragment.adapter = TimetableListViewAdapter(fragment.day)
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_timetable_day, container, false)

        if(day.typeOfDay == Timetable.Week.Day.TypeOfDay.REST && view != null) {
            val lparams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lparams.gravity = Gravity.CENTER_HORIZONTAL
            val tv = TextView(activity)
            tv.layoutParams = lparams
            tv.text = "Сегодня нет занятий"
            tv.gravity = Gravity.CENTER
            tv.textSize = 24f
            (view as FrameLayout).addView(tv)
        }
        else {
            view?.findViewById<ListView>(R.id.list_view)?.adapter = adapter
        }

        return view
    }



//    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        thread {
//            Log.d("TimetableDayFragment", "In thread lessons[0]: ${if(day.typeOfDay != Timetable.Week.Day.TypeOfDay.REST) day.lessons[0] else "REST"}")
//            if(day.typeOfDay == Timetable.Week.Day.TypeOfDay.REST && view != null) {
//                activity.runOnUiThread {
//                    val lparams = LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                    lparams.gravity = Gravity.CENTER_HORIZONTAL
//                    val tv = TextView(activity)
//                    tv.layoutParams = lparams
//                    tv.text = "Сегодня нет занятий"
//                    tv.gravity = Gravity.CENTER
//                    tv.textSize = 24f
//                    (view as FrameLayout)?.addView(tv)
//                }
//            }
//            else {
//                val adapter = TimetableListViewAdapter(day)
//                activity.runOnUiThread {
//                    list_view?.adapter = adapter
//                }
//            }
//        }
//    }


}
