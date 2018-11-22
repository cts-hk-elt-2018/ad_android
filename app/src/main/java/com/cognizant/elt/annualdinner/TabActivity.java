package com.cognizant.elt.annualdinner;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class TabActivity extends AppCompatActivity implements OneFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;
    private String token;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            mTextMessage.setVisibility(View.INVISIBLE);
            switch (item.getItemId()) {
                case R.id.navigation_checkin:
//                    mTextMessage.setText(R.string.title_home);
                    OneFragment onef = OneFragment.newInstance(token);
                    openFragment(onef);
                    return true;
                case R.id.navigation_game:
//                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_effect:
//                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
