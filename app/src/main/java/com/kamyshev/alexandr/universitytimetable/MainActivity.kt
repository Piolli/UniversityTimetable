package com.kamyshev.alexandr.universitytimetable
import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*
import java.text.SimpleDateFormat
import android.R.menu
import android.app.AlertDialog
import android.view.*
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.FrameLayout


class MainActivity : AppCompatActivity() {

    var groupName = "БПИ16-01"

    val userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val timetable = Timetable()

        val firstReq = Jsoup.connect("https://timetable.pallada.sibsau.ru/timetable/")
                .method(Connection.Method.GET)
                .userAgent(userAgent)

        var cookies: Map<String, String>? = null
        var csfnToken = ""
        var groupLink = ""

        val firstThread = Thread {
            log("", "-------------FIRST REQUEST-------------")

            val response = firstReq.execute()

            log("statusMessage", response.statusMessage())
            log("contentType", response.contentType())
            log("statusCode", response.statusCode().toString())
            log("cookies.values", response.cookies().values.joinToString())

            cookies = response.cookies()

            //Parse csfn_token
            val htmlDoc = response.parse()
            val csfn = htmlDoc.getElementsByAttributeValue("name", "csrf_token")
            csfnToken = csfn.`val`()

            log("CSRF", csfnToken)
        }
        firstThread.start()

        val secondThread = Thread {
            synchronized(firstThread) {
                firstThread.join()

                log("", "-------------SECOND REQUEST-------------")
                val secondReq = Jsoup.connect("https://timetable.pallada.sibsau.ru/timetable/")
                        .method(Connection.Method.POST)
                        .userAgent(userAgent)
                        .data("obj_search", "2")
                        .data("search_text", groupName)
                        .data("search_btn", "")
                        .data("csrf_token", csfnToken)
                        .cookies(cookies)

                val response = secondReq.execute()
                log("statusCode", response.statusCode().toString())

                //Parse group link
                val htmlDoc = response.parse()
                groupLink = htmlDoc
                        .getElementsByClass("list-unstyled search_list")
                        .select("a")
                        .attr("href")

                log("groupLink", groupLink)
            }
        }
        secondThread.start()

        val thirdThread = Thread {
            synchronized(secondThread) {
                secondThread.join()

                log("", "-------------THIRD REQUEST-------------")
                val thirdReq = Jsoup.connect("https://timetable.pallada.sibsau.ru" + groupLink)
                        .method(Connection.Method.GET)
                        .userAgent(userAgent)
                        .cookies(cookies)

                val response = thirdReq.execute()
                log("statusCode", response.statusCode().toString())

                val htmlDoc = response.parse()

                //Get weeks in html format
                val weekDoc0 = htmlDoc.getElementById("one_week")
                val weekDoc1 = htmlDoc.getElementById("two_week")


                //Get lessons for both weeks
                timetable.weeks[0] = getLessonsForWeek(weekDoc0)
                timetable.weeks[1] = getLessonsForWeek(weekDoc1)

                log("Timetable", timetable.toString())

                runOnUiThread {
                    toolbar.title = groupName
                    setSupportActionBar(toolbar)

                    val dayOfWeek = getDayOfWeek()
                    val numberOfWeek = getNumberOfWeek()
                    log("DayOfWeek", dayOfWeek.toString())
                    log("numberOfWeek", numberOfWeek.toString())

//                    //Check if schedule days < today
//                    if(dayOfWeek >= timetable.weeks[numberOfWeek].days.size || dayOfWeek == -1) {
//                        message("Сегодня нет занятий")
//                        return@runOnUiThread
//                    }
                    //TODO(perfomance of pager view)
                    val startPosition = (numberOfWeek * 7 + dayOfWeek)

                    val dayFragments = arrayOfNulls<TimetableDayFragment>(28)
                    var index = 0
                    repeat(2) {
                        for(day in timetable.weeks[0].days)
                            dayFragments[index++] = TimetableDayFragment.createInstance(day)

                        for(day in timetable.weeks[1].days)
                            dayFragments[index++] = TimetableDayFragment.createInstance(day)
                    }


                    pager.adapter = TimetableDayPagerAdapter(dayFragments, startPosition)
                    pager.currentItem = startPosition

                    tabs.setupWithViewPager(pager)

//                    tabs.setScrollPosition(startPosition, 0f, false)
//                    list_view.adapter = TimetableListViewAdapter(timetable.weeks[getNumberOfWeek()].days[getDayOfWeek()])
                }
            }
        }
        thirdThread.start()

    }

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

        log("resultWeek SIZE", resultWeek.days.size.toString())

        return resultWeek
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.change_group_name -> {
                val input = EditText(this@MainActivity)
                val alertDialog = AlertDialog.Builder(this)
                        .setTitle("Изменение группы")
                        .setMessage("Введите название группы")
                        .setPositiveButton("Ок", {dialog, which ->
                            if(!input.text.isNullOrEmpty()) groupName = input.text.toString()
                            dialog.dismiss()
                        })
                        .setNegativeButton("Отмена", {dialog, which ->  dialog.dismiss()})
                val lp = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT)
                input.layoutParams = lp


                alertDialog.setView(input).create().show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

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

    fun log(title: String, message: String, logName: String = "RESPONSE") = Log.d(logName, title + " " + message)

    fun message(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

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

    //TODO
    inner class TimetableDayPagerAdapter(private val fragments: Array<TimetableDayFragment?>,
                                         private val startPosition: Int)
        : FragmentPagerAdapter(supportFragmentManager) {

        //TODO(change mutablelist to list for more performance)

        override fun getItem(position: Int): Fragment {
            return fragments[position]!!
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            //Left boundary of pager view
            val factor = if(position < startPosition) -1 else 1

            val sdf = SimpleDateFormat("EEEE, d MMMM", Locale("ru"))

            val calendar = GregorianCalendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, factor*(Math.abs(position - startPosition)))

            return sdf.format(calendar.time)
        }


    }
}

