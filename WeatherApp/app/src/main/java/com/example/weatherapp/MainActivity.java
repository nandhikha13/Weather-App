package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;


import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    TextView locationText;
    TextView dateAndTimeText;
    TextView enterZipText;
    EditText zip;
    TextView temperatureText;
    TextView descriptionText;
    TextView interval1;
    TextView interval2;
    TextView interval3;
    TextView interval4;
    TextView interval5;
    ImageView weatherPic;
    SeekBar seekBar;
    private static String zipCode = "08852";
    private static int i = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        locationText = findViewById(R.id.location);
        dateAndTimeText = findViewById(R.id.dateAndTime);
        enterZipText = findViewById(R.id.enterzip);
        temperatureText = findViewById(R.id.temperature);
        descriptionText = findViewById(R.id.description);
        seekBar = findViewById(R.id.seekBar);
        weatherPic = findViewById(R.id.weatherPic);
        weatherPic.setImageResource(R.drawable.sanreki);
        zip = findViewById(R.id.editTextText);
        interval1 = findViewById(R.id.interval1);
        interval2 = findViewById(R.id.interval2);
        interval3 = findViewById(R.id.interval3);
        interval4 = findViewById(R.id.interval4);
        interval5 = findViewById(R.id.interval5);

        AsyncThread myThread = new AsyncThread();
        myThread.execute();


        zip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 5)
                {
                    zipCode = String.valueOf(s);
                    AsyncThread myThread = new AsyncThread();
                    myThread.execute();
                }
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                i = progress;
                AsyncThread myThread = new AsyncThread();
                myThread.execute();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });
    }
    public class AsyncThread extends AsyncTask<String, Void, Void>
    {
        String temp = "";

        @Override
        protected Void doInBackground(String... strings) {
            String weatherurlstring = "http://api.openweathermap.org/data/2.5/forecast?zip=" + zipCode + "&appid=7175c1324ee99d963207d9a6efa29c39";
            URL weatherURL;
            try {
                weatherURL = new URL(weatherurlstring);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);

            }

            URLConnection urlConnection;
            InputStream inputStream;

            try {
                urlConnection = weatherURL.openConnection();
                inputStream = urlConnection.getInputStream();


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    temp = temp + line;
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            JSONObject weather = null;

            try {
                weather = new JSONObject(temp);

                String location = weather.getJSONObject("city").getString("name") + ", " + weather.getJSONObject("city").getString("country");
                String dttxt = weather.getJSONArray("list").getJSONObject(i).getString("dt_txt");
                double temperature = Double.parseDouble(weather.getJSONArray("list").getJSONObject(i).getJSONObject("main").getString("temp"));
                int iTemperature = (int) ((temperature - 273.15) * (9 / 5) + 32);
                double feelsLike = Double.parseDouble(weather.getJSONArray("list").getJSONObject(i).getJSONObject("main").getString("feels_like"));
                int iFeelsLike = (int) ((feelsLike - 273.15) * (9 / 5) + 32);
                String weatherDescription = weather.getJSONArray("list").getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description");
                char degree = '\u00B0';

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                outputFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));

                Date utcDate = inputFormat.parse(dttxt);
                String formattedDate = outputFormat.format(utcDate);

                locationText.setText(location);
                dateAndTimeText.setText(formattedDate);
                temperatureText.setText(iTemperature + " " + degree + "F");
                descriptionText.setText("Feels like " + iFeelsLike + " " + degree + "F \n\n" + weatherDescription);

                int hour = Integer.parseInt(formattedDate.substring(8, 10));
                boolean daytime = (hour >= 6 && hour <= 11 && formattedDate.contains("AM")) || (((hour >= 1 && hour <= 5) || hour == 12) && formattedDate.contains("PM"));
                boolean nighttime = (hour >= 6 && hour <= 11 && formattedDate.contains("PM")) || (((hour >= 1 && hour <= 5) || hour == 12) && formattedDate.contains("AM"));

                if(weatherDescription.equals("overcast clouds") || weatherDescription.equals("broken clouds"))
                    weatherPic.setImageResource(R.drawable.brokenovercast);
                else if(weatherDescription.contains("clear") && daytime)
                    weatherPic.setImageResource(R.drawable.dayclear);
                else if(weatherDescription.contains("clear") && nighttime)
                    weatherPic.setImageResource(R.drawable.nightclearr);
                else if(weatherDescription.contains("thunderstorm"))
                    weatherPic.setImageResource(R.drawable.thunderstorm);
                else if((weatherDescription.contains("drizzle")) || weatherDescription.contains("shower rain"))
                    weatherPic.setImageResource(R.drawable.showerrain);
                else if((weatherDescription.equals("light rain") || weatherDescription.equals("moderate rain") || weatherDescription.equals("heavy intensity rain") || weatherDescription.equals("very heavy rain") || weatherDescription.equals("extreme rain")) && daytime)
                    weatherPic.setImageResource(R.drawable.lmhrain);
                else if((weatherDescription.equals("light rain") || weatherDescription.equals("moderate rain") || weatherDescription.equals("heavy intensity rain") || weatherDescription.equals("very heavy rain") || weatherDescription.equals("extreme rain")) && nighttime)
                    weatherPic.setImageResource(R.drawable.lmhrainnight);
                else if(weatherDescription.contains("snow") || weatherDescription.contains("sleet") || weatherDescription.equals("freezing rain"))
                    weatherPic.setImageResource(R.drawable.snow);
                else if(weatherDescription.equals("mist") || weatherDescription.equals("smoke") || weatherDescription.equals("haze") || weatherDescription.equals("sand/dust whirls") || weatherDescription.equals("fog") || weatherDescription.equals("sand") || weatherDescription.equals("dust") || weatherDescription.equals("volcanic ash") || weatherDescription.equals("squalls") || weatherDescription.equals("tornado"))
                    weatherPic.setImageResource(R.drawable.atmosphere);
                else if(weatherDescription.equals("few clouds") && daytime)
                    weatherPic.setImageResource(R.drawable.dayfewclouds);
                else if(weatherDescription.equals("few clouds") && nighttime)
                    weatherPic.setImageResource(R.drawable.nightfewclouds);
                else if(weatherDescription.equals("scattered clouds"))
                    weatherPic.setImageResource(R.drawable.scattered);

                for(int x = 0; x < 5; x++)
                {
                    String dttxtInterval = weather.getJSONArray("list").getJSONObject(x).getString("dt_txt");

                    SimpleDateFormat inputFormatInterval = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    inputFormatInterval.setTimeZone(TimeZone.getTimeZone("UTC"));

                    SimpleDateFormat outputFormatInterval = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                    outputFormatInterval.setTimeZone(TimeZone.getTimeZone("America/New_York"));

                    Date utcDateInterval = inputFormatInterval.parse(dttxtInterval);
                    String formattedDateInterval = outputFormatInterval.format(utcDateInterval);

                    int hourInterval = Integer.valueOf(formattedDateInterval.substring(8, 10));

                    if(x == 0)
                        interval1.setText(hourInterval + formattedDateInterval.substring(14));
                    if(x == 1)
                        interval2.setText(hourInterval + formattedDateInterval.substring(14));
                    if(x == 2)
                        interval3.setText(hourInterval + formattedDateInterval.substring(14));
                    if(x == 3)
                        interval4.setText(hourInterval + formattedDateInterval.substring(14));
                    if(x == 4)
                        interval5.setText(hourInterval + formattedDateInterval.substring(14));
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}