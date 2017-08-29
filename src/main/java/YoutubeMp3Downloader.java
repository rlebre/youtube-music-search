
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Rui Lebre (ruilebre@ua.pt) on 8/23/17.
 */


public class YoutubeMp3Downloader {
    public static String[] getLink(String url) throws ClientProtocolException, IOException {
        boolean passCode = false;
        String h = "";
        String title = "";
        String result = "";
        String[] returnVal = {"", ""};
        Map<String, String> jsonTable;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpInitialGet = new HttpGet("http://www.youtube-mp3.org/a/pushItem/?item=http%3A//www.youtube.com/watch%3Fv%3D" + url + "&el=ma&bf=false&r=1503508581492&s=53827");
        httpInitialGet.addHeader("Accept-Location", "*");
        httpInitialGet.addHeader("Referrer", "http://www.youtube-mp3.org");
        HttpParams params = new SyncBasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1");
        httpInitialGet.setParams(params);
        HttpResponse firstResponse = httpClient.execute(httpInitialGet);
        //http://www.youtube-mp3.org/a/pushItem/?item=http%3A//www.youtube.com/watch%3Fv%3DKMU0tzLwhbE&el=ma&bf=false&r=1503506369972&s=51225
        try {
            if (firstResponse.getStatusLine().toString().contains("200")) {
                passCode = true;
            }
        } finally {
            httpInitialGet.releaseConnection();
        }

        if (passCode) {
            while (true) {
                HttpGet httpStatusGet = new HttpGet("http://www.youtube-mp3.org/a/itemInfo/?video_id=" + url + "&ac=www&t=grp&r=1503508582665&s=103613");
                httpStatusGet.addHeader("Accept-Location", "*");
                httpStatusGet.addHeader("Referrer", "http://www.youtube-mp3.org");
                httpStatusGet.setParams(params);
                HttpResponse secondResponse = httpClient.execute(httpStatusGet);
                HttpEntity secondEntity = secondResponse.getEntity();
                InputStream is = secondEntity.getContent();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                httpStatusGet.releaseConnection();
                result = result.replaceAll("\\}.*", "}");
                result = result.replaceAll(".*?\\{", "{");
                try {
                    JSONObject jsonData = new JSONObject(result);
                    JSONArray jsonArray = jsonData.names();
                    JSONArray valArray = jsonData.toJSONArray(jsonArray);
                    jsonTable = new HashMap<String, String>(jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonTable.put(jsonArray.get(i).toString(), valArray.get(i).toString());
                    }
                    if (jsonTable.get("status").equals("serving")) {
                        h = jsonTable.get("h");
                        title = jsonTable.get("title");
                        break;
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            returnVal[0] = "http://www.youtube-mp3.org/get?video_id=" + url + "&h=" + h;
            returnVal[1] = title;
            return returnVal;
        } else {
            //TODO: Error, vid not downloadable
        }
        return null;
    }
}
