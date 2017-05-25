package com.codeme.hr

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.AdapterView
import android.widget.SimpleAdapter
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MainActivity : Activity() {

    private var queue: RequestQueue? = null
    private val list = ArrayList<HashMap<String, String>>()
    private var map: HashMap<String, String>? = null
    private var detl: Array<String?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        queue = Volley.newRequestQueue(applicationContext)
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val sm = 7
        val sy = 2016
        val t = (y - sy) * 12 + m - sm
        var cy: Int
        var cm: Int
        detl = arrayOfNulls<String>(t + 1)
        for (i in 0..t) {
            cy = sy + (sm - 1 + i) / 12
            cm = (sm - 1 + i) % 12 + 1
            sch(cy.toString() + "" + if (cm > 9) cm else "0" + cm, i)
        }
        val from = arrayOf("month", "yingji", "koukuan", "shide")
        val to = intArrayOf(R.id.month, R.id.yingji, R.id.koukuan, R.id.shide)
        val adapter = SimpleAdapter(this, list, R.layout.item, from, to)
        lv.adapter = adapter
        lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            @Suppress("UNCHECKED_CAST")
            map = lv.getItemAtPosition(i) as HashMap<String, String>?
            AlertDialog.Builder(this)
                    .setTitle(map?.get("month") + "工资明细")
                    .setMessage(detl!![i]?:"")
                    .setPositiveButton("确定", null)
                    .show()
        }
    }

    fun sch(month: String, k: Int) {
        val url = "http://mobile.faw.com.cn:8080/mmdhr/route/getdata.rest?methodName=getView" +
                "&params=[\"" + month + "\"]&routeName=DBAdapter" +
                "&mmd-token=FBD5026532FF43868EE9C02F63845C2D" +
                "&qm_device_id=61bd3fa2ebbb3a6f561baad89f421b89ac818895"
        val stringRequest2: JsonObjectRequest = JsonObjectRequest(url, null,
                Response.Listener<JSONObject> { response ->
                    val array1 = response.optJSONObject("data").optJSONArray("result")
                    val array2 = response.optJSONObject("data").optJSONArray("DetailResult")
                    map = HashMap<String, String>()
                    try {
                        if (array1 != null && array1.length() > 0) {
                            map?.put("month", array1.getJSONObject(1).optString("gvalue"))
                            map?.put("yingji", array1.getJSONObject(2).optString("gvalue"))
                            map?.put("koukuan", array1.getJSONObject(3).optString("gvalue"))
                            map?.put("shide", array1.getJSONObject(5).optString("gvalue"))
                            list.add(map!!)
                            Collections.sort(list) { m1, m2 -> m1["month"]!!.compareTo(m2["month"]!!) }
                            detl!![k] = array2.getJSONObject(1).optString("fieldname") + "：\n"
                            for (j in 2..array2.length() - 1) {
                                detl!![k] += array2.getJSONObject(j).optString("fieldname")
                                detl!![k] += "："
                                detl!![k] += array2.getJSONObject(j).optString("value")
                                detl!![k] += if (j + 1 == array2.length()) "" else "\n"
                            }
                        } else {
                            map?.put("month", month)
                            map?.put("yingji", "无数据")
                            map?.put("koukuan", "无数据")
                            map?.put("shide", "无数据")
                            list.add(map!!)
                            Collections.sort(list) { m1, m2 -> m1["month"]!!.compareTo(m2["month"]!!) }
                        }
                        lv.invalidateViews()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { _ ->
        })
        queue?.add(stringRequest2)

    }

}
