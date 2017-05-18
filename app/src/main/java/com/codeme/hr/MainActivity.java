package com.codeme.hr;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends Activity {

    private ListView lv;
    private final ArrayList<HashMap<String, String>> list = new ArrayList<>();
    private HashMap<String, String> map = null;
    private RequestQueue queue;
    private JSONArray array1, array2;
    private String[] detl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.lv);
        queue = Volley.newRequestQueue(getApplicationContext());
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int sm = 7;
        int sy = 2016;
        int t = (y - sy) * 12 + m - sm;
        int cy, cm;
        detl = new String[t + 1];
        int i;
        for (i = 0; i <= t; i++) {
            cy = sy + (sm - 1 + i) / 12;
            cm = (sm - 1 + i) % 12 + 1;
            sch(cy + "" + (cm > 9 ? cm : ("0" + cm)), i);
        }
        String[] from = {"month", "yingji", "koukuan", "shide"};
        int[] to = {R.id.month, R.id.yingji, R.id.koukuan, R.id.shide};
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item, from, to);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                map = (HashMap<String, String>) lv.getItemAtPosition(i);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(map.get("month") + "工资明细")
                        .setMessage(detl[i])
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }

    private void sch(final String month, final int k) {
        String url = "http://mobile.faw.com.cn:8080/mmdhr/route/getdata.rest?methodName=getView" +
                "&params=[\"" + month + "\"]&routeName=DBAdapter" +
                "&mmd-token=FBD5026532FF43868EE9C02F63845C2D" +
                "&qm_device_id=61bd3fa2ebbb3a6f561baad89f421b89ac818895";
        JsonObjectRequest stringRequest2 = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        array1 = response.optJSONObject("data").optJSONArray("result");
                        array2 = response.optJSONObject("data").optJSONArray("DetailResult");
                        map = new HashMap<>();
                        try {
                            if (array1.length() > 0) {
                                map.put("month", array1.getJSONObject(1).optString("gvalue"));
                                map.put("yingji", array1.getJSONObject(2).optString("gvalue"));
                                map.put("koukuan", array1.getJSONObject(3).optString("gvalue"));
                                map.put("shide", array1.getJSONObject(5).optString("gvalue"));
                                list.add(map);
                                Collections.sort(list, new Comparator<HashMap<String, String>>() {
                                    @Override
                                    public int compare(HashMap<String, String> m1, HashMap<String, String> m2) {
                                        return m1.get("month").compareTo(m2.get("month"));
                                    }
                                });
                                detl[k] = array2.getJSONObject(1).optString("fieldname") + "：\n";
                                for (int j = 2; j < array2.length(); j++) {
                                    detl[k] += array2.getJSONObject(j).optString("fieldname");
                                    detl[k] += "：";
                                    detl[k] += array2.getJSONObject(j).optString("value");
                                    detl[k] += (j + 1 == array2.length() ? "" : "\n");
                                }
                            } else {
                                map.put("month", month);
                                map.put("yingji", "无数据");
                                map.put("koukuan", "无数据");
                                map.put("shide", "无数据");
                                list.add(map);
                                Collections.sort(list, new Comparator<HashMap<String, String>>() {
                                    @Override
                                    public int compare(HashMap<String, String> m1, HashMap<String, String> m2) {
                                        return m1.get("month").compareTo(m2.get("month"));
                                    }
                                });
                            }
                            lv.invalidateViews();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest2);
    }

}
