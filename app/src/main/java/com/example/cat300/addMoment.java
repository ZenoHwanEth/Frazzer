package com.example.cat300;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class addMoment extends AppCompatActivity {

    private ImageView image,back;
    private EditText momentText;
    private Button upload;
    private String username ="";
    private String profile_pic="";
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    StorageReference storageReference;
    private StorageTask storageTask;
    private FirebaseUser firebaseUser;
    Integer REQUEST_CAMERA=1, SELECT_FILE=0;
    Bitmap bmp= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_moment);
        image = findViewById(R.id.image);
        momentText = findViewById(R.id.momentText);
        upload = findViewById(R.id.upload);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] items={"Camera","Gallery","Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(addMoment.this);
                builder.setTitle("Add Image");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(items[i].equals("Camera")){
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent,REQUEST_CAMERA);
                        }else if(items[i].equals("Gallery")){
                            Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select File"),SELECT_FILE);
                        }else if(items[i].equals("Cancel")){
                            dialogInterface.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Profile profile = dataSnapshot.getValue(Profile.class);
                username= profile.getUsername();
                profile_pic=profile.getImage();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String txt = momentText.getText().toString();
                if(!txt.equals("") && bmp!=null){

                    final String un=username;
                    final String pp=profile_pic;
                    final ProgressDialog progressDialog = new ProgressDialog(addMoment.this);
                    progressDialog.setMessage("Uploading");
                    progressDialog.show();
                    Context applicationContext = MainActivity.getContextOfApplication();
                    final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+".jpg");
                    Bitmap bitmap1 = Bitmap.createScaledBitmap(bmp,bmp.getWidth(),bmp.getHeight(),true);

                    ByteArrayOutputStream base = new ByteArrayOutputStream();
                    bitmap1.compress(Bitmap.CompressFormat.JPEG,50,base);
                    byte[] final_image = base.toByteArray();

                    storageTask = fileReference.putBytes(final_image);
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
                                DatabaseReference df = FirebaseDatabase.getInstance().getReference();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
                                String currentDateandTime = sdf.format(new Date());
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("id",firebaseUser.getUid());
                                hashMap.put("profile_pic",pp);
                                hashMap.put("name",un);
                                hashMap.put("text",txt);
                                hashMap.put("date",currentDateandTime);
                                hashMap.put("image",_uri);
                                df.child("Moments").push().setValue(hashMap);
                                progressDialog.dismiss();
                                finish();
                            }else{
                                Toast.makeText(addMoment.this,"Failed upload",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                finish();
                            }



                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(addMoment.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            finish();
                        }
                    });

                }else {
                    Toast.makeText( addMoment.this, "You can't upload nothing", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context applicationContext = MainActivity.getContextOfApplication();
        if(requestCode==REQUEST_CAMERA){
            Bundle bundle = data.getExtras();
            bmp = (Bitmap)bundle.get("data");
        }else if (requestCode==SELECT_FILE){
            Uri selectedImageURI = data.getData();
            try{
                bmp = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(), selectedImageURI);
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        image.setImageBitmap(bmp);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
