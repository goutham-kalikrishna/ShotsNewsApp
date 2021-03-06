package com.me.shots.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.shorts.jgirish.snu_pro.AsyncTasks.AsyncTasks.GetNewsImageAsync;
//import com.shorts.jgirish.snu_pro.R;
//import com.shorts.jgirish.snu_pro.Utils.Utils.NewsPOGO;
//import com.shorts.jgirish.snu_pro.Utils.Utils.WebViewActivity;

import com.*;
import com.me.shots.AsyncTasks.GetNewsImageAsync;
import com.me.shots.LoginActivity;
import com.me.shots.R;
import com.me.shots.Utils.NewsPOGO;
import com.me.shots.Utils.WebViewActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jp.wasabeef.blurry.Blurry;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Half_BlooD PrincE on 10/6/2017.
 */

public class VerticalCycleAdapter extends PagerAdapter {

    Context context;
    GetNewsImageAsync getImage;
    static int like=0;
    static int bookmark=0;
    SharedPreferences sharedPreferences;
    ArrayList<NewsPOGO> filteredNews=new ArrayList<>();


    public VerticalCycleAdapter(Context context)
    {
        this.context=context;
        sharedPreferences=context.getSharedPreferences("MYSHAREDPREFERENCES",MODE_PRIVATE);
        String mip=sharedPreferences.getString("mip","lol");
        String interests=sharedPreferences.getString("interests","lol");
        String mydomain=sharedPreferences.getString("mydomain","Nodomain");
        ArrayList<String> my_interests=new ArrayList<>();
        StringTokenizer st1=new StringTokenizer(interests,",");
        if(mydomain.equals("null"))
        {
            mydomain="Programming";
        }
        my_interests.add(mydomain);
        while (st1.hasMoreTokens())
        {
            String token=st1.nextToken();
            my_interests.add(token);
        }

        my_interests.remove("Tech");
        for(int i=0;i<NewsPOGO.newsArray.size();i++)
        {
            String types=NewsPOGO.newsArray.get(i).types;
            for(int j=0;j<my_interests.size();j++)
            if(NewsPOGO.newsArray.get(i).types.contains(my_interests.get(j)))
            {
                filteredNews.add(NewsPOGO.newsArray.get(i));
//                Log.d("interests", "VerticalCycleAdapter: "+my_interests.get(j)+"  "+NewsPOGO.newsArray.get(i).body.substring(0,20));
                break;
            }
        }
    }

    public boolean intersection(List<String> list1, List<String> list2) {
        ArrayList<String> result = new ArrayList<String>(list1);

        result.retainAll(list2);
        if(result.size()!=0)
        return true;
        else
            return false;
    }

    @Override
    public int getCount() {
//        Log.d("tagged", "getCount: "+filteredNews.size());
        return filteredNews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view= LayoutInflater.from(context).inflate(R.layout.fragment_card,container,false);
        TextView textView= (TextView) view.findViewById(R.id.newsText);
        TextView title= (TextView) view.findViewById(R.id.newsTitle);
        TextView timestamp= (TextView) view.findViewById(R.id.newsTimeStamp);
        Log.d("MYTag", "instantiateItem: "+position);
        String timeedit=filteredNews.get(position).timestamp.substring(0,10)+"    "+filteredNews.get(position).timestamp.substring(11,18);
        textView.setText(""+filteredNews.get(position).body);
        title.setText(filteredNews.get(position).title);
        timestamp.setText(timeedit);


        LinearLayout cardLayout= (LinearLayout) view.findViewById(R.id.cardLayout);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, WebViewActivity.class);
                intent.putExtra("Link",filteredNews.get(position).link);
                context.startActivity(intent);

            }
        });




        final TextView likeCount= (TextView) view.findViewById(R.id.like_count);
        likeCount.setText(filteredNews.get(position).likes+"");

