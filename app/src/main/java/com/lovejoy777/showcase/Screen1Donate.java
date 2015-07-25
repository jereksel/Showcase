package com.lovejoy777.showcase;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovejoy777.showcase.adapters.CardViewAdapter;
import com.lovejoy777.showcase.adapters.RecyclerItemClickListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by lovejoy777 on 24/06/15.
 */
public class Screen1Donate extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    ArrayList<Theme> themesList;
    private CardViewAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen1);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        themesList = new ArrayList<Theme>();

        new JSONAsyncTask().execute();

        mRecyclerView = (RecyclerView) findViewById(R.id.cardList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new CardViewAdapter(themesList, R.layout.adapter_card_layout, this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(Screen1Donate.this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String free = String.valueOf(themesList.get(position).isFree());
                        String title = themesList.get(position).getTitle();
                        String link = themesList.get(position).getLink();
                        String googleplus = themesList.get(position).getGoogleplus();
                        String promo = themesList.get(position).getPromo();
                        String developer = themesList.get(position).getAuthor();
                        String screenshot_1 = themesList.get(position).getScreenshot_1();
                        String screenshot_2 = themesList.get(position).getScreenshot_2();
                        String screenshot_3 = themesList.get(position).getScreenshot_3();
                        String description = themesList.get(position).getDescription();
                        String donate_link = themesList.get(position).getDonate_link();


                        Intent DonateDetails = new Intent(Screen1Donate.this, DonateDetails.class);

                        DonateDetails.putExtra("free", free);
                        DonateDetails.putExtra("keytitle", title);
                        DonateDetails.putExtra("keylink", link);
                        DonateDetails.putExtra("keydonate_link", donate_link);
                        DonateDetails.putExtra("keygoogleplus", googleplus);
                        DonateDetails.putExtra("keypromo", promo);
                        DonateDetails.putExtra("keyscreenshot_1", screenshot_1);
                        DonateDetails.putExtra("keyscreenshot_2", screenshot_2);
                        DonateDetails.putExtra("keyscreenshot_3", screenshot_3);
                        DonateDetails.putExtra("keydescription", description);
                        DonateDetails.putExtra("keydeveloper", developer);

                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                        startActivity(DonateDetails, bndlanimation);
                    }
                })
        );

        //initialize swipetorefresh
        mSwipeRefresh.setColorSchemeResources(R.color.accent, R.color.primary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                themesList.clear();
                new JSONAsyncTask().execute();
                onItemsLoadComplete();
            }

            void onItemsLoadComplete() {
            }
        });
    }

    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefresh.setRefreshing(true);
                }
            });
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {

                File tagname = new File(Environment.getExternalStorageDirectory() + "/showcase/showcasejson/showcase.json");
                FileInputStream stream = new FileInputStream(tagname);
                String jString = null;
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                /* Instead of using default, pass in a decoder. */
                    jString = Charset.defaultCharset().decode(bb).toString();
                } finally {
                    stream.close();
                }

                JSONObject jsono = new JSONObject(jString);
                JSONArray jarray = jsono.getJSONArray("Themes");

                Random rnd = new Random();
                for (int i = jarray.length() - 1; i >= 0; i--) {
                    int j = rnd.nextInt(i + 1);

                    // Simple swap
                    JSONObject object = jarray.getJSONObject(j);
                    jarray.put(j, jarray.get(i));
                    jarray.put(i, object);


                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    Theme theme = objectMapper
                            .readValue(object.toString(), Theme.class);

                    if (theme.isDonate()) {
                        themesList.add(theme);
                    }
                }
                return true;

                //------------------>>

            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            mAdapter.notifyDataSetChanged();
            if (result == false)
                Toast.makeText(getApplicationContext(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();
            mSwipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefresh.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }


}