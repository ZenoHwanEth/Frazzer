package com.example.cat300;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cat300.notification.APIService;
import com.example.cat300.notification.Client;
import com.example.cat300.notification.Data;
import com.example.cat300.notification.MyResponse;
import com.example.cat300.notification.Sender;
import com.example.cat300.notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class showMessage extends AppCompatActivity {
    
    private CircleImageView pic;
    private TextView username;
    private Button sendBtn, back;
    private EditText sendText;

    private messageAdapter messageAdapter;
    private List<Chat> chats;
    private RecyclerView recyclerView;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    
    private Intent intent;

    private ValueEventListener statusListener;

    private String userid;

    private APIService apiService;

    private Boolean notify=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        back = findViewById(R.id.back);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        pic = findViewById(R.id.pic);
        username = findViewById(R.id.username);
        sendBtn = findViewById(R.id.sendBtn);
        sendText = findViewById(R.id.sendText);

        intent = getIntent();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        userid = intent.getStringExtra("userid");
        final String userId = intent.getStringExtra("userid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                username.setText(profile.getUsername());
                if (profile.getImage().equals("default")){
                    pic.setImageResource(R.drawable.user);
                }else{
                    Picasso.get().load(profile.getImage()).into(pic);
                }

                readMsg(firebaseUser.getUid(),userId,profile.getImage());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String txt = sendText.getText().toString();
                if(!txt.equals("")){
                    DatabaseReference df = FirebaseDatabase.getInstance().getReference();

                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("sender",firebaseUser.getUid());
                    hashMap.put("receiver",userId);
                    hashMap.put("message",txt);
                    hashMap.put("status",false);
                    df.child("Chats").push().setValue(hashMap);
                    final String msg = txt;
                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Profile profile = dataSnapshot.getValue(Profile.class);
                            if(notify){
                                sendNotice(userId,profile.getUsername(),msg);

                            }
                            notify=false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else {
                    Toast.makeText( showMessage.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                sendText.setText("");


            }
        });
        messageStatus(userId);
    }

    private void messageStatus(final String userid){
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        statusListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid())&&chat.getSender().equals(userid)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("status",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotice(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(),R.mipmap.ic_launcher,username+": "+message,"New Message",userid);
                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code()==200){
                                if (response.body().success!=1){
                                    Toast.makeText(showMessage.this,"Failed",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMsg(final String id1,final String id2, final String url){
        chats = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(id1) && chat.getSender().equals(id2) ||
                    chat.getReceiver().equals(id2) && chat.getSender().equals(id1)){
                        chats.add(chat);
                    }

                    messageAdapter = new messageAdapter(showMessage.this,chats,url);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
