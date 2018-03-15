package com.kamyshev.alexandr.universitytimetable
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.Request.Method.POST
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.CookieHandler
import java.net.CookieManager
import kotlin.concurrent.thread


class MainActivity : Activity() {

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
                        .data("search_text", "БПИ16-01")
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
                val weeksDoc = htmlDoc.getElementsByAttributeValueStarting("class", "tab-pane")

                //Get lessons for both weeks
                timetable.weeks[0] = getLessonsForWeek(weeksDoc[0])
                timetable.weeks[1] = getLessonsForWeek(weeksDoc[1])

                log("Timetable", timetable.toString())

                runOnUiThread {
                    list_view.adapter = TimetableListViewAdapter(timetable.weeks[0].days[0])
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
            }
        }

        log("ALL_DAYS SIZE", all_days.size.toString())

        return resultWeek
    }

    fun parseLessonHtmlText(lesson: Element): Lesson {
        val text = lesson.text()
        val name = text.substringBefore("(").trim()
        val type = text.substringAfter("(").substringAfterLast(")")
        val office = text.substringAfterLast(")").substringBeforeLast(",")
        val teacher = text.substringAfterLast(",")

        val startTime = lesson.previousElementSibling().text()

        return Lesson(name, type, office, teacher, startTime)
    }

    fun log(title: String, message: String, logName: String = "RESPONSE") = Log.d(logName, title + " " + message)

    fun message(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}
