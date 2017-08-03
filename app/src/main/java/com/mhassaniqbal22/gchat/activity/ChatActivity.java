package com.mhassaniqbal22.gchat.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mhassaniqbal22.gchat.R;
import com.mhassaniqbal22.gchat.model.Message;
import com.mhassaniqbal22.gchat.preferences.CodelabPreferences;


import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public static final String MESSAGES_CHILD = "messages";
    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 100;
    private static final int REQUEST_IMAGE = 2;
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> firebaseAdapter;

    private EditText inputMessage;
    private FloatingActionButton fabSend;
    private ProgressBar progressBar;
    private ImageView addMessageImageView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;


    private String uid;
    private String username;
    private String photoUrl;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        username = ANONYMOUS;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        getSupportActionBar().setTitle(title);
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        if (firebaseUser != null) {
            username = firebaseUser.getEmail();
            if (firebaseUser.getPhotoUrl() != null) {
                photoUrl = firebaseUser.getPhotoUrl().toString();
            }
        }

        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recycler_view);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        firebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.item_message,
                MessageViewHolder.class,
                databaseReference.child(MESSAGES_CHILD)) {

            @Override
            protected Message parseSnapshot(DataSnapshot snapshot) {
                Message message = super.parseSnapshot(snapshot);
                if (message != null) {
                    message.setId(snapshot.getKey());
                }
                return message;
            }

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              Message message, int position) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                if (message.getText() != null) {
                    viewHolder.messageTextView.setText(message.getText());
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                    viewHolder.messageImageView.setVisibility(ImageView.GONE);
                } else {
                    String imageUrl = message.getImageUrl();
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(viewHolder.messageImageView.getContext())
                                                    .load(downloadUrl)
                                                    .into(viewHolder.messageImageView);
                                        } else {
                                            Toast.makeText(ChatActivity.this, "Getting download url was not successful.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Glide.with(viewHolder.messageImageView.getContext())
                                .load(message.getImageUrl())
                                .into(viewHolder.messageImageView);
                    }
                    viewHolder.messageImageView.setVisibility(ImageView.VISIBLE);
                    viewHolder.messageTextView.setVisibility(TextView.GONE);
                }


                viewHolder.messengerTextView.setText(message.getName());
                if (message.getPhotoUrl() == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
                            R.drawable.ic_account_circle_black_24dp));
                } else {
                    Glide.with(ChatActivity.this)
                            .load(message.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }

            }
        };

        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(firebaseAdapter);

        inputMessage = findViewById(R.id.input_message);
        inputMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(sharedPreferences
                .getInt(CodelabPreferences.MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    fabSend.setEnabled(true);
                } else {
                    fabSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        addMessageImageView = findViewById(R.id.addMessageImageView);
        addMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        fabSend = findViewById(R.id.send);
        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message(inputMessage.getText().toString(), username,
                        photoUrl, null);
                databaseReference.child(MESSAGES_CHILD).push().setValue(message);
                inputMessage.setText("");
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                Toast.makeText(getApplicationContext(), "Info..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_refresh:
                Toast.makeText(getApplicationContext(), "refresh..", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messengerTextView = itemView.findViewById(R.id.messengerTextView);
            messengerImageView = itemView.findViewById(R.id.messengerImageView);
        }
    }
}
