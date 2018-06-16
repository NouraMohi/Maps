package com.zoma.map;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by noura_000 on 8/19/2017.
 */

public class DownloadURL {

    public String readURL(String myUrl) throws IOException {
        String data ="";
        InputStream iStream = null;
        HttpURLConnection urlConn = null;
        try {
            URL url = new URL(myUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();

            iStream = urlConn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while((line =  br.readLine()) != null)
            {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (IOException e) {
            Log.d("Exception", e.toString());
            e.printStackTrace();
        }
        finally
        {
            try {
                iStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConn.disconnect();
        }
        return data;
    }
}
