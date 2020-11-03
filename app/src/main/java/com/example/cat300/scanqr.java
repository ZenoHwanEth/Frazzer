package com.example.cat300;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class scanqr extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView qrscan;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanqr);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        qrscan=findViewById(R.id.qrscan);

        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                qrscan.setResultHandler(scanqr.this);
                qrscan.startCamera();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(scanqr.this,"Accept the camera permission",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        })
                .check();
    }

    @Override
    protected void onDestroy() {
        qrscan.stopCamera();
        super.onDestroy();
    }

    @Override
    public void handleResult(Result rawResult) {
        checkExist(rawResult.getText());
    }

    private void checkExist(String uid){
        final String userid=uid;
        DatabaseReference df = FirebaseDatabase.getInstance().getReference("Users");
        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String emaillll="";
                boolean exist = false;
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Profile profile = snapshot.getValue(Profile.class);
                    if(profile.getEmail().equals(userid)){
                        exist = true;
                        emaillll = profile.getEmail();
                    }
                }
                if(exist){
                    Intent intent = new Intent(scanqr.this, people_profile.class);
                    intent.putExtra("userid",emaillll);
                    startActivity(intent);
                }else{
                    Toast.makeText(scanqr.this,userid+" not exist",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
