package com.kamyshev.alexandr.universitytimetable

/**
 * Created by alexandr on 15/03/2018.
 */
/**
 * @name - name of lesson
 * @type - type : lecture, practice, lab, ..
 * @office - where doing lesson
 * @teacher - that is doing lesson
 * @startTime - time, when lesson begin
 */
class Lesson(
        val name: String = "",
        val type: String = "",
        val office: String = "",
        val teacher: String = "",
        val startTime: String = "") {

    override fun toString(): String {
        return "Lesson(name='$name', type='$type', office='$office', teacher='$teacher', startTime='$startTime')\n"
    }
}