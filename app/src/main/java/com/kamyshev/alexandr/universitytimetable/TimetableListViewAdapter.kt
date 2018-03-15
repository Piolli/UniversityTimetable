package com.kamyshev.alexandr.universitytimetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.timetable_item.view.*

/**
 * Created by alexandr on 15/03/2018.
 */
class TimetableListViewAdapter(val day: Timetable.Week.Day) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view =  LayoutInflater.from(parent?.context).inflate(R.layout.timetable_item, parent, false)
        val lesson = getLesson(position)

        view.name.text = lesson.name
        view.type.text = lesson.type
        view.teacher.text = lesson.teacher
        view.startTime.text = lesson.startTime
        view.office.text = lesson.office

        return view
    }

    override fun getItem(position: Int): Any {
        return day.lessons[position]
    }

    private fun getLesson(position: Int) = getItem(position) as Lesson

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return day.lessons.size
    }
}