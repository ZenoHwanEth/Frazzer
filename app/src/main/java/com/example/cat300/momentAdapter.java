package com.example.cat300;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class momentAdapter extends RecyclerView.Adapter<momentAdapter.ViewHolder>{
    private Context context;
    private List<Moment> moments;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    public momentAdapter(Context context, List<Moment> moments){
        this.context = context;
        this.moments = moments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.moment_item,parent,false);
        return new momentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ViewHolder h = holder;
        Moment moment = moments.get(position);
        holder.momentText.setText(moment.getText());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(moment.getId());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Profile profile = dataSnapshot.getValue(Profile.class);
                if(profile.getImage()!="default"){
                    Picasso.get().load(profile.getImage()).into(h.pic);
                }
                h.username.setText(profile.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (moment.getProfile_pic().equals("default")){
            holder.pic.setImageResource(R.drawable.user);
        }else{
            Picasso.get().load(moment.getProfile_pic()).into(holder.pic);
        }
        if (moment.getImage().equals("")){
        }else{
            Picasso.get().load(moment.getImage()).into(holder.image);
        }
        holder.date.setText(moment.getDate());

    }

    @Override
    public int getItemCount() {
        return moments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView pic;
        public TextView momentText;
        public TextView date;
        public ImageView image;

        public ViewHolder(View view){
            super(view);

            username = view.findViewById(R.id.username);
            pic = view.findViewById(R.id.pic);
            momentText = view.findViewById(R.id.momentText);
            date = view.findViewById(R.id.date);
            image = view.findViewById(R.id.image);
        }
    }
}
