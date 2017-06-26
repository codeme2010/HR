package com.codeme.hr

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.SimpleAdapter
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

    private val list = ArrayList<HashMap<String, String>>()
    private lateinit var map: HashMap<String, String>
    private lateinit var queue: RequestQueue
    private lateinit var array1: JSONArray
    private lateinit var array2: JSONArray
    private lateinit var detl: Array<String?>
    private lateinit var tokenP: SharedPreferences
    private lateinit var token: String
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tokenP = getPreferences(Context.MODE_PRIVATE)
        token = tokenP.getString("token", "")
        queue = Volley.newRequestQueue(applicationContext)
        if (token.isEmpty()) {
            val inflater =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    } else {
                        LayoutInflater.from(this)
                    }
            //final View view = LayoutInflater.from(this).inflate(R.layout.login, null);
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
        } else {
            go()
        }
    }

    private fun go() {
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val sm = 7
        val sy = 2016
        val t = (y - sy) * 12 + m - sm
        var cy: Int
        var cm: Int
        detl = arrayOfNulls<String>(t + 1)
        var i: Int = 0
        while (i <= t) {
            cy = sy + (sm - 1 + i) / 12
            cm = (sm - 1 + i) % 12 + 1
            sch("$cy" + (if (cm > 9) cm else ("0$cm")), i)
            i++
        }
        val from = arrayOf("id", "month", "yingji", "koukuan", "shide")
        val to = intArrayOf(R.id.id, R.id.month, R.id.yingji, R.id.koukuan, R.id.shide)
        //        String[] from = {"month", "yingji", "koukuan", "shide"};
        //        int[] to = {R.id.month, R.id.yingji, R.id.koukuan, R.id.shide};
        val adapter = SimpleAdapter(this, list, R.layout.item, from, to)
        lv.adapter = adapter
        lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            map = lv.getItemAtPosition(i) as HashMap<String, String>
            AlertDialog.Builder(this@MainActivity)
                    .setTitle(map["month"] + "工资明细")
                    .setMessage(detl[Integer.parseInt(map["id"])])
                    .setPositiveButton("确定", null)
                    .show()
        }
    }

    private fun sch(month: String, k: Int) {
        url = "http://mobile.faw.com.cn:8080/mmdhr/route/getdata.rest?methodName=getView" +
                "&params=[\"" + month + "\"]&routeName=DBAdapter" +
                "&mmd-token=" + token +
                "&qm_device_id=61bd3fa2ebbb3a6f561baad89f421b89ac818895"
        val stringRequest2 = JsonObjectRequest(url, null,
                Response.Listener { response ->
                    array1 = response.optJSONObject("data").optJSONArray("result")
                    array2 = response.optJSONObject("data").optJSONArray("DetailResult")
                    map = HashMap<String, String>()
                    try {
                        if (array1.length() > 0) {
                            map.put("id", k.toString())
                            map.put("month", array1.getJSONObject(1).optString("gvalue"))
                            map.put("yingji", array1.getJSONObject(2).optString("gvalue"))
                            map.put("koukuan", array1.getJSONObject(3).optString("gvalue"))
                            map.put("shide", array1.getJSONObject(5).optString("gvalue"))
                            detl[k] = array2.getJSONObject(1).optString("fieldname") + "：\n"
                            for (j in 2..array2.length() - 1) {
                                detl[k] += array2.getJSONObject(j).optString("fieldname")
                                detl[k] += "："
                                detl[k] += array2.getJSONObject(j).optString("value")
                                detl[k] += if (j + 1 == array2.length()) "" else "\n"
                            }
                        } else {
                            map.put("id", k.toString())
                            map.put("month", month)
                            map.put("yingji", "无数据")
                            map.put("koukuan", "无数据")
                            map.put("shide", "无数据")
                        }
                        list.add(map)
                        Collections.sort<HashMap<String, String>>(list) { m1, m2 -> m1["month"]?.compareTo(m2["month"]!!)!! }
                        lv.invalidateViews()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { })
        queue.add<JSONObject>(stringRequest2)
    }

}
