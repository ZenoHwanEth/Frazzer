package com.example.cat300;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class qrcode extends AppCompatActivity {

    private ImageView qr;
    private FirebaseAuth firebaseAuth;
    String TAG="GenerateQrCode";
    Bitmap bitmap;
    QRGEncoder qrgEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        qr=findViewById(R.id.qr);

        firebaseAuth = FirebaseAuth.getInstance();

        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        Toast.makeText(qrcode.this,firebaseUser.getEmail(),Toast.LENGTH_SHORT).show();
        WindowManager manager=(WindowManager)getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point=new Point();
        display.getSize(point);
        int width=point.x;
        int height=point.y;
        int smallerdimension=width<height ? width:height;
        qrgEncoder=new QRGEncoder(firebaseUser.getEmail(),null, QRGContents.Type.TEXT,smallerdimension);
        try{
            bitmap=qrgEncoder.encodeAsBitmap();
            qr.setImageBitmap(bitmap);
        }catch(WriterException e){
            Log.v(TAG,e.toString());
        }
    }
}
