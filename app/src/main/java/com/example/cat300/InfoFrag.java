package com.example.cat300;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class InfoFrag extends Fragment {

    private Button logout,addMoment,profile,qr;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri filePath;
    private ImageView pic,fullscreen;
    private TextView email,name;
    StorageReference storageReference;
    private StorageTask storageTask;
    private List<Moment> moments;
    private RecyclerView momentView;
    private momentAdapter momentAdapter;
    private RelativeLayout lo,full_pic;
    private int screenWidth;
    private int screenHeight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.infofrag,container,false);

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager wm = getActivity().getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        logout = (Button)view.findViewById(R.id.logout);
        profile = (Button)view.findViewById(R.id.profile);
        pic = (ImageView)view.findViewById(R.id.pic);
        name = (TextView)view.findViewById(R.id.name);
        email =(TextView)view.findViewById(R.id.email);
        addMoment = (Button)view.findViewById(R.id.addMoment);
        qr = (Button)view.findViewById(R.id.qr);
        momentView = (RecyclerView)view.findViewById(R.id.moment);
        lo = (RelativeLayout)view.findViewById(R.id.lo);

        full_pic = new RelativeLayout(getContext());
        lo.addView(full_pic);
        full_pic.getLayoutParams().width = screenWidth;
        full_pic.getLayoutParams().height = screenHeight;
        full_pic.setX(screenWidth);
        full_pic.setY(screenHeight);
        full_pic.setBackgroundColor(R.color.Black);
        fullscreen = new ImageView(getContext());
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


        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            getActivity().finish();
            startActivity(new Intent(getActivity(),start.class));
        }

        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),qrcode.class);
                startActivity(intent);
            }
        });

        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        addMoment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),addMoment.class);
                startActivity(intent);
                return;
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),selfProfile.class);
                startActivity(intent);
                return;
            }
        });



        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Profile profile = dataSnapshot.getValue(Profile.class);
                if(profile.getImage()!="default"){
                    Picasso.get().load(profile.getImage()).into(pic);
                    Picasso.get().load(profile.getImage()).into(fullscreen);
                }else{
                    pic.setImageResource(R.drawable.user);
                    fullscreen.setImageResource(R.drawable.user);
                }
                name.setText("Name: "+profile.getUsername());
                email.setText("Email: "+profile.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                getActivity().finish();
                startActivity(new Intent(getActivity(),start.class));
            }
        });

        momentView.setHasFixedSize(true);
        momentView.setLayoutManager(new LinearLayoutManager(getContext()));

        moments = new ArrayList<>();

        readMoment();

        return view;
    }

    public void readMoment(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Moments");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                moments.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Moment moment = snapshot.getValue(Moment.class);

                    assert moment !=null;
                    assert firebaseUser !=null;

                    if(moment.getId().equals(firebaseUser.getUid())){
                        moments.add(moment);

                    }
                }
                Collections.reverse(moments);
                momentAdapter = new momentAdapter(getContext(),moments);
                momentView.setAdapter(momentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        super.onActivityResult(requestCode, resultCode, data);
        Context applicationContext = MainActivity.getContextOfApplication();
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(), filePath);
                pic.setImageBitmap(bitmap);
                if(filePath!=null){
                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage("Uploading");
                    progressDialog.show();
                    final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(filePath));
                    storageTask = fileReference.putFile(filePath);
                    storageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()){
                                throw task.getException();
                            }

                            return fileReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                Uri uri = task.getResult();
                                String _uri = uri.toString();
                                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("image",_uri);
                                databaseReference.updateChildren(map);
                                progressDialog.dismiss();
                            }else{
                                Toast.makeText(getActivity(),"Failed upload",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }



                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }else{
                    Toast.makeText(getActivity(),"No image",Toast.LENGTH_SHORT).show();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
