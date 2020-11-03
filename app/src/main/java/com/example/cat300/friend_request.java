package com.example.cat300;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class friend_request extends AppCompatActivity {

    private Button back;
    private RecyclerView recyclerView;
    private userAdapter userAdapter;
    private List<Profile> profiles;
    private List<String> id_list;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        id_list = new ArrayList<>();
        profiles = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        DatabaseReference df1 = FirebaseDatabase.getInstance().getReference("Friends");
        df1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                id_list.clear();
                for(final DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Friends friends = snapshot.getValue(Friends.class);

                    assert friends !=null;
                    if(friends.getReceiver().equals(firebaseUser.getUid()) && friends.getStatus().equals("false")){
                        id_list.add(friends.getSender());
                    }

                }

                readProfile();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readProfile(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                profiles.clear();
                for(DataSnapshot snapshot:dataSnapshot1.getChildren()){
                    Profile profile = snapshot.getValue(Profile.class);

                    for(String id: id_list){
                        if(id.equals(profile.getId())){
                            profiles.add(profile);
                        }
                    }
                }
                userAdapter = new userAdapter(friend_request.this,profiles,2);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError1) {

            }
        });
    }
}
