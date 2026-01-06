package com.dtomic.pametnipaketnik.utils;

import android.content.Context;
import android.os.Build;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeocodingAPI {

    private static String API_KEY = "";
    private double[] latitudes;
    private double[] longitudes;

    public void getCoordinates(List<String> addresses, Context context)
            throws FileNotFoundException {

        latitudes  = new double[addresses.size()];
        longitudes = new double[addresses.size()];

        OkHttpClient client = new OkHttpClient();

        for (int i = 0; i < addresses.size(); i++) {

            String url = "https://maps.googleapis.com/maps/api/geocode/json" + "?address=" + addresses.get(i) + "&key=" + API_KEY;

            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
                System.out.println(json.toString());

                JsonArray results = json.getAsJsonArray("results");

                if (results.size() == 0) {
                    latitudes[i]  = 0;
                    longitudes[i] = 0;
                    continue;
                }

                JsonObject location = results.get(0).getAsJsonObject().getAsJsonObject("geometry").getAsJsonObject("location");

                latitudes[i]  = location.get("lat").getAsDouble();
                longitudes[i] = location.get("lng").getAsDouble();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        makeTextFile(context);
    }

    private void makeTextFile(Context context) throws FileNotFoundException {
        File dir = new File(context.getFilesDir(), "DataCache");
        if (!dir.exists()) dir.mkdirs();
        PrintWriter latOut = new PrintWriter(new File(dir, "latitudes.txt"));
        PrintWriter lngOut = new PrintWriter(new File(dir, "longitudes.txt"));

        for (int i = 0; i < latitudes.length; i++) {
            latOut.println(latitudes[i]);
            lngOut.println(longitudes[i]);
        }

        latOut.close();
        lngOut.close();
    }
}
