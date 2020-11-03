package com.example.cat300;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.webkit.MimeTypeMap;
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
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import id.zelory.compressor.Compressor;

public class selfProfile extends AppCompatActivity {

    private ImageView qrcode;
    private ImageView profile_pic;
    private Button userbtn,birthbtn,edit_pic,phonebtn,back,confirm,attribute;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private Uri filePath;
    private EditText username,phnum;
    private TextView bday;
    StorageReference storageReference;
    private StorageTask storageTask;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    Integer REQUEST_CAMERA=1, SELECT_FILE=0;
    final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_profile);



        confirm = findViewById(R.id.confirm);
        back=findViewById(R.id.back);
        username=findViewById(R.id.username);
        profile_pic=findViewById(R.id.profile_pic);
        phnum = findViewById(R.id.phone);
        bday=findViewById(R.id.birthday);
        qrcode=findViewById(R.id.qr);
        attribute = findViewById(R.id.attribute);
        userbtn = findViewById(R.id.userbtn);
        birthbtn = findViewById(R.id.birthbtn);
        phonebtn = findViewById(R.id.phonebtn);
        edit_pic = findViewById(R.id.edit_pic);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        attribute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(selfProfile.this,user_attribute.class);
                intent.putExtra("status","notregister");
                startActivity(intent);
                finish();
            }
        });

        bday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(selfProfile.this, dateSetListener, calendar
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

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            this.finish();
            startActivity(new Intent(selfProfile.this,start.class));
        }

        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(selfProfile.this,qrcode.class);
                startActivity(intent);
                return;
            }
        });

        edit_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] items={"Camera","Gallery","Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(selfProfile.this);
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
                username.setText(profile.getUsername());
                phnum.setText(profile.getPhonenumber());
                bday.setText(profile.getBirthday());
                if(profile.getImage()!="default"){
                    Picasso.get().load(profile.getImage()).into(profile_pic);
                }else{
                    profile_pic.setImageResource(R.drawable.user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                HashMap<String, Object> map = new HashMap<>();
                map.put("username",username.getText().toString().trim());
                map.put("phonenumber",phnum.getText().toString().trim());
                map.put("birthday",bday.getText().toString().trim());
                databaseReference.updateChildren(map);
                Toast.makeText(selfProfile.this,"Done!",Toast.LENGTH_SHORT).show();
                finish();
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
        Bitmap bmp= null;
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
        if(checkFace(bmp)){
            profile_pic.setImageBitmap(bmp);
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
                        String _uri = uri.toString();
                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("image",_uri);
                        databaseReference.updateChildren(map);
                        progressDialog.dismiss();
                    }else{
                        Toast.makeText(selfProfile.this,"Failed upload",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(selfProfile.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }else{
            Toast.makeText(getApplicationContext(),"No face detected",Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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
