package com.example.weatherapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    String cityName;
    TextView edtTemp , edtFeelTemp , edtmaxTemp , edtminTemp , Humidity , Description;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_bar,menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Enter City......");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this,query,Toast.LENGTH_LONG).show();
                cityName = query;
                try {
                    String encodedCityName = URLEncoder.encode(cityName,"UTF-8");

                    DownLoadTask task = new DownLoadTask();
                    task.execute("https://api.openweathermap.org/data/2.5/forecast?q="+ encodedCityName +"&appid=c095aa4415e1d53c991fd60814b5cb80");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtTemp = findViewById(R.id.edtTemp);
        edtFeelTemp = findViewById(R.id.edtFeelTemp);
        edtmaxTemp = findViewById(R.id.maxTemp);
        edtminTemp = findViewById(R.id.minTemp);
        Humidity = findViewById(R.id.Humidity);
        Description = findViewById(R.id.description);

    }

    public class DownLoadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try {
                url = new URL(urls[0]);

                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream in = httpURLConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                String s = e.getMessage();
                Log.i("WError", s);
                return null;
            }


        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //Log.i("Website Content","" + result);
            try {

                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);

                    DecimalFormat df2 = new DecimalFormat("#.#");

                    JSONArray arr = jsonObject.getJSONArray("list");

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    String localDateTime = dtf.format(now);

                    //Log.i("Website Content","" + arr);

                    for (int i = 0; i < arr.length(); i++) {

                        JSONObject jsonPart = arr.getJSONObject(i);

                        String date_time = jsonPart.getString("dt_txt");
                        Log.i("Date and Time ", date_time);
                        String[] apiDate = date_time.split("\\s");
                        String apiTime = apiDate[1];
                        String[] apiArr = apiTime.split(":");

                        Double temp, minTemp, maxTemp, feelTemp;
                        int humidity;
                        String description = "";

                        String[] date = localDateTime.split("\\s");
                        String time = date[1];
                        String[] tArr = time.split(":");

                        if (apiDate[0].equals(date[0])) {

                            if ((Integer.parseInt(tArr[0]) >= Integer.parseInt(apiArr[0]) && Integer.parseInt(tArr[0]) < (Integer.parseInt(apiArr[0]) + 3))) {

                                JSONObject main = jsonPart.getJSONObject("main");
                                //Log.i("Website Content", "" + main);

                                temp = main.getDouble("temp") - 273;
                                feelTemp = main.getDouble("feels_like") - 273;
                                maxTemp = main.getDouble("temp_max") - 273;
                                minTemp = main.getDouble("temp_min") - 273;
                                humidity = main.getInt("humidity");

                                JSONArray weather = jsonPart.getJSONArray("weather");
                                // JSONArray arr1 = new JSONArray(weather);
                                for (int j = 0; j < weather.length(); j++) {
                                    JSONObject jsonPart1 = weather.getJSONObject(j);
                                    description = jsonPart1.getString("description");

                                }

                                edtTemp.setText(df2.format(temp));
                                edtFeelTemp.setText("Feel Like: " + df2.format(feelTemp));
                                edtmaxTemp.setText("Max Temp:" + df2.format(maxTemp));
                                edtminTemp.setText("Min Temp: " + df2.format(minTemp));
                                Humidity.setText("Humidity: " + humidity + "%");
                                Description.setText(description);

                            }
                        } else {
                            Log.i("Website Content", "Error>>>>>>>>>>>>>>>>>>!!!!!!!!!!!!!!!!!!");
                        }

                    }
                }else {
                    Toast.makeText(MainActivity.this,"There seems to be an error with the city you have entered", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
