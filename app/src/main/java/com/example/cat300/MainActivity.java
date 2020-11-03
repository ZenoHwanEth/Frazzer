package com.example.cat300;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mainNav;
    private FrameLayout mainFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contextOfApplication = getApplicationContext();

        mainFrame = (FrameLayout) findViewById(R.id.main_frame);
        mainNav = (BottomNavigationView) findViewById(R.id.main_nav);

        setFrag(new HomeFrag());

        mainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Fragment fragment = null;
                switch (menuItem.getItemId()){
                    case R.id.nav_home:
                        fragment = new HomeFrag();
                        break;
                    case R.id.nav_chat:
                        fragment = new ChatFrag();
                        break;
                    case R.id.nav_friend:
                        fragment = new FrenFrag();
                        break;
                    case R.id.nav_info:
                        fragment = new InfoFrag();
                        break;
                }

                setFrag(fragment);

                return true;

            };
        });

    }

    private void setFrag (Fragment frag){
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,frag).commit();
    }

    public static Context contextOfApplication;
    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }
}
