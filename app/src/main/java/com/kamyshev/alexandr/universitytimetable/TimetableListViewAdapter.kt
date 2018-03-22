package com.kamyshev.alexandr.universitytimetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.lesson_item.view.*

/**
 * Created by alexandr on 15/03/2018.
 */
class TimetableListViewAdapter(val day: Timetable.Week.Day) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: DayViewHolder
        val returnView: View

        if(convertView == null) {
            returnView = LayoutInflater.from(parent?.context).inflate(R.layout.lessn_item_linear, parent, false)
            viewHolder = DayViewHolder()

            viewHolder.lessonName = returnView.lesson_name
            viewHolder.lessonTeacher = returnView.lesson_teacher
            viewHolder.lessonStartTime = returnView.lesson_start_time
            viewHolder.lessonEndTime = returnView.lesson_end_time
            viewHolder.lessonOffice = returnView.lesson_office

            returnView.tag = viewHolder
        }
        else {
            viewHolder = convertView.tag as DayViewHolder
            returnView = convertView
        }

        val lesson = getLesson(position)
        viewHolder.lessonName.text = "${lesson.name}, ${lesson.type}"
        viewHolder.lessonTeacher.text = lesson.teacher
        viewHolder.lessonStartTime.text = lesson.startTime
        viewHolder.lessonEndTime.text = lesson.endTime
        viewHolder.lessonOffice.text = lesson.office

        return returnView
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

    class DayViewHolder {
        lateinit var lessonName: TextView
        lateinit var lessonTeacher: TextView
        lateinit var lessonStartTime: TextView
        lateinit var lessonEndTime: TextView
        lateinit var lessonOffice: TextView
    }


}
