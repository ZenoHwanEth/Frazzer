package com.example.cat300;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
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

public class userAdapter extends RecyclerView.Adapter<userAdapter.ViewHolder> {

    private Context context;
    private List<Profile> profiles;
    private int type;

    String lastMessage;

    public userAdapter(Context context, List<Profile> profiles,int type){
        this.context = context;
        this.profiles = profiles;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.usericon,parent,false);
        return new userAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Profile pf = profiles.get(position);
        holder.userName.setText(pf.getUsername());
        if (pf.getImage().equals("default")){
            holder.userPic.setImageResource(R.drawable.user);
        }   else{
            Picasso.get().load(pf.getImage()).into(holder.userPic);
        }
        if(type==1){
            lastMsg(pf.getId(),holder.last);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(type==1){
                    Intent intent = new Intent(context, showMessage.class);
                    intent.putExtra("userid",pf.getId());
                    context.startActivity(intent);
                }else if(type==0){
                    Intent intent = new Intent(context, people_profile.class);
                    intent.putExtra("userid",pf.getEmail());
                    context.startActivity(intent);
                }else if(type==2){
                    Intent intent = new Intent(context, people_profile.class);
                    intent.putExtra("userid",pf.getEmail());
                    context.startActivity(intent);
                    return;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView userName;
        public ImageView userPic;
        public TextView last;

        public ViewHolder(View view){
            super(view);

            last = view.findViewById(R.id.last);
            userName = view.findViewById(R.id.userName);
            userPic = view.findViewById(R.id.userPic);
        }
    }

    private void lastMsg(final String userid, final TextView last){

        lastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid())&&chat.getSender().equals(userid) ||
                    chat.getReceiver().equals(userid)&&chat.getSender().equals(firebaseUser.getUid())){
                        lastMessage = chat.getMessage();
                    }
                }

                switch (lastMessage){
                    case "default":
                        last.setText("No message");
                        break;
                    default:
                        last.setText(lastMessage);
                        break;

                }

                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
