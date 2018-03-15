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


        class Day(val lessons: MutableList<Lesson>) {
            override fun toString(): String {
                return "Day(lessons=${lessons.joinToString()})"
            }
        }

    }
}