//        NewsPOGO.currentPosition=position;

        ImageView imageView= (ImageView) view.findViewById(R.id.newsImage);
        if(filteredNews.get(position).news_image==null)
        {
            new ImageDownloaderTask(imageView,position).execute(filteredNews.get(position).image);
        }
        else
        {
            imageView.setImageBitmap(filteredNews.get(position).news_image);
        }

        if(position==0)
        {
            for(int i=1;i<=10;i++)
            {
                if(position+i==filteredNews.size())
                    break;
                if(filteredNews.get(position+i).news_image==null)
                    new ImageDownloaderTask(imageView,position+i,false).execute(filteredNews.get(position+i).image);
            }
        }
        else
        {
            int i=10;
            if(((position+i)<filteredNews.size())&&filteredNews.get(position+i).news_image==null)
                new ImageDownloaderTask(imageView,position+i,false).execute(filteredNews.get(position+i).image);
        }

        SharedPreferences sharedPreferences=context.getSharedPreferences("MYSHAREDPREFERENCES",MODE_PRIVATE);
        final String userId=sharedPreferences.getString("userid",null);

        final Button like_btn= (Button) view.findViewById(R.id.like_btn);


        if(filteredNews.get(position).liked)
        {
            like_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.love,0,0);
        }
        else
        {
            like_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.unlove,0,0);
        }

        like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!filteredNews.get(position).liked)
                {
                    filteredNews.get(position).liked=true;
                    filteredNews.get(position).likes++;
                    likeCount.setText(filteredNews.get(position).likes+"");
                    new LikeTask(filteredNews.get(position).id+"",userId,0).execute();
                    like_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.love,0,0);
                }
                else
                {
                    filteredNews.get(position).likes--;
                    likeCount.setText(filteredNews.get(position).likes+"");
                    filteredNews.get(position).liked=false;
                    new LikeTask(filteredNews.get(position).id+"",userId,1).execute();
                    like_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.unlove,0,0);
                }
            }
        });


        final Button bookmark_btn=(Button) view.findViewById(R.id.bookmark_btn);

        if(filteredNews.get(position).bookmarked)
        {
            bookmark_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.bookmark,0,0);
        }
        else
        {
            bookmark_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.unbookmark,0,0);
        }

        bookmark_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!filteredNews.get(position).bookmarked)
                {
                    filteredNews.get(position).bookmarked=true;
                    new BookmarkTask(filteredNews.get(position).id+"",userId,0).execute();
                    bookmark_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.bookmark,0,0);

                }
                else
                {
                    filteredNews.get(position).bookmarked=false;
                    new BookmarkTask(filteredNews.get(position).id+"",userId,1).execute();
                    bookmark_btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.unbookmark,0,0);
                }
            }
        });

        final Button share_btn= (Button) view.findViewById(R.id.share_btn);

        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Shots is great: "+filteredNews.get(position).link);
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);
            }
        });


        container.addView(view);
        return view;

    }


    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        int position=-1;
        boolean flag=true;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        public ImageDownloaderTask(ImageView imageView,int position) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.position=position;
        }

        public ImageDownloaderTask(ImageView imageView,int position,boolean flag) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.position=position;
            this.flag=flag;
        }


        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        if(flag)
                        imageView.setImageBitmap(bitmap);
                        filteredNews.get(position).news_image=bitmap;
                        Log.d("imgDownload", "onPostExecute: "+position+"  "+flag);
                    } else {
                        Drawable placeholder = null;
                        if(flag)
                        imageView.setImageDrawable(placeholder);
                    }
                }
            }
        }

        private Bitmap downloadBitmap(String url) {
            HttpURLConnection urlConnection = null;
            try {
                URL uri = new URL(url);
                urlConnection = (HttpURLConnection) uri.openConnection();

                final int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                }
            } catch (Exception e) {
                try{urlConnection.disconnect();}catch (Exception ex){}
                Log.w("ImageDownloader", "Errore durante il download da " + url);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }



    public class LikeTask extends AsyncTask<Void, Void, String> {

        private final String postID;
        private final String userID;
        String API_URL = "http://ec2-52-14-50-89.us-east-2.compute.amazonaws.com";

        LikeTask(String postID, String userID,int upOrDown) {
            this.postID = postID;
            this.userID = userID;
            if(upOrDown==0)
                API_URL=API_URL +"/upvote"+ "/" + postID + "/" + userID;
            else
                API_URL=API_URL +"/undoupvote"+ "/" + postID + "/" + userID;

            Log.e("my","---"+API_URL);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            Log.e("my","---doin");

            String line="False";
            try {
                Log.e("my","---"+API_URL);
                URL url = new URL(API_URL);
                Log.e("my","---"+API_URL);


                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    line = bufferedReader.readLine();
                    Log.e("my","---"+line);
                    bufferedReader.close();

                    return urlConnection.getResponseCode()+"";
                } finally {
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                Log.e("my","!!!"+line);
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("my","---"+line);
            }

            return "Network Error";
        }


        @Override
        protected void onPostExecute(String s) {
            //mProgressView.setVisibility(View.GONE);
//            Toast.makeText(context,"--"+s,Toast.LENGTH_LONG).show();
            //check=s;

        }
    }


    public class BookmarkTask extends AsyncTask<Void, Void, String> {

        private final String postID;
        private final String userID;
        String API_URL = "http://ec2-52-14-50-89.us-east-2.compute.amazonaws.com";

        BookmarkTask(String postID, String userID,int upOrDown) {
            this.postID = postID;
            this.userID = userID;
            if(upOrDown==0)
                API_URL=API_URL + "/bookmark"+ "/" + postID + "/" + userID;
            else
                API_URL=API_URL + "/undobookmark" + "/" + postID + "/" + userID;

            Log.e("my","---"+API_URL);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            Log.e("my","---doin");

            String line="False";
            try {
                Log.e("my","---"+API_URL);
                URL url = new URL(API_URL);
                Log.e("my","---"+API_URL);


                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    line = bufferedReader.readLine();
                    Log.e("my","---"+line);
                    bufferedReader.close();

                    return urlConnection.getResponseCode()+"";
                } finally {
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                Log.e("my","!!!"+line);
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("my","---"+line);
            }

            return "Network Error";
        }


        @Override
        protected void onPostExecute(String s) {
            //mProgressView.setVisibility(View.GONE);
            //Toast.makeText(context,"--"+s,Toast.LENGTH_LONG).show();
            //check=s;

        }
    }

}
