package com.dtomic.pametnipaketnik.utils;

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
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;


public class DistanceMatrixAPI {
    private static String API_KEY = "AIzaSyDCqMj4ivVWaoUZVlLxgLHlCN5AWbVnHV4";
    private long [][] distances;
    private long [][] durations;
    private static final int ORIGIN_CHUNK_SIZE = 5;
    private static final int DEST_CHUNK_SIZE   = 20;

    public void getData(List<String> origins, List<String> destinations) throws FileNotFoundException {

        distances = new long[origins.size()][destinations.size()];
        durations = new long[origins.size()][destinations.size()];

       for (int i = 0; i < origins.size(); i += ORIGIN_CHUNK_SIZE) {
           List<String> originChunk = origins.subList(i, Math.min(i + ORIGIN_CHUNK_SIZE, origins.size()));
           String originChunkStr = joinAndEncode(originChunk);
           for (int j = 0; j < destinations.size(); j += DEST_CHUNK_SIZE) {
               List<String> destinationChunk = destinations.subList(j, Math.min(j + DEST_CHUNK_SIZE, destinations.size()));
                String destinationChunkStr = joinAndEncode(destinationChunk);
               String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + originChunkStr + "&destinations=" + destinationChunkStr + "&key=" + API_KEY;

               OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    JsonArray rows = jsonResponse.getAsJsonArray("rows");
                    for (int originIndex = 0; originIndex < rows.size(); originIndex++) {
                        JsonArray elements = rows.get(originIndex).getAsJsonObject().getAsJsonArray("elements");
                        for (int destIndex = 0; destIndex < elements.size(); destIndex++) {
                            JsonObject element = elements.get(destIndex).getAsJsonObject();
                            long distance = element.getAsJsonObject("distance").get("value").getAsLong();
                            long duration = element.getAsJsonObject("duration").get("value").getAsLong();
                            distances[i + originIndex][j + destIndex] = distance;
                            durations[i + originIndex][j + destIndex] = duration;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

           }
       }
       makeTextFile();
    }

    public void makeTextFile() throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new File("app/src/main/java/com/dtomic/pametnipaketnik/utils/distances.txt"));
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances[i].length; j++) {
                out.print(distances[i][j]);
                if (j < distances[i].length - 1) {
                    out.print(" ");
                }
            }
            out.println();
        }
        out.close();

        PrintWriter out2 = new PrintWriter(new File("app/src/main/java/com/dtomic/pametnipaketnik/utils/durations.txt"));
        for (int i = 0; i < durations.length; i++) {
            for (int j = 0; j < durations[i].length; j++) {
                out2.print(durations[i][j]);
                if (j < durations[i].length - 1) {
                    out2.print(" ");
                }
            }
            out2.println();
        }
        out2.close();
    }

    private String joinAndEncode(List<String> locations) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locations.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(locations.get(i).replace(" ", "+"));
        }
        return sb.toString();
    }

}
