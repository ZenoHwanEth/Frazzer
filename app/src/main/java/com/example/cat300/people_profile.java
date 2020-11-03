package com.example.cat300;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cat300.notification.APIService;
import com.example.cat300.notification.Client;
import com.example.cat300.notification.Data;
import com.example.cat300.notification.MyResponse;
import com.example.cat300.notification.Sender;
import com.example.cat300.notification.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class people_profile extends AppCompatActivity {

    private Intent intent;
    private ImageView pic,back,fullscreen;
    private Button send;
    private RecyclerView momentView;
    private TextView name,id,text,race,religion,hobby,gender,age;
    private String userId;
    private int screenWidth;
    private int screenHeight;
    private RelativeLayout lo,full_pic;
    private List<Moment> moments;
    private momentAdapter momentAdapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    StorageReference storageReference;
    private Boolean notify=false;
    private APIService apiService;


    private int findAge(int Year){
        int Age;
        if(Year/10==0||Year/10==1||Year/10==2)
        {
            Age =19-Year;
        }
        else {
            Year = 1900 + Year;
            Age = 2019 - Year;
        }
        return Age;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_profile);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager wm = getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        intent = getIntent();
        userId = intent.getStringExtra("userid");
        pic = findViewById(R.id.pic);
        back=findViewById(R.id.back);
        send=findViewById(R.id.send);
        text=findViewById(R.id.profile_text);
        momentView=findViewById(R.id.moment);
        name=findViewById(R.id.name);
        id=findViewById(R.id.id);
        race=findViewById(R.id.race_text);
        religion=findViewById(R.id.religion_text);
        hobby=findViewById(R.id.hobby_text);
        gender=findViewById(R.id.gender_text);
        age=findViewById(R.id.age_text);

        lo = findViewById(R.id.lo);

        full_pic = new RelativeLayout(getApplicationContext());
        lo.addView(full_pic);
        full_pic.getLayoutParams().width = screenWidth;
        full_pic.getLayoutParams().height = screenHeight;
        full_pic.setX(screenWidth);
        full_pic.setY(screenHeight);
        full_pic.setBackgroundColor(R.color.Black);
        fullscreen = new ImageView(getApplicationContext());
        full_pic.addView(fullscreen);
        fullscreen.setBackgroundColor(R.color.Black);
        fullscreen.getLayoutParams().width=screenWidth;
        fullscreen.getLayoutParams().height=screenHeight;
        fullscreen.setX(screenWidth/2 - fullscreen.getLayoutParams().width/2);
        fullscreen.setX(screenHeight/2 - fullscreen.getLayoutParams().height/2);

        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                full_pic.setX(0);
                full_pic.setY(0);
            }
        });

        full_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                full_pic.setX(screenWidth);
                full_pic.setY(screenHeight);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        send.setText("");
        text.setText("Add Friend");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference df = FirebaseDatabase.getInstance().getReference();
                notify=true;
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("sender",firebaseUser.getUid());
                hashMap.put("receiver",userId);
                hashMap.put("status","false");
                df.child("Friends").push().setValue(hashMap);
                if(notify){
                    sendNotice(userId);

                }
                notify=false;
            }
        });

        DatabaseReference dft = FirebaseDatabase.getInstance().getReference("Users");
        dft.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot1:dataSnapshot.getChildren()){
                    Profile profile = snapshot1.getValue(Profile.class);


                    if(profile.getEmail().equals(userId)){
                        userId = profile.getId();
                        DatabaseReference df1 = FirebaseDatabase.getInstance().getReference("Friends");
                        df1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean value = false;
                                for(DataSnapshot snapshot6:dataSnapshot.getChildren()){
                                    final DataSnapshot snapshot = snapshot6;
                                    Friends friends = snapshot.getValue(Friends.class);
                                    assert friends !=null;
                                    if(friends.getSender().trim().equals(firebaseUser.getUid().trim()) && friends.getReceiver().trim().equals(userId.trim()) && friends.getStatus().trim().equals("false")){
                                        send.setX(screenWidth);
                                        Toast.makeText(people_profile.this,"Friend request sent!",Toast.LENGTH_SHORT).show();
                                        value=true;
                                        text.setX(screenWidth);
                                        break;
                                    }else if((friends.getSender().trim().equals(firebaseUser.getUid().trim()) && friends.getReceiver().trim().equals(userId.trim()) && friends.getStatus().trim().equals("true"))||(friends.getSender().trim().equals(userId.trim()) && friends.getReceiver().trim().equals(firebaseUser.getUid().trim()) && friends.getStatus().trim().equals("true"))){
                                        send.setBackgroundResource(R.drawable.conversation);
                                        text.setText("Let's Chat!");
                                        send.setHeight(screenHeight/7);
                                        send.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(people_profile.this, showMessage.class);
                                                intent.putExtra("userid",userId);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                        value=true;
                                        break;
                                    }else if(friends.getSender().trim().equals(userId.trim()) && friends.getReceiver().trim().equals(firebaseUser.getUid().trim()) && friends.getStatus().trim().equals("false")){
                                        send.setBackgroundResource(R.drawable.add_friend);
                                        text.setText("Add Friend");
                                        send.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                DatabaseReference df = FirebaseDatabase.getInstance().getReference("Friends").child(snapshot.getKey());
                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("status","true");
                                                df.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(people_profile.this,"Accepted",Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                        else{
                                                            Toast.makeText(people_profile.this,"Failed",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                        value=true;
                                        break;
                                    }
                                }
                                if(!value){
                                    send.setBackgroundResource(R.drawable.add_friend);
                                    send.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            DatabaseReference df = FirebaseDatabase.getInstance().getReference();
                                            notify=true;
                                            HashMap<String,Object> hashMap = new HashMap<>();
                                            hashMap.put("sender",firebaseUser.getUid());
                                            hashMap.put("receiver",userId);
                                            hashMap.put("status","false");
                                            df.child("Friends").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(people_profile.this,"Friend request sent",Toast.LENGTH_SHORT).show();
                                                        if(notify){
                                                            sendNotice(userId);
                                                        }
                                                        notify=false;
                                                    }
                                                    else{
                                                        Toast.makeText(people_profile.this,"Failed",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });


                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        storageReference = FirebaseStorage.getInstance().getReference("uploads");


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Profile profile = snapshot.getValue(Profile.class);
                    if(profile.getId().equals(userId)){
                        if(profile.getImage()!="default"){
                            Picasso.get().load(profile.getImage()).into(pic);
                            Picasso.get().load(profile.getImage()).into(fullscreen);
                        }else{
                            Picasso.get().load("https://cdn1.iconfinder.com/data/icons/business-users/512/circle-512.png").into(pic);
                            Picasso.get().load("https://cdn1.iconfinder.com/data/icons/business-users/512/circle-512.png").into(fullscreen);
                        }
                        race.setText("Race : " +profile.getRaces());
                        religion.setText("Religion : " +profile.getReligions());
                        hobby.setText("Hobby : " +profile.getHobby());
                        name.setText("Name : "+profile.getUsername());
                        id.setText("Email : "+profile.getEmail());
                        gender.setText("Gender : " + profile.getGender());
                        age.setText("Age : " + findAge(Integer.parseInt(profile.getBirthday().substring(6,8))));
                        break;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        momentView.setHasFixedSize(true);
        momentView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        moments = new ArrayList<>();

        readMoment();

    }

    public void readMoment(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Moments");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                moments.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Moment moment = snapshot.getValue(Moment.class);

                    assert moment !=null;

                    if(moment.getId().equals(userId)){
                        moments.add(moment);
                    }
                }
                Collections.reverse(moments);
                momentAdapter = new momentAdapter(people_profile.this,moments);
                momentView.setAdapter(momentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotice(final String receiver){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(),R.mipmap.ic_launcher,"","New Friend Request",receiver);
                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code()==200){
                                if (response.body().success!=1){
                                    Toast.makeText(people_profile.this,"Failed",Toast.LENGTH_SHORT).show();
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
}
