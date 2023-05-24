package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        new JsonTask().execute("https://awu4j6hku3.execute-api.eu-central-1.amazonaws.com/dev/weather/hist?days=1&group=12");

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(result);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            LineGraphSeries<DataPoint> seriesT = new LineGraphSeries<>(new DataPoint[] {});
            LineGraphSeries<DataPoint> seriesH = new LineGraphSeries<>(new DataPoint[] {});
            LineGraphSeries<DataPoint> seriesW = new LineGraphSeries<>(new DataPoint[] {});
            LineGraphSeries<DataPoint> seriesP = new LineGraphSeries<>(new DataPoint[] {});

            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = null;
                try {
                    jsonobject = jsonarray.getJSONObject(i);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                try {
                    String time = jsonobject.getString("time");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String value = null;
                try {
                    value = jsonobject.getString("value");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String dataType = null;
                try {
                    dataType = jsonobject.getString("dataType");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                if (dataType.equals("temp"))
                    seriesT.appendData(new DataPoint(i/5.0,Double.parseDouble(value)),true,12);
                if (dataType.equals("humidity"))
                    seriesH.appendData(new DataPoint(i/5.0,Double.parseDouble(value)),true,12);
                if (dataType.equals("wind"))
                    seriesW.appendData(new DataPoint(i/5.0,Double.parseDouble(value)),true,12);
                if (dataType.equals("pressure"))
                    seriesP.appendData(new DataPoint(i/5.0,Double.parseDouble(value)),true,12);
            }

            GraphView graphT = (GraphView) findViewById(R.id.graphT);
            graphT.setTitle("Temperature");
            graphT.setTitleTextSize(80);
            graphT.setTitleColor(Color.RED);
            seriesT.setColor(Color.RED);
            graphT.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
            graphT.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
            graphT.getGridLabelRenderer().setGridColor(Color.WHITE);
            graphT.addSeries(seriesT);

            GraphView graphH = (GraphView) findViewById(R.id.graphH);
            graphH.setTitle("Humidity");
            graphH.setTitleTextSize(80);
            graphH.setTitleColor(Color.BLUE);
            seriesH.setColor(Color.BLUE);
            graphH.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
            graphH.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
            graphH.getGridLabelRenderer().setGridColor(Color.WHITE);
            graphH.addSeries(seriesH);

            GraphView graphW = (GraphView) findViewById(R.id.graphW);
            graphW.setTitle("Wind Speed");
            graphW.setTitleTextSize(80);
            graphW.setTitleColor(Color.GREEN);
            seriesW.setColor(Color.GREEN);
            graphW.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
            graphW.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
            graphW.getGridLabelRenderer().setGridColor(Color.WHITE);
            graphW.addSeries(seriesW);

            GraphView graphP = (GraphView) findViewById(R.id.graphP);
            graphP.setTitle("Pressure");
            graphP.setTitleTextSize(80);
            graphP.setTitleColor(Color.YELLOW);
            seriesP.setColor(Color.YELLOW);
            graphP.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
            graphP.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
            graphP.getGridLabelRenderer().setGridColor(Color.WHITE);
            graphP.addSeries(seriesP);
        }
    }
    public void clickExit(View view) {
        this.finishAffinity();
    }
}