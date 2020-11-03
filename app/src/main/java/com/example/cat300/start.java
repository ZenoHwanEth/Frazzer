package com.example.cat300;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class start extends AppCompatActivity {

    private Button login;
    private TextView register;
    private EditText email,password;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private static final int REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        while(!verifypermission()){
            verifypermission();
        }

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser !=null){
            Intent intent = new Intent(start.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(start.this);
        email = (EditText)findViewById(R.id.phone);
        password = (EditText)findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        register = (TextView) findViewById(R.id.register1);
        SpannableString content = new SpannableString("sign up now!");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        register.setText(content);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String _email = email.getText().toString().trim();
                String _password = password.getText().toString().trim();

                if (TextUtils.isEmpty(_email)){
                    Toast.makeText(start.this,"Please enter email",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(_password)){
                    Toast.makeText(start.this,"Please enter password",Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.setMessage("Login...");
                progressDialog.show();

                firebaseAuth.signInWithEmailAndPassword(_email,_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Intent intent = new Intent(start.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                            Toast.makeText(start.this,"Please enter right info",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(start.this,register.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    private boolean verifypermission(){
        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),permission[0])== PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),permission[1])==PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),permission[2])==PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            ActivityCompat.requestPermissions(start.this,permission,REQUEST_CODE);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifypermission();
    }
}
