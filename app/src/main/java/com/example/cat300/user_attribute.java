package com.example.cat300;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class user_attribute extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner races, religions, hobby,gender,target;
    private Button done;
    private String v_races,v_religions,v_hobby,v_gender,v_target;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_attribute);

        intent = getIntent();

        final String status = intent.getStringExtra("status");
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(getApplicationContext(),start.class));
        }
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        gender=findViewById(R.id.gender);
        target=findViewById(R.id.target);
        done=findViewById(R.id.done);
        races=findViewById(R.id.races);
        religions=findViewById(R.id.religions);
        hobby=findViewById(R.id.hobby);
        races.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                v_races = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        religions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                v_religions= adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        hobby.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                v_hobby= adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                v_gender= adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        target.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                v_target= adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> hashMap1 = new HashMap<>();
                hashMap1.put("gender",v_gender);
                hashMap1.put("target",v_target);
                hashMap1.put("races",v_races);
                hashMap1.put("religions",v_religions);
                hashMap1.put("hobby",v_hobby);
                databaseReference.updateChildren(hashMap1);
                if(status.equals("register")){
                    Intent intent = new Intent(user_attribute.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    finish();
                }
            }
        });

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                gender.setSelection(returnGender(profile.getGender()));
                target.setSelection(returnGender(profile.getTarget()));
                races.setSelection(returnRaces(profile.getRaces()));
                religions.setSelection(returnReligions(profile.getReligions()));
                hobby.setSelection(returnHobby(profile.getHobby()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public int returnRaces(String test){
        if(test.equals("Malays")){
            return 1;
        }
        else if(test.equals("Chinese")){
            return 2;
        }
        else if(test.equals("Indian")){
            return 3;
        }
        return 0;
    }

    public int returnGender(String test){
        if(test.equals("Male")){
            return 0;
        }
        else if(test.equals("Female")){
            return 1;
        }
        return 0;
    }

    public int returnReligions(String test){
        if(test.equals("Islam")){
            return 1;
        }
        else if(test.equals("Buddhism")){
            return 2;
        }
        else if(test.equals("Christian")){
            return 3;
        }
        else if(test.equals("Hindu")){
            return 4;
        }
        return 0;
    }

    public int returnHobby(String test){
        if(test.equals("Music")){
            return 1;
        }
        else if(test.equals("Sport")){
            return 2;
        }
        else if(test.equals("Reading")){
            return 3;
        }
        return 0;
    }
}
