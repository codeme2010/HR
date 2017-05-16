package com.codeme.hr;

import android.app.Activity;
import android.os.Bundle;
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
        for (int i = 0; i <= t; i++) {
            cy = sy + (sm - 1 + i) / 12;
            cm = (sm - 1 + i) % 12 + 1;
            sch(cy + "" + (cm > 9 ? cm : ("0" + cm)));
        }
        String[] from = {"month", "yingji", "koukuan", "shide"};
        int[] to = {R.id.month, R.id.yingji, R.id.koukuan, R.id.shide};
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item, from, to);
        lv.setAdapter(adapter);
    }

    private void sch(String month) {
        String url = "http://mobile.faw.com.cn:8080/mmdhr/route/getdata.rest?methodName=getView" +
                "&params=[\"" + month + "\"]&routeName=DBAdapter" +
                "&mmd-token=FBD5026532FF43868EE9C02F63845C2D" +
                "&qm_device_id=61bd3fa2ebbb3a6f561baad89f421b89ac818895";
        JsonObjectRequest stringRequest2 = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray array1;
                        array1 = response.optJSONObject("data").optJSONArray("result");
//                        array2 = response.optJSONObject("data").optJSONArray("DetailResult");
                        try {
                            map = new HashMap<>();
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
                            lv.invalidateViews();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        /*s1 = s1 + "\n";
                        s2 = s2 + "\n";
                        for (int i = 1; i < array2.length(); i++) {
                            try {
                                s1 = s1 + array2.getJSONObject(i).optString("fieldname") + "\n";
                                s2 = s2 + array2.getJSONObject(i).optString("value") + "\n";
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        tv1.setText(s1);
                        tv2.setText(s2);*/
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest2);
    }

}
