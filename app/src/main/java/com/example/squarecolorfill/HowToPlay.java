package com.example.squarecolorfill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;

public class HowToPlay extends AppCompatActivity {
    public int page = 1;

    public void setPage(int pageNum) { this.page = pageNum; }
    public int getPage() { return this.page; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //Removes the title bar from the app
        getSupportActionBar().hide(); //Hides the title bar from the app
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Sets the xml page to full screen removing the space that was being used for the title bar

        pageSelector();
    }

    public void pageOne(View v) {
        setPage(1);
        pageSelector();
    }

    public void pageTwo(View v) {
        setPage(2);
        pageSelector();
    }

    public void pageThree(View v) {
        setPage(3);
        pageSelector();
    }

    public void pageSelector() {
        switch(getPage()) {
            case 2:
                howToPlayTwo();
                break;
            case 3:
                howToPlayThree();
                break;
            default:
                howToPlay();
                break;
        }

        ConstraintLayout foreGround = (ConstraintLayout) findViewById(R.id.htpForeground);

        foreGround.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                if(getPage() < 3) {
                    setPage(getPage()+1);
                } else {
                    setPage(1);
                }
                pageSelector();
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                if(getPage() > 1) {
                    setPage(getPage()-1);
                    pageSelector();
                }
            }
        });
    }

    public void howToPlay() {
        setContentView(R.layout.how_to_play);
    }

    public void howToPlayTwo() {
        setContentView(R.layout.how_to_play_2);
    }

    public void howToPlayThree() {
        setContentView(R.layout.how_to_play_3);
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, LandingPage.class);
        finish(); //This finishes the activity so that it is removed from the stack
        startActivity(intent);
    }

    //These buttons are onClick events set in the xml sheet
    public void mainMenu(View view) {
        Intent intent = new Intent(this, LandingPage.class);
        finish(); //This finishes the activity so that it is removed from the stack
        startActivity(intent);
    }
}