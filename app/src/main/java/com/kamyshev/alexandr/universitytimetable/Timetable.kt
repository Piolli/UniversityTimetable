package com.kamyshev.alexandr.universitytimetable

import org.jsoup.nodes.Element
import java.util.*

/**
 * Created by alexandr on 15/03/2018.
 */

class Timetable {

    val weeks = arrayOf(Week(), Week())

    override fun toString(): String {
        return weeks.joinToString()
    }

    class Week {

        val days = mutableListOf<Day>()

        init {
            for(i in 0..6) {
                days.add(Day(mutableListOf()))
            }
        }

        override fun toString(): String {
            return "Week(days=$days)"
        }


        //Default is REST because in pager adapter use empty days for view data
        class Day(val lessons: MutableList<Lesson>, var typeOfDay: TypeOfDay = TypeOfDay.REST) {

            enum class TypeOfDay { STANDARD, REST }

            override fun toString(): String {
                return "Day(lessons=${lessons.joinToString()})"
            }
        }

    }

    object Utils {

        fun getLessonsForWeek(weekDoc: Element): Timetable.Week {
            val resultWeek = Timetable.Week()

            //all_days[0] - monday
            val all_days = weekDoc.getElementsByAttributeValueStarting("class", "row day")

            for(i in 0 until all_days.size) {
                val lessonsDay = all_days[i].getElementsByClass("col-lg-10 col-md-10 col-sm-10 col-xs-10 discipline")
                lessonsDay.forEach {
                    val parseLesson = parseLessonHtmlText(it)
                    resultWeek.days[i].lessons.add(parseLesson)
                    resultWeek.days[i].typeOfDay = Timetable.Week.Day.TypeOfDay.STANDARD
                }
            }

            return resultWeek
        }

        fun parseLessonHtmlText(lesson: Element): Lesson {
            val text = lesson.text().replace("(ЭЛЕКТИВНАЯ ДИСЦИПЛИНА)", "")
            val name = text.substringBefore("(").trim()
            val type = text.substringAfter("(").substringBeforeLast(")")
            val office = text.substringAfterLast(")").substringBeforeLast(",")
            val teacher = text.substringAfterLast(",")

            val startTime = lesson.previousElementSibling().text()

            return Lesson(name, type, office, teacher, startTime)
        }

        //0 or 1
        fun getNumberOfWeek(date: Date = Date()): Int {
            val calendar = GregorianCalendar.getInstance()
            calendar.time = date

            return (calendar.get(GregorianCalendar.WEEK_OF_YEAR) - 1) % 2
        }

        fun getDayOfWeek(date: Date = Date()): Int {
            val calendar = GregorianCalendar.getInstance()
            calendar.time = date

            val dayOfWeek = calendar.get(GregorianCalendar.DAY_OF_WEEK)

            return when(dayOfWeek) {
                GregorianCalendar.MONDAY -> 0
                GregorianCalendar.TUESDAY -> 1
                GregorianCalendar.WEDNESDAY -> 2
                GregorianCalendar.THURSDAY -> 3
                GregorianCalendar.FRIDAY -> 4
                GregorianCalendar.SATURDAY -> 5
                GregorianCalendar.SUNDAY -> 6
                else -> -1
            }
        }
    }
}