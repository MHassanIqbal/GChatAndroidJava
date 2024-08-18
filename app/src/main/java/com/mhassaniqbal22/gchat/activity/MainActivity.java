package com.mhassaniqbal22.gchat.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mhassaniqbal22.gchat.R;
import com.mhassaniqbal22.gchat.adapter.ViewPagerAdapter;
import com.mhassaniqbal22.gchat.fragment.ChatFragment;
import com.mhassaniqbal22.gchat.fragment.GroupFragment;
import com.mhassaniqbal22.gchat.fragment.TopicFragment;
import com.mhassaniqbal22.gchat.helper.ZoomOutPageTransformer;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;


    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onStart();

        Toolbar toolbar = findViewById(R.id.topic_room_toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        fab = findViewById(R.id.fab);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        fab.setImageResource(R.drawable.ic_edit_white_24dp);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                topicDialog();
                            }
                        });
                        break;
                    case 1:
                        fab.setImageResource(R.drawable.ic_edit_white_24dp);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar.make(view, "Create new group", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                        });
                        break;
                    case 2:
                        fab.setImageResource(R.drawable.ic_search_white_24dp);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar.make(view, "Create new topic", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                        });
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TopicFragment(), "TOPICS");
        adapter.addFragment(new GroupFragment(), "GROUPS");
        adapter.addFragment(new ChatFragment(), "CHATS");
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.setAdapter(adapter);
    }

    public void topicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_topic, null);
        builder.setView(dialogView);

        final EditText etTitle = dialogView.findViewById(R.id.et_title);

        builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                title = etTitle.getText().toString().trim();
                if (TextUtils.isEmpty(title)){
                    Toast.makeText(MainActivity.this, R.string.error_field_required, Toast.LENGTH_SHORT).show();
                    topicDialog();
                } else {
//                    writeTopic();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = builder.create();
        b.show();
    }

//    private void writeTopic() {
//        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
//        progressDialog.setTitle(getString(R.string.loading));
//        progressDialog.setMessage(getString(R.string.creating_topic));
//        progressDialog.setIndeterminate(true);
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        String url = "https://gchat-3a570.firebaseio.com/topics.json";
//        firebaseDatabase = FirebaseDatabase.getInstance();
//        databaseReference = firebaseDatabase.getReference("topics");
//        firebaseUser = firebaseAuth.getCurrentUser();
//        StringRequest request = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        if (response.isEmpty()) {
//                            databaseReference.child(title).child("id").setValue(title + "@gchat.com");
//                            databaseReference.child(title).child("title").setValue(title);
//                            databaseReference.child(title).child("title").setValue(title);
//                            databaseReference.child(title).child("lastMessage").setValue("Hey! I'm using G Chat");
//                            databaseReference.child(title).child("timestamp").setValue("12:00 AM");
//
//                            Toast.makeText(MainActivity.this, "Topic Created Successfully", Toast.LENGTH_SHORT).show();
//
//                        } else {
//                            JSONObject object = new JSONObject();
//                            if (!object.has(title)) {
//                                databaseReference.child(title).child("id").setValue(title + "@gchat.com");
//                                databaseReference.child(title).child("title").setValue(title);
//                                databaseReference.child(title).child("title").setValue(title);
//                                databaseReference.child(title).child("lastMessage").setValue("Hey! I'm using G Chat");
//                                databaseReference.child(title).child("timestamp").setValue("12:00 AM");
//
//                                Toast.makeText(MainActivity.this, "Topic Created Successfully", Toast.LENGTH_SHORT).show();
//
//                            } else {
//                                Toast.makeText(MainActivity.this, "Topic Already Exist", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        progressDialog.dismiss();
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                System.out.println("" + error);
//                Toast.makeText(MainActivity.this,"" + error, Toast.LENGTH_SHORT).show();
//                progressDialog.dismiss();
//            }
//        });
//
//        RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
//        rQueue.add(request);
//    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Toast.makeText(getApplicationContext(), "Search...", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
