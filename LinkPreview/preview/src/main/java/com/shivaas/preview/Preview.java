package com.shivaas.preview;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preview extends AsyncTask<Void, Void, Void> {

    ProgressDialog mProgressDialog;
    Context context;
    String url;
    String title = null, image = "", desc = null;
    PreviewListener ml;
    public Preview (Context context, String url,PreviewListener ml) {
        super();
        // do stuff
        this.url=url;
        this.context=context;
        this.ml=ml;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
           /* mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("Loading preview...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();*/
    }

    @Override
    protected Void doInBackground(Void... params) {
        Document document = null;


        String pattern = "https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*";

        if (url.matches(pattern)) {
            String pattern1 = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|watch\\?v%3D|\u200C\u200B%2Fvideos%2F|embed%2\u200C\u200BF|youtu.be%2F|%2Fv%2\u200C\u200BF)[^#\\&\\?\\n]*";

            Pattern compiledPattern = Pattern.compile(pattern1);
            Matcher matcher = compiledPattern.matcher(url);

            if (matcher.find()) {

                try {
                    document = Jsoup
                            .connect(url)
                            .userAgent("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36")
                            .get();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                if (document != null) {
                    // Get the html document title
                    title = document.title();
                    Elements description = document
                            .select("meta[name=description]");
                    // Locate the content attribute
                    desc = description.attr("content");


                    // Using Elements to get the class data
                    Elements img = document.select("a[class=brand brand-image] img[src]");
                    // Locate the src attribute
                    image = img.attr("src");
                }
            }
        } else {

            try {
                document = Jsoup
                        .connect(url)
                        .userAgent("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36")
                        .get();


                if (document != null) {
                    // Get the html document title
                    title = document.title();
                    Elements description = document
                            .select("meta[name=description]");
                    // Locate the content attribute
                    desc = description.attr("content");

                    // Using Elements to get the class data
                    Elements img = document.select("a[class=brand brand-image] img[src]");
                    // Locate the src attribute
                    image = img.attr("src");

                    Elements twitterDesc = document.select("meta[name=twitter:card]");

                    String twitterDescription = twitterDesc.attr("content");
                    System.out.println("Twitter Description :" + twitterDescription);
                }

                Elements ogTags = document.select("meta[property^=og:]");
                if (ogTags.size() <= 0) {
                    //return;
                }

                for (int i = 0; i < ogTags.size(); i++) {
                    Element tag = ogTags.get(i);

                    String text = tag.attr("property");
                    if ("og:image".equals(text)) {
                        image = tag.attr("content");
                    } else if ("og:description".equals(text)) {
                        desc = tag.attr("content");
                    } else if ("og:title".equals(text)) {
                        title = tag.attr("content");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        // Set title into TextView
        System.out.println("Title :" + title);
        // Toast.makeText(context,title,Toast.LENGTH_SHORT).show();
        JSONObject jObjectElements = new JSONObject();
        try {
            jObjectElements.put("title",title);
            jObjectElements.put("description",desc);
            jObjectElements.put("image",image);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ml.callback(jObjectElements);
        // mProgressDialog.dismiss();



    }

}
