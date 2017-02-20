package com.example.samsung.db_query;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String LOG_TAG = "myLogs";
    final String NAME_TABLE = "my_table";

    String[] name = {"Китай", "США", "Бразилия", "Россия", "Япония",
                    "Германия", "Египет", "Италия", "Франция", "Канада"},
             region = {"Азия", "Америка", "Америка", "Европа", "Азия",
                    "Европа", "Африка", "Европа", "Европа", "Америка"};
    int[] population = {1400, 311, 195, 142, 128, 82, 80, 60, 66, 35};
    Button btnAll, btnFunc, btnPopulation, btnSort, btnGroup, btnHaving;
    EditText etFunc, etPopulation, etRegionsPopulation;
    RadioGroup rgSoort;

    DBHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAll = (Button) findViewById(R.id.btnAll);
        btnAll.setOnClickListener(this);

        btnFunc = (Button) findViewById(R.id.btnFunc);
        btnFunc.setOnClickListener(this);

        btnPopulation = (Button) findViewById(R.id.btnPopulation);
        btnPopulation.setOnClickListener(this);

        btnSort = (Button) findViewById(R.id.btnSort);
        btnSort.setOnClickListener(this);

        btnGroup = (Button) findViewById(R.id.btnGroup);
        btnGroup.setOnClickListener(this);

        btnHaving = (Button) findViewById(R.id.btnHaving);
        btnHaving.setOnClickListener(this);

        etFunc = (EditText) findViewById(R.id.etFunc);
        etPopulation = (EditText) findViewById(R.id.etPopulation);
        etRegionsPopulation = (EditText) findViewById(R.id.etRegionsPopulation);

        rgSoort = (RadioGroup) findViewById(R.id.rgSort);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        //проверка существования записей
        Cursor cursor = db.query(NAME_TABLE, null, null, null, null, null, null);

        if (cursor.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            //заполняем таблицу
            for (int i = 0; i < 10; i++) {
                contentValues.put("name", name[i]);
                contentValues.put("population", population[i]);
                contentValues.put("region", region[i]);
                Log.d(LOG_TAG, "id = " + db.insert(NAME_TABLE, null, contentValues));
            }
        }

        cursor.close();
        dbHelper.close();
        //эмулируем нажатие кнопки btnAll
        onClick(btnAll);
    }

    @Override
    public void onClick(View v) {
        //подключение к базе данных
        db = dbHelper.getWritableDatabase();
        //читаем данные с экрана
        String sFunc = etFunc.getText().toString(),
                sPopulation = etPopulation.getText().toString(),
                sRegionsPopulation = etRegionsPopulation.getText().toString();
        //переменные для query
        String[] columns = null,
                 selectionArgs = null;
        String selection = null,
                groupBy = null,
                having = null,
                orderBy = null;
        //курсор
        Cursor cursor = null;
        //определяем нажатие кнопки
        switch (v.getId()) {
            //все записи
            case R.id.btnAll :
                Log.d(LOG_TAG, "--- Все записи ---");
                cursor = db.query(NAME_TABLE, null, null, null, null, null, null);
                break;
            //функция
            case R.id.btnFunc :
                Log.d(LOG_TAG, "--- Функция " + sFunc + " ---");
                columns = new String[] {sFunc};
                cursor = db.query(NAME_TABLE, columns, null, null, null, null, null);
                break;
            //население больше, чем
            case R.id.btnPopulation :
                Log.d(LOG_TAG, "--- Население больше " + sPopulation + " ---");
                selection = "population > ?";
                selectionArgs = new String[] {sPopulation};
                cursor = db.query(NAME_TABLE, null, selection, selectionArgs, null, null, null);
                break;
            //население по региону
            case R.id.btnGroup :
                Log.d(LOG_TAG, "--- Население по региону ---");
                columns = new String[] {"region", "sum(population) as population"};
                groupBy = "region";
                cursor = db.query(NAME_TABLE, columns, null, null, groupBy, null, null);
                break;
            //население по региону больше, чем
            case R.id.btnHaving :
                Log.d(LOG_TAG, "--- Регионы с населением больше " + sRegionsPopulation + " ---");
                columns = new String[] {"region", "sum(population) as population"};
                groupBy = "region";
                having = "sum(population) > " + sRegionsPopulation;
                cursor = db.query(NAME_TABLE, columns, null, null, groupBy, having, null);
                break;
            //сортировка
            case R.id.btnSort :
                //сортировка по
                switch (rgSoort.getCheckedRadioButtonId()) {
                    //наименованию
                    case R.id.rbName :
                        Log.d(LOG_TAG, "--- Сортирока по наименованию ---");
                        orderBy = "name";
                        break;
                    //населению
                    case R.id.rbPopulation :
                        Log.d(LOG_TAG, "--- Сортирока по населению ---");
                        orderBy = "population";
                        break;
                    //региону
                    case R.id.rbRegion :
                        Log.d(LOG_TAG, "--- Сортирока по региону ---");
                        orderBy = "region";
                        break;
                }
                cursor = db.query(NAME_TABLE, null, null, null, null, null, orderBy);
                break;
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String string;
                do {
                    string = "";
                    for (String cn : cursor.getColumnNames()) {
                        string = string.concat(cn + " = "
                        + cursor.getString(cursor.getColumnIndex(cn)) + "; ");
                    }
                    Log.d(LOG_TAG, string);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.d(LOG_TAG, "!!! Cursor is null !!!");
        }
        dbHelper.close();

    }
    public class DBHelper extends SQLiteOpenHelper {

        final static String DB_NAME = "myDB";
        final static int DB_VERSION = 1;

        public DBHelper(Context context) {
            //конструктор суперкласса
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            //создаём таблицу с полями
            db.execSQL("create table " + NAME_TABLE + "("
                        + "id integer primary key autoincrement," + "name text,"
                        + "population integer," + "region text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
