package com.codeme.hr

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

internal class DbHelper(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL("CREATE TABLE [hr](\n" +
                    "    [_id] CHAR(6) PRIMARY KEY NOT NULL UNIQUE, \n" +
                    "    [yingji] FLOAT, \n" +
                    "    [koukuan] FLOAT, \n" +
                    "    [shide] FLOAT, \n" +
                    "    [det] CHAR);")
        } catch (e: SQLiteException) {
            Log.e("creatTable", e.toString(), null)
        }

    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {

    }
}
