/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Ahmed
 */
public class Crawler {

    public static void main(String args[]) throws JSONException, IOException {
        Document search_result;
        String requested[] = new String[]{"aspirin", "Fentanyl"};
        ArrayList<Newsfeed_item> threads = new ArrayList();
        String query = "https://www.dailystrength.org/search/ajax?query=";
        int count=0;
        try {
            for (int i = 0; i < requested.length; i++) {
                for (int j = 1;; j++) {
                    String json = jsonGetRequest(query + requested[i] + "&type=discussion&page=" + (j));
                    JSONObject obj = new JSONObject(json);
                    String pageName = obj.getString("content");
                    search_result = Jsoup.parse(pageName);
                    Elements posts = search_result.getElementsByClass("newsfeed__item");
                    for (Element item : posts) {

                        String group = "https://www.dailystrength.org";
                        Elements link = item.getElementsByClass("newsfeed__btn-container posts__discuss-btn");
                        
                        Newsfeed_item currentItem = new Newsfeed_item();
                        currentItem.replysLink = link.attr("href");
                        int tries=0;
                        while(true){
                        try {
                            Document reply_result = Jsoup.connect(group + currentItem.replysLink).get();
                            Elements description = reply_result.getElementsByClass("posts__content");

                            currentItem.description = description.text();
                            currentItem.subject = requested[i];
                            System.out.println((count++) + ":" + currentItem);
                            threads.add(currentItem);
                            break;
                        }
                        catch(HttpStatusException e)
                        {
                            System.out.println("Ignore this post");
                        }
                        catch(SocketTimeoutException e2)
                        {
                            System.out.println("Connection Timeout");
                            if(tries++==3)
                            {
                                break;
                            }
                        }
                        }
                    }
                    if(!obj.getBoolean("has_more"))
                    {
                        break;
                    }
                }
            }
            System.out.println(threads.size());
        } catch (IOException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static String jsonGetRequest(String urlQueryString) {
        String json = null;
        try {
            java.net.URL url = new java.net.URL(urlQueryString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();
            InputStream inStream = connection.getInputStream();
            json = streamToString(inStream); // input stream to string
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    private static String streamToString(InputStream inputStream) {
        String text = new Scanner(inputStream, "UTF-8").useDelimiter("\\Z").next();
        return text;
    }
}
