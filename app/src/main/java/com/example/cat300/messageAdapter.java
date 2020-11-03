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
import com.squareup.picasso.Picasso;

import java.util.List;

public class messageAdapter extends RecyclerView.Adapter<messageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Chat> chats;
    private String url;

    FirebaseUser firebaseUser;

    public messageAdapter(Context context, List<Chat> chats, String url){
        this.context = context;
        this.chats = chats;
        this.url = url;
    }

    @NonNull
    @Override
    public messageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chatright,parent,false);
            return new messageAdapter.ViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.chatleft,parent,false);
            return new messageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull messageAdapter.ViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.message.setText(chat.getMessage());

        if (url.equals("default")){
            holder.pic.setImageResource(R.drawable.user);
        }else{
            Picasso.get().load(url).into(holder.pic);
        }

        if(position == chats.size()-1){
            if(chat.getStatus()){
                holder.status.setText("Seen");
            }else{
                holder.status.setText("Delivered");
            }

        }else{
            holder.status.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView message;
        public ImageView pic;

        public TextView status;


        public ViewHolder(View view){
            super(view);

            message = view.findViewById(R.id.message);
            pic = view.findViewById(R.id.pic);
            status= view.findViewById(R.id.status);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chats.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return  MSG_TYPE_LEFT;
        }
    }
}
