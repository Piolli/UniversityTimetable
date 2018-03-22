package com.kamyshev.alexandr.universitytimetable

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
}