package com.example.cat300;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FrenFrag extends Fragment {

    private RecyclerView recyclerView;
    private ImageView pic,scan;
    private userAdapter userAdapter;
    private List<Profile> profiles;
    private List<String> userlist;
    private EditText uid;
    private Button request;
    private FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frenfrag,container,false);
        userlist = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pic=view.findViewById(R.id.pic);
        scan=view.findViewById(R.id.scan);
        request=view.findViewById(R.id.request);
        profiles = new ArrayList<>();
        uid=view.findViewById(R.id.uid);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),friend_request.class);
                startActivity(intent);
                return;
            }
        });

        readUser();

        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkExist(uid.getText().toString());
                uid.setText("");
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),scanqr.class);
                startActivity(intent);
                return;
            }
        });


        return view;
    }

    private void readUser() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Friends");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userlist.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Friends friends = snapshot.getValue(Friends.class);

                    if(friends.getSender().equals(firebaseUser.getUid())&&friends.getStatus().equals("true")){
                        userlist.add(friends.getReceiver());
                    }
                    if(friends.getReceiver().equals(firebaseUser.getUid())&&friends.getStatus().equals("true")){
                        userlist.add(friends.getSender());
                    }
                }

                readChat();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readChat() {
        profiles = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profiles.clear();

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Profile profile = snapshot.getValue(Profile.class);

                    for(String id: userlist){
                        if(profile.getId().equals(id)){
                            if(!profiles.contains(profile)){

                                profiles.add(profile);
                            }

                        }
                    }
                }
                userAdapter = new userAdapter(getContext(),profiles,0);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkExist(String uid){
        final String userid=uid;
        DatabaseReference df = FirebaseDatabase.getInstance().getReference("Users");
        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean existma=false;
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Profile profile = snapshot.getValue(Profile.class);
                    if(profile.getEmail().equals(userid)){
                        existma = true;
                        break;
                    }
                }
                if(existma){
                    Intent intent = new Intent(getContext(), people_profile.class);
                    intent.putExtra("userid",userid);
                    startActivity(intent);
                }else{
                    Toast.makeText(getContext(),userid+" not exist",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
