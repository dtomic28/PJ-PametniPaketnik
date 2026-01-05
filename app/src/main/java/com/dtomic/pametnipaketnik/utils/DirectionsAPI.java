package com.dtomic.pametnipaketnik.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DirectionsAPI {

    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final int MAX_WAYPOINTS = 23;

    private final OkHttpClient client = new OkHttpClient();

    public List<com.google.android.gms.maps.model.LatLng> buildPath(List<String> orderedPoints) throws IOException {

        List<com.google.android.gms.maps.model.LatLng> fullPath = new ArrayList<>();

        for (int i = 0; i < orderedPoints.size() - 1; i += MAX_WAYPOINTS) {

            int end = Math.min(i + MAX_WAYPOINTS + 1, orderedPoints.size());

            String origin = encode(orderedPoints.get(i));
            String destination = encode(orderedPoints.get(end - 1));
            List<String> waypoints = (i + 1 < end - 1) ? orderedPoints.subList(i + 1, end - 1) : List.of();
            String url = "https://maps.googleapis.com/maps/api/directions/json" + "?origin=" + origin + "&destination=" + destination + "&mode=driving" + "&key=" + API_KEY;
            if (!waypoints.isEmpty()) {
                url += "&waypoints=" + encodeJoin(waypoints);
            }

            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {

                if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());

                List<com.google.android.gms.maps.model.LatLng> chunk = parsePolyline(response.body().string());

                if (!fullPath.isEmpty() && !chunk.isEmpty()) {
                    chunk.remove(0);
                }

                fullPath.addAll(chunk);
            }
        }

        return fullPath;
    }

    private List<com.google.android.gms.maps.model.LatLng> parsePolyline(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        String encoded = root.getAsJsonArray("routes").get(0).getAsJsonObject().getAsJsonObject("overview_polyline").get("points").getAsString();
        return PolyUtil.decode(encoded);
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String encodeJoin(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(encode(items.get(i)));
        }
        return sb.toString();
    }
}
