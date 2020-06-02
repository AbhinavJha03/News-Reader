package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> newsReader = new ArrayList<String>();
    ListView listView;

       //For converting URL into char

    public class DownloadTask extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        }

                // retrieving Url and title from api JSON
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                SQLiteDatabase myDatabase = MainActivity.this.openOrCreateDatabase("NewsReader", MODE_PRIVATE, null);
                myDatabase.execSQL("DROP TABLE IF EXISTS newsReader");
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS newsReader (url VARCHAR , id INTEGER PRIMARY KEY)");


                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonarray = jsonObject.getJSONArray("articles");

                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);

                   String url=jsonobject.getString("url");
               //    String title= jsonObject.getString("title");
                     myDatabase.execSQL("INSERT INTO newsReader (url) VALUES ('"+url+"') ");

                     Log.i("abcd",url);
                }


            }
                catch (Exception e) {

                e.printStackTrace();
            }

        }
    }




        // for item click and new activity
    public void itemClick()
    {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, newsReader);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                Intent intent = new Intent(getApplicationContext(), WebsiteActivity.class);
                intent.putExtra("url", newsReader.get(i));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        DownloadTask task = new DownloadTask();
        task.execute("https://newsapi.org/v2/top-headlines?sources=techcrunch&apiKey=60e14a90ac494976a8936b5f0cbe326f");

        SQLiteDatabase myDatabase = this.openOrCreateDatabase("NewsReader", MODE_PRIVATE, null);

        Cursor c = myDatabase.rawQuery("SELECT * FROM newsReader", null);
        int url = c.getColumnIndex("url");
        int id_index = c.getColumnIndex("id");
        c.moveToFirst();
        while (!c.isAfterLast()) {

            Log.i("url", c.getString(url));
            Log.i("id", Integer.toString(c.getInt(id_index)));
            newsReader.add(c.getString(url));
            c.moveToNext();
        }
           itemClick();
    }
}



