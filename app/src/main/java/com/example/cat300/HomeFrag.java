package com.example.cat300;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class HomeFrag extends Fragment {

    private int screenWidth;
    private int screenHeight;
    private RelativeLayout[] alayout = new RelativeLayout[200];
    private ImageView[] img = new ImageView[200];
    private TextView[] txt2= new TextView[200];
    private TextView[] txt = new TextView[200];
    private RelativeLayout lo;
    private FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    private int delta_x=0;
    private View view;
    private List<String> matchid;
    private List<Double> matchpoint;
    private List<String> matchemail;
    private ImageView want,cancel;

    private static DecimalFormat format = new DecimalFormat("0.00");

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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.homefrag,container,false);

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager wm = getActivity().getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        cancel = view.findViewById(R.id.cancel);
        want = view.findViewById(R.id.want);
        lo = view.findViewById(R.id.lo);
        matchid = new ArrayList<>();
        matchpoint = new ArrayList<>();
        matchemail = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference df = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        for(int x = 200-1 ;x>=0;x--){
            alayout[x] = new RelativeLayout(getActivity());
            lo.addView(alayout[x]);
            img[x] = new ImageView(getContext());
            txt[x] = new TextView(getContext());
            txt2[x] = new TextView(getContext());
            alayout[x].addView(img[x]);
            alayout[x].addView(txt[x]);
            alayout[x].addView(txt2[x]);
            img[x].setX(screenWidth);
            img[x].setBackgroundColor(R.color.Black);
            img[x].setY(screenHeight);
            txt[x].setX(screenWidth);
            txt[x].setY(screenHeight);
            txt2[x].setX(screenWidth);
            txt2[x].setY(screenHeight);
        }
        df.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                matchid.clear();
                matchpoint.clear();
                matchemail.clear();
                final Profile attributes = dataSnapshot.getValue(Profile.class);
                DatabaseReference df1 = FirebaseDatabase.getInstance().getReference("Users");
                df1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                        matchid.clear();
                        matchpoint.clear();
                        matchemail.clear();
                        for (DataSnapshot snapshot: dataSnapshot1.getChildren()){
                            Profile attributes1 = snapshot.getValue(Profile.class);
                            if(snapshot.getKey().equals(firebaseUser.getUid())){
                                continue;
                            }

                            if(attributes1.getGender().equals(attributes.getTarget())){
                                matchid.add(snapshot.getKey());

                                double score = 0.0;
                                if(attributes.getHobby().equals(attributes1.getHobby())){
                                    score+=10;
                                }
                                if(attributes.getRaces().equals(attributes1.getRaces())){
                                    score+=10;
                                }
                                if(attributes.getReligions().equals(attributes1.getReligions())){
                                    score+=10;
                                }
                                matchpoint.add(score);
                                matchemail.add(attributes1.getEmail());
                            }
                        }
                        DatabaseReference df4 = FirebaseDatabase.getInstance().getReference("Friends");

                        df4.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot5) {
                                for(DataSnapshot snapshot5:dataSnapshot5.getChildren()){
                                    Friends friends = snapshot5.getValue(Friends.class);
                                    for(int z = 0; z<matchid.size(); z++){
                                        if((friends.getReceiver().equals(firebaseUser.getUid())&&friends.getSender().equals(matchid.get(z)))||friends.getReceiver().equals(matchid.get(z))&&friends.getSender().equals(firebaseUser.getUid())){
                                            matchid.remove(z);
                                            matchemail.remove(z);
                                            matchpoint.remove(z);
                                            break;
                                        }
                                    }

                                }
                                DatabaseReference df3 = FirebaseDatabase.getInstance().getReference("Match").child(firebaseUser.getUid());
                                df3.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                                            if(matchid.contains(ds.getKey())){
                                                Integer key=matchid.indexOf(ds.getKey());
                                                matchpoint.set(key,matchpoint.get(key)+(1-(Double.parseDouble(ds.getValue().toString())))*70);
                                            }
                                        }

                                        String[] tempid = new String[matchid.size()];
                                        final String[] tempemail = new String[matchid.size()];
                                        final double[] tempscore = new double[matchid.size()];
                                        for(int x=0;x<matchid.size();x++){
                                            tempid[x] = matchid.get(x);
                                            tempscore[x] = matchpoint.get(x);
                                            tempemail[x] = matchemail.get(x);
                                        }

                                        for (int i = 0; i < ( matchid.size() - 1 ); i++) {
                                            for (int j = 0; j < matchid.size() - i - 1; j++) {
                                                if (tempscore[j] < tempscore[j+1])
                                                {
                                                    double temp1 = tempscore[j];
                                                    tempscore[j] = tempscore[j+1];
                                                    tempscore[j+1] = temp1;
                                                    String temp2 = tempid[j];
                                                    tempid[j] = tempid[j+1];
                                                    tempid[j+1] = temp2;
                                                    String temp3 = tempemail[j];
                                                    tempemail[j] = tempemail[j+1];
                                                    tempemail[j+1] = temp3;
                                                }
                                            }
                                        }


                                        View.OnTouchListener myTouchListener= new View.OnTouchListener() {
                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {
                                                final int X = (int)event.getRawX();
                                                switch(event.getAction()&MotionEvent.ACTION_MASK){
                                                    case MotionEvent.ACTION_DOWN:
                                                        delta_x = X -(int)v.getX();
                                                        break;
                                                    case MotionEvent.ACTION_UP:
                                                        if(v.getX()+v.getWidth()/2<0){
                                                            v.setX(screenWidth);
                                                            Intent intent = new Intent(getContext(), people_profile.class);
                                                            intent.putExtra("userid",tempemail[v.getId()]);
                                                            startActivity(intent);
                                                        }else if(v.getX()+v.getWidth()/2>screenWidth){
                                                            v.setX(screenWidth);
                                                        }else{
                                                            v.setX(screenWidth/2-v.getLayoutParams().width/2);
                                                        }
                                                        break;
                                                    case MotionEvent.ACTION_POINTER_DOWN:
                                                        break;
                                                    case MotionEvent.ACTION_POINTER_UP:
                                                        break;
                                                    case MotionEvent.ACTION_MOVE:
                                                        float temp = v.getX();
                                                        v.setX(X-delta_x) ;
                                                        break;
                                                }
                                                lo.invalidate();
                                                return true;
                                            }
                                        };
                                        for(int x=matchid.size()-1;x>=0;x--){
                                            alayout[x].setX(screenWidth);
                                        }
                                        for(int x=matchid.size()-1;x>=0;x--){
                                            DatabaseReference df = FirebaseDatabase.getInstance().getReference("Users").child(tempid[x]);

                                            alayout[x].getLayoutParams().height=screenHeight*4/10;
                                            alayout[x].getLayoutParams().width=screenWidth*9/10;
                                            alayout[x].setX(screenWidth/2-alayout[x].getLayoutParams().width/2);
                                            alayout[x].setY(screenHeight/2-alayout[x].getLayoutParams().height/2 -alayout[x].getLayoutParams().height/10);
                                            alayout[x].setId(x);
                                            img[x].getLayoutParams().width = screenWidth*9/10;
                                            img[x].getLayoutParams().height = screenHeight/3;
                                            img[x].setY(0);
                                            img[x].setX(0);
                                            alayout[x].setBackgroundResource(R.drawable.round_btn);
                                            txt[x].setY(screenHeight/2-alayout[x].getLayoutParams().height/2-alayout[x].getLayoutParams().height/7);
                                            txt2[x].setY(screenHeight/2-alayout[x].getLayoutParams().height/2-alayout[x].getLayoutParams().height/7);

                                            alayout[x].setOnTouchListener(myTouchListener);
                                            final int b=x;
                                            df.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Profile profile = dataSnapshot.getValue(Profile.class);
                                                    if(profile.getImage()!="default"){
                                                        Picasso.get().load(profile.getImage()).into(img[b]);
                                                    }else{
                                                        Picasso.get().load("https://cdn1.iconfinder.com/data/icons/business-users/512/circle-512.png").into(img[b]);
                                                    }
                                                    txt[b].setText(profile.getUsername().toUpperCase() +" , "+ findAge(Integer.parseInt(profile.getBirthday().substring(6,8))));
                                                    txt[b].setX(20);
                                                    txt[b].setTypeface(Typeface.SERIF,Typeface.BOLD);
                                                    txt[b].setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
                                                    txt[b].setTextColor(Color.parseColor("#FFFFFF"));
                                                    txt[b].setGravity(Gravity.CENTER_HORIZONTAL);

                                                    txt2[b].setText("\n\nScore : "+format.format(tempscore[b]));
                                                    txt2[b].setX(screenWidth/2 - 300);
                                                    txt2[b].setTypeface(Typeface.SERIF);
                                                    txt2[b].setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
                                                    txt2[b].setTextColor(Color.parseColor("#242729"));
                                                    txt2[b].setGravity(Gravity.CENTER_HORIZONTAL);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
}
