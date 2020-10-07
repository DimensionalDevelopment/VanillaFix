package org.dimdev.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class HasteUpload {
    public static String uploadToHaste(String baseUrl, String extension, String str) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        URL uploadURL = new URL(baseUrl + "/documents");
        HttpURLConnection connection = (HttpURLConnection) uploadURL.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        connection.setFixedLengthStreamingMode(bytes.length);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();

        try {
            try (OutputStream os = connection.getOutputStream()) {
                os.write(bytes);
            }

            try (InputStream is = connection.getInputStream()) {
                JsonObject json = new Gson().fromJson(new InputStreamReader(is), JsonObject.class);
                return baseUrl + "/" + json.get("key").getAsString() + (extension == null || extension.isEmpty() ? "" : "." + extension);
            }
        } finally {
            connection.disconnect();
        }
    }
}
