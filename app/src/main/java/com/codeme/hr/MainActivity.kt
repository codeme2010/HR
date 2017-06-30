package com.codeme.hr

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MainActivity : Activity() {

    private lateinit var queue: RequestQueue
    private lateinit var array1: JSONArray
    private lateinit var array2: JSONArray
    private lateinit var tokenP: SharedPreferences
    private lateinit var token: String
    private lateinit var url: String
    private lateinit var db: SQLiteDatabase
    private val dbVersion = 1
    private val dbh = DbHelper(this, "hr.db", null, dbVersion)
    private var t = 0
//    private var count = 0//确认各月是否加载完毕

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        bt.setOnClickListener {logout()}
    }

    private fun init() {
        db = dbh.writableDatabase
        //登录口令
        tokenP = getPreferences(Context.MODE_PRIVATE)
        token = tokenP.getString("token", "")
        queue = Volley.newRequestQueue(applicationContext)
        if (token.isEmpty()) {
            login()
        } else {
            go()
        }
    }

    private fun login() {
        val inflater =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                } else {
                    LayoutInflater.from(this)
                }
        val view = inflater.inflate(R.layout.login, null)
        val account = view.account
        val psw = view.psw
        val builder = AlertDialog.Builder(this)
        builder.setTitle("只需登陆一次：")
        builder.setView(view)
        builder.setPositiveButton("确定") { _, _ ->
            url = "http://mobile.faw.com.cn:8080/mmdhr/route/getdata.rest?methodName=login" +
                    "&params=[\"${account.text}\",\"${psw.text}\",\"http://10.7.65.34\"]" +
                    "&routeName=DBAdapter&Connection=Keep-Alive&qm_device_id=61bd3fa2ebbb3a6f561baad89f421b89ac818895"
            val stringRequest1 = JsonObjectRequest(url, null,
                    Response.Listener<JSONObject> { response ->
                        if (response.optJSONObject("data").optBoolean("success")) {
                            val editor = tokenP.edit()
                            editor.putString("token", response.optJSONObject("data").optString("token"))
                            editor.apply()
                            token = tokenP.getString("token", "")
                            Toast.makeText(applicationContext, "登录成功", Toast.LENGTH_SHORT).show()
                            go()
                        } else {
                            Toast.makeText(applicationContext, "登录失败", Toast.LENGTH_SHORT).show()
                        }
                    }, Response.ErrorListener { })
            queue.add<JSONObject>(stringRequest1)
        }
        builder.show()
    }

    private fun logout() {
        val editor = tokenP.edit()
        editor.putString("token", "")
        editor.apply()
        db.execSQL("delete from hr")
        lv.adapter = null
        Toast.makeText(applicationContext, "登出成功", Toast.LENGTH_SHORT).show()
        login()
    }

    private fun go() {
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val sm = 7
        val sy = 2016
        t = (y - sy) * 12 + m - sm
        var cy: Int
        var cm: Int
        var i: Int = 0
        while (i <= t) {
            cy = sy + (sm - 1 + i) / 12
            cm = (sm - 1 + i) % 12 + 1
            sch("$cy" + (if (cm > 9) cm else ("0$cm")))
            i++
        }
    }

    private fun sch(month: String) {
        val cr: Cursor = db.query("hr", null, "_id='$month'", null, null, null, null)
        val cr_count = cr.count
        cr.close()
        if (cr_count == 0) {
            url = "http://mobile.faw.com.cn:8080/mmdhr/route/getdata.rest?methodName=getView" +
                    "&params=[\"$month\"]&routeName=DBAdapter&mmd-token=$token" +
                    "&qm_device_id=61bd3fa2ebbb3a6f561baad89f421b89ac818895"
            val stringRequest2 = JsonObjectRequest(url, null,
                    Response.Listener { response ->
                        array1 = response.optJSONObject("data").optJSONArray("result")
                        try {
                            val cv = ContentValues()
                            if (array1.length() > 0) {
                                array2 = response.optJSONObject("data").optJSONArray("DetailResult")
                                var s = array2.getJSONObject(1).optString("fieldname") + "：\n"
                                for (j in 2..array2.length() - 1) {
                                    s += array2.getJSONObject(j).optString("fieldname")
                                    s += "："
                                    s += array2.getJSONObject(j).optString("value")
                                    s += if (j + 1 == array2.length()) "" else "\n"
                                }
                                cv.put("_id", array1.getJSONObject(1).optString("gvalue"))
                                cv.put("yingji", array1.getJSONObject(2).optString("gvalue"))
                                cv.put("koukuan", array1.getJSONObject(3).optString("gvalue"))
                                cv.put("shide", array1.getJSONObject(5).optString("gvalue"))
                                cv.put("det", s)
                                db.insert("hr", null, cv)

                            } else {
                                if (month == "201612") {
                                    cv.put("_id", month)
                                    cv.put("yingji", "无数据")
                                    cv.put("koukuan", "无数据")
                                    cv.put("shide", "无数据")
                                    cv.put("det", "无数据")
                                    db.insert("hr", null, cv)
                                }
                            }
//                            count += 1
//                            if (count > t) {
//                                show()
//                            }
                            show()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }, Response.ErrorListener { })
            queue.add<JSONObject>(stringRequest2)
        } else {
//            count += 1
//            if (count > t) {
//                show()
//            }
            show()
        }
    }

    private fun show() {
        val from = arrayOf("_id", "yingji", "koukuan", "shide")
        val to = intArrayOf(R.id.month, R.id.yingji, R.id.koukuan, R.id.shide)
        val cr = db.rawQuery("select * from hr order by _id", null)
        lv.adapter = SimpleCursorAdapter(this, R.layout.item, cr, from, to, 0)
        lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val c = lv.getItemAtPosition(i) as Cursor
            val c_det = db.rawQuery("select det from hr where _id = '${c.getString(0)}'", null)
            c_det.moveToFirst()
            val det = c_det.getString(0)
            c_det.close()
            AlertDialog.Builder(this)
                    .setTitle(c.getString(0) + "工资明细")
                    .setMessage(det)
                    .setPositiveButton("确定", null)
                    .show()
        }
    }

}