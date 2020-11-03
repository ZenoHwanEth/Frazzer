package com.example.cat300;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.auth.AuthResult;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class register extends AppCompatActivity {

    private Button register;
    private ImageView pic;
    private EditText email,password,name,phnum;
    private TextView bday;
    private ImageView back;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private boolean upload_pic=false;
    final Calendar calendar = Calendar.getInstance();
    private StorageTask storageTask;
    StorageReference storageReference;
    Integer REQUEST_CAMERA=1, SELECT_FILE=0;
    private Bitmap bmp;
    private String _uri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(register.this);

        pic = findViewById(R.id.pic);
        register = (Button)findViewById(R.id.register);
        email = (EditText)findViewById(R.id.phone);
        password = (EditText)findViewById(R.id.password);
        name = (EditText)findViewById(R.id.name);
        phnum = (EditText)findViewById(R.id.phnum);
        bday = (TextView) findViewById(R.id.bday);
        back= findViewById(R.id.back);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(register.this,start.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] items={"Camera","Gallery","Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(register.this);
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


        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String _email = email.getText().toString().trim();
                final String _password = password.getText().toString().trim();
                final String _name = name.getText().toString().trim();
                final String _phnum = phnum.getText().toString().trim();
                final String _bday = bday.getText().toString().trim();

                if(!upload_pic){
                    Toast.makeText(register.this,"Please upload profile picture",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(_uri.isEmpty()){
                    Toast.makeText(register.this,"Please wait for the picture to be upload",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(_email)){
                    Toast.makeText(register.this,"Please enter email",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(_password)){
                    Toast.makeText(register.this,"Please enter password",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(_name)){
                    Toast.makeText(register.this,"Please enter username",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(_phnum)){
                    Toast.makeText(register.this,"Please enter phone number",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(_password.length()<6 || _password.length()>20){
                    Toast.makeText(register.this,"Password must be 6-20 cahracters.",Toast.LENGTH_SHORT).show();
                    return;
                }

                final String temp_email = _email;
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users");
                db.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            Profile profile = snapshot.getValue(Profile.class);
                            if(profile.getEmail().equals(temp_email)){
                                Toast.makeText(register.this,"Email used! Please enter again!",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        progressDialog.setMessage("Registering user...");
                        progressDialog.show();

                        firebaseAuth.createUserWithEmailAndPassword(_email,_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                    String userid = firebaseUser.getUid();

                                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put("id",userid);
                                    hashMap.put("username",_name);
                                    hashMap.put("phonenumber",_phnum);
                                    hashMap.put("password",_password);
                                    hashMap.put("email",_email);
                                    hashMap.put("birthday",_bday);
                                    hashMap.put("gender","Male");
                                    hashMap.put("target","Female");
                                    hashMap.put("races","Other");
                                    hashMap.put("religions","Other");
                                    hashMap.put("hobby","Other");
                                    hashMap.put("image",_uri);

                                    databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(register.this,"Registered",Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(register.this,user_attribute.class);
                                                intent.putExtra("status","register");
                                                startActivity(intent);
                                                finish();
                                            }
                                            else{
                                                Toast.makeText(register.this,"Failed",Toast.LENGTH_SHORT).show();
                                            }
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });







            }
        });

        bday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(register.this, dateSetListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                calendar.set(Calendar.YEAR, y);
                calendar.set(Calendar.MONTH, m);
                calendar.set(Calendar.DAY_OF_MONTH, d);
                updateLabel();
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        super.onActivityResult(requestCode, resultCode, data);
        Context applicationContext = getApplicationContext();
        bmp= null;
        if(requestCode==REQUEST_CAMERA){
            Bundle bundle = data.getExtras();
            bmp = (Bitmap)bundle.get("data");
        }else if (requestCode==SELECT_FILE){
            try{
                Uri selectedImageURI = data.getData();
                bmp = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(), selectedImageURI);
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if(checkFace(bmp)){
            upload_pic = true;
            pic.setImageBitmap(bmp);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading");
            progressDialog.show();
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+".jpg");
            Bitmap bitmap1 = Bitmap.createScaledBitmap(bmp,bmp.getWidth(),bmp.getHeight(),true);

            ByteArrayOutputStream base = new ByteArrayOutputStream();
            bitmap1.compress(Bitmap.CompressFormat.JPEG,100,base);
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
                        _uri = uri.toString();

                        progressDialog.dismiss();
                    }else{
                        Toast.makeText(register.this,"Failed upload",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(register.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }else{
            Toast.makeText(getApplicationContext(),"No face detected",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.CHINA);

        bday.setText(sdf.format(calendar.getTime()));
    }

    private boolean checkFace(Bitmap bitmap){
        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(bitmap, 0, 0, null);
        FaceDetector faceDetector = new
                FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                .build();
        if(!faceDetector.isOperational()){
            new AlertDialog.Builder(getApplicationContext()).setMessage("Could not set up the face detector!").show();
            return false;
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);
        for(int i=0; i<faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
        }
        if(faces.size()>0){
            return true;
        }else{
            return false;
        }
    }
}
