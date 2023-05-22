package com.example.squarecolorfill;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ImageViewCompat;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GamePageSmall extends AppCompatActivity {
    private static final String FILE_NAME="GameData.txt";
    final private int small = 5, sides = 4;
    final private int[] image = { R.drawable.squaretop, R.drawable.bottom, R.drawable.left, R.drawable.right };
    private boolean gameFinished = false, isBackPressed = true;
    private boolean[] finished;
    private int active = -1, holder, adCounterEasy, pallet, isChallenge, isColorBlind, moveCount;
    private int[][] squares;
    private InterstitialAd mInterstitialAd;

    public void setSquares(int[][] squares) { this.squares = squares; }
    public void changeSquare(int x, int y, int value) { this.squares[x][y] = value; }
    public int getSquares(int x, int y) { return this.squares[x][y]; }
    public int getSmall() { return this.small; }
    public int getSides() { return this.sides; }
    public int getImage(int x) { return this.image[x]; }
    public int getActive() { return this.active; }
    public void setActive(int x) { this.active = x; }
    public void setHolder(int x) { this.holder = x; }
    public int getHolder() { return this.holder; }
    public void setFinished(int x, boolean finished) { this.finished[x] = finished; }
    public boolean getFinished(int x) { return this.finished[x]; }
    public void setGameFinished(boolean finished) { this.gameFinished = finished; }
    public boolean getGameFinished() { return this.gameFinished; }
    public void setAdCounterEasy(int adCounterEasy) { this.adCounterEasy = adCounterEasy; }
    public int getAdCounterEasy() { return this.adCounterEasy; }
    public void setColorBlind(int isColorBlind) { this.isColorBlind = isColorBlind; }
    public boolean getColorBlind() {
        if(isColorBlind == 0)
            return false;
        else
            return true;
    }
    public void setPallet(int pallet) { this.pallet = pallet; }
    public int getPallet() { return this.pallet; }
    public void setBackPressed(boolean isBackPressed) { this.isBackPressed = isBackPressed; }
    public boolean getIsPressed() { return this.isBackPressed; }
    public void setIsChallenge(int isChallenge) { this.isChallenge = isChallenge; }
    public boolean getIsChallenge() {
        if(isChallenge == 0)
            return false;
        else
            return true;
    }
    public void setMoveCount(int moveCount) { this.moveCount = moveCount; }
    public void boostMoveCount() { this.moveCount++; }
    public int getMoveCount() { return this.moveCount; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //Removes the title bar from the app
        getSupportActionBar().hide(); //Hides the title bar from the app
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Sets the xml page to full screen removing the space that was being used for the title bar

        BuildSquares build = new BuildSquares(); //call the BuildSquares class
        setSquares(build.BuildSquares(getSmall())); //This method builds the squares and sets an int that will be used as an index in the Colors class for setting a color in each triangle of each square

        this.finished = new boolean[getSmall()]; //Declare size of the finished boolean array
        setContentView(R.layout.game_page_easy); //Declare the xml sheet for this java class
        victoryScreen(false); //Set the victory screen to false

        for(int x = 0; x < getSmall(); x++) {
            setFinished(x, false);
        } //Set all elements in the finished boolean array to false

        gameData('r'); //call the gameData method which will find saved app information
        setMoveCount(0);

        //This selection structure will determine if an ad is to be run before gameplay
        //If the ad is run then the play counter is reset and the player can play 6 more times before being forced to watch view another full screen ad
        if(getAdCounterEasy() > 6) {
            runAd();
        }

        game(); //This method holds all the gameplay
    }

    public void game() {
        DisplayMetrics displayMetrics = new DisplayMetrics(); //Call the display metrics class
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels; //get the screen width in pixels from the display metrics class
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics); //On some devices the space that the back, home, and task buttons are held in are subtracted from the screen height and width
            //calling getRealMetrics will add that space back when grabbing screen width/height in pixels
            int realWidth = displayMetrics.widthPixels;
            if(width<realWidth)
                width = realWidth;
        }

        ScaleSetter size = new ScaleSetter(width); //This class will make the game elements more dynamic based off of screen size

        Colors color = new Colors(); //This class sets the color information for each square
        color.setThemeID(getPallet()); //This method call will set the app color theme

        //These custom animations are for when color is being swapped between squares to give the user better "feedback" that they are actually doing something
        Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        ConstraintLayout background = (ConstraintLayout) findViewById(R.id.gamePageBackground); //Put the background into a variable
        background.setBackgroundColor(Color.parseColor(color.getColorCodeByID(14))); //Set the background color

        TextView moveCounter = (TextView)findViewById(R.id.moveCounter);
        if(getIsChallenge()) {
            moveCounter.setVisibility(View.VISIBLE);
            moveCounter.setText(String.valueOf(getMoveCount()));

            if(getPallet() == 0) {
                moveCounter.setTextColor(Color.parseColor("#FFFFFF"));
            } else if(getPallet() == 1) {
                moveCounter.setTextColor(Color.parseColor("#000000"));
            }//This selection structure determines what color the text will be
        } else {
            moveCounter.setVisibility(View.INVISIBLE);
        }//This selection determines if the move counter is viable


        //This is the bottom frame of the squares that will be colored black, red or blue depending on the user interaction with the squares
        FrameLayout[] square = { (FrameLayout)findViewById(R.id.squareOneFrame), (FrameLayout)findViewById(R.id.squareTwoFrame),
                (FrameLayout)findViewById(R.id.squareThreeFrame), (FrameLayout)findViewById(R.id.squareFourFrame), (FrameLayout)findViewById(R.id.squareFiveFrame) };

        //This is the frame that will be directly holding the squares
        FrameLayout[] squareHolder = { (FrameLayout)findViewById(R.id.squareOne), (FrameLayout)findViewById(R.id.squareTwo),
                (FrameLayout)findViewById(R.id.squareThree), (FrameLayout)findViewById(R.id.squareFour), (FrameLayout)findViewById(R.id.squareFive) };

        //This loop will take each frame layout view and set a pixel height to it that is related to screen size
        for(int x = 0; x < getSmall(); x++) {
            square[x].getLayoutParams().height = size.getHeightWidth();
            square[x].getLayoutParams().width = size.getHeightWidth();
            squareHolder[x].getLayoutParams().height = size.getHeightWidth()-10;
            squareHolder[x].getLayoutParams().width = size.getHeightWidth()-10;
        }

        //This two dimensional array is sorted in that the first index is the square and the second index is the specific triangle in the square
        ImageView[][] squaresImg = {
                { (ImageView) findViewById(R.id.squareOneTop), (ImageView) findViewById(R.id.squareOneBottom),
                        (ImageView) findViewById(R.id.squareOneLeft), (ImageView) findViewById(R.id.squareOneRight) },
                { (ImageView) findViewById(R.id.squareTwoTop), (ImageView) findViewById(R.id.squareTwoBottom),
                        (ImageView) findViewById(R.id.squareTwoLeft), (ImageView) findViewById(R.id.squareTwoRight) },
                { (ImageView) findViewById(R.id.squareThreeTop), (ImageView) findViewById(R.id.squareThreeBottom),
                        (ImageView) findViewById(R.id.squareThreeLeft), (ImageView) findViewById(R.id.squareThreeRight) },
                { (ImageView) findViewById(R.id.squareFourTop), (ImageView) findViewById(R.id.squareFourBottom),
                        (ImageView) findViewById(R.id.squareFourLeft), (ImageView) findViewById(R.id.squareFourRight) },
                { (ImageView) findViewById(R.id.squareFiveTop), (ImageView) findViewById(R.id.squareFiveBottom),
                        (ImageView) findViewById(R.id.squareFiveLeft), (ImageView) findViewById(R.id.squareFiveRight) } };

        //This loop takes the int that is stored for each triangle and sets a color in each triangle that is in the square by calling the Colors class
        for(int x = 0; x < getSmall(); x++) {
            for(int y = 0; y < getSides(); y++) {
                if(y!=4) {
                    if (getSquares(x, y) != -1) {
                        ImageViewCompat.setImageTintList(squaresImg[x][y], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(x, y)))));
                    } //If the int is a -1 then the square is to be left blank
                    squaresImg[x][y].setImageResource(getImage(y)); //Takes the earlier setImageTintList statement and sets it so that the image has color when the user plays the app
                }
            } //End nested loop for each triangle in the square
        } //End squares loop

        /*
        * This documentation block is to cover all of the setOnTouchListener calls
        -------------------------------------------------------------------------------------------------------------------------------------------------
        * For each square there are 5 methods                                                                                                           *
        * onClick, onSwipeUp, onSwipeDown, onSwipeLeft, onSwipeRight                                                                                    *
        -------------------------------------------------------------------------------------------------------------------------------------------------
        * onClick will either rotate the square or run an animation for the square to spin 360 degrees                                                  *
        * the square will rotate 90 degrees when selected                                                                                               *
        * if the square is filled with one color the bottom frame will show blue except for square 2                                                    *
        -------------------------------------------------------------------------------------------------------------------------------------------------
        * onSwipeUp will use the selected square and determine if it is a valid square for the onSwipeUp method                                         *
        * Valid square for swiping up are not in the top row                                                                                            *
        * Invalid squares are at indexes 0, 1, 3                                                                                                        *
        * When the onSwipeUp method is called the top triangle of the active square will be swapped with the bottom triangle of the square above        *
        -------------------------------------------------------------------------------------------------------------------------------------------------
        * onSwipeDown will use the selected square and determine if it is a valid square for the onSwipeDown method                                     *
        * Valid square for swiping down are not in the bottom row                                                                                       *
        * Invalid squares are at indexes 1, 3, 4                                                                                                        *
        * When the onSwipeDown method is called the bottom triangle of the active square will be swapped with the top triangle of the square below      *
        -------------------------------------------------------------------------------------------------------------------------------------------------
        * onSwipeLeft will use the selected square and determine if it is a valid square for the onSwipeLeft method                                     *
        * Valid square for swiping left are not in the left column                                                                                      *
        * Invalid squares are at indexes 0, 1, 4                                                                                                        *
        * When the onSwipeLeft method is called the left triangle of the active sqr will be swapped with the right triangle of the square to the left   *
        -------------------------------------------------------------------------------------------------------------------------------------------------
        * onSwipeRight will use the selected square and determine if it is a valid square for the onSwipeRight method                                   *
        * Valid square for swiping right are not in the right column                                                                                    *
        * Invalid squares are at indexes 0, 3, 4                                                                                                        *
        * When the onSwipeRight method is called the right triangle of the active sqr will be swapped with the left triangle of the square to the right *
        -------------------------------------------------------------------------------------------------------------------------------------------------
        * Every time there is a swipe action the game will check to see if the square is finished
        * If all squares are finished then the game will end with a victory screen
        * End of setOnTouchListener documentation block
        */
        squaresImg[0][0].setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onClick() {
                // your on click here
                super.onClick();
                if (!getFinished(0)) {
                    setActive(0);
                    rotate();
                    boostMoveCount();
                    moveCounter.setText(String.valueOf(getMoveCount()));
                    for(int x = 0; x<getSides(); x++) {
                        if (getSquares(getActive(), x) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), x)))));
                        } else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], null);
                        }
                    }
                } else {
                    square[0].animate().rotation(360).setStartDelay(30).setDuration(1000);
                }
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                setActive(0);

                if(getActive() != 0 && getActive() != 1 && getActive() != 3) {
                    if(getIsChallenge() && getSquares(getActive(), 0) == -1 || getIsChallenge() && getSquares(getActive()-2, 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }

                    }
                }
            }

            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                setActive(0);

                if(getActive() != 1 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive()+2, 0) == -1 || getIsChallenge() && getSquares(getActive(), 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if (!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                setActive(0);

                if(getActive() != 0 && getActive() != 1 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 3) == -1 || getIsChallenge() && getSquares(getActive()-1, 2) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                setActive(0);

                if(getActive() != 0 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 2) == -1 || getIsChallenge() && getSquares(getActive()+1, 3) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }
        });

        squaresImg[1][0].setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onClick() {
                // your on click here
                super.onClick();
                if (!getFinished(1)) {
                    setActive(1);
                    rotate();
                    boostMoveCount();
                    moveCounter.setText(String.valueOf(getMoveCount()));
                    for(int x = 0; x<getSides(); x++) {
                        if (getSquares(getActive(), x) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), x)))));
                        } else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], null);
                        }
                    }
                } else {
                    square[1].animate().rotation(360).setStartDelay(30).setDuration(1000);
                }
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                setActive(1);

                if(getActive() != 0 && getActive() != 1 && getActive() != 3) {
                    if(getIsChallenge() == true && getSquares(getActive(), 0) == -1 || getSquares(getActive()-2, 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }

                    }
                }
            }

            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                setActive(1);

                if(getActive() != 1 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive()+2, 0) == -1 || getIsChallenge() && getSquares(getActive(), 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if (!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                setActive(1);

                if(getActive() != 0 && getActive() != 1 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 3) == -1 || getIsChallenge() && getSquares(getActive()-1, 2) == -1) {
                        boostMoveCount();
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                setActive(1);

                if(getActive() != 0 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 2) == -1 || getIsChallenge() && getSquares(getActive()+1, 3) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }
        });

        squaresImg[2][0].setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onClick() {
                // your on click here
                super.onClick();
                if (!getFinished(2)) {
                    setActive(2);
                    rotate();
                    boostMoveCount();
                    moveCounter.setText(String.valueOf(getMoveCount()));
                    for(int x = 0; x<getSides(); x++) {
                        if (getSquares(getActive(), x) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), x)))));
                        } else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], null);
                        }
                    }
                } else {
                    square[2].animate().rotation(360).setStartDelay(30).setDuration(1000);
                }
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                setActive(2);

                if(getActive() != 0 && getActive() != 1 && getActive() != 3) {
                    if(getIsChallenge() && getSquares(getActive(), 0) == -1 || getIsChallenge() && getSquares(getActive()-2, 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }

                    }
                }
            }

            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                setActive(2);

                if(getActive() != 1 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive()+2, 0) == -1 || getIsChallenge() && getSquares(getActive(), 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if (!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                setActive(2);

                if(getActive() != 0 && getActive() != 1 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 3) == -1 || getIsChallenge() && getSquares(getActive()-1, 2) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                setActive(2);

                if(getActive() != 0 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 2) == -1 || getIsChallenge() && getSquares(getActive()+1, 3) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }
        });

        squaresImg[3][0].setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onClick() {
                // your on click here
                super.onClick();
                if(!getFinished(3)) {
                    setActive(3);
                    rotate();
                    boostMoveCount();
                    moveCounter.setText(String.valueOf(getMoveCount()));
                    for(int x = 0; x<getSides(); x++) {
                        if (getSquares(getActive(), x) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), x)))));
                        } else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], null);
                        }
                    }
                } else {
                    square[3].animate().rotation(360).setStartDelay(30).setDuration(1000);
                }
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                setActive(3);

                if(getActive() != 0 && getActive() != 1 && getActive() != 3) {
                    if(getIsChallenge() == true && getSquares(getActive(), 0) == -1 || getSquares(getActive()-2, 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }

                    }
                }
            }

            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                setActive(3);

                if(getActive() != 1 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive()+2, 0) == -1 || getIsChallenge() && getSquares(getActive(), 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if (!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                setActive(3);

                if(getActive() != 0 && getActive() != 1 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 3) == -1 || getIsChallenge() && getSquares(getActive()-1, 2) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                setActive(3);

                if(getActive() != 0 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 2) == -1 || getIsChallenge() && getSquares(getActive()+1, 3) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }
        });

        squaresImg[4][0].setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onClick() {
                // your on click here
                super.onClick();
                if(!getFinished(4)) {
                    setActive(4);
                    rotate();
                    boostMoveCount();
                    moveCounter.setText(String.valueOf(getMoveCount()));
                    for(int x = 0; x<getSides(); x++) {
                        if (getSquares(getActive(), x) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), x)))));
                        } else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][x], null);
                        }
                    }
                } else {
                    square[4].animate().rotation(360).setStartDelay(30).setDuration(1000);
                }
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                setActive(4);

                if(getActive() != 0 && getActive() != 1 && getActive() != 3) {
                    if(getIsChallenge() && getSquares(getActive(), 0) == -1 || getIsChallenge() && getSquares(getActive()-2, 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 0));
                        changeSquare(getActive(), 0, getSquares(getActive()-2, 1));
                        changeSquare(getActive()-2, 1, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 0) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][0], null);
                        }
                        if(getSquares(getActive()-2, 1) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-2, 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-2][1], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][0].clearAnimation();
                        squaresImg[getActive()][0].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-2][1].clearAnimation();
                        squaresImg[getActive()-2][1].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                setActive(4);

                if(getActive() != 1 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive()+2, 0) == -1 || getIsChallenge() && getSquares(getActive(), 1) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if (!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 1));
                        changeSquare(getActive(), 1, getSquares(getActive()+2, 0));
                        changeSquare(getActive()+2, 0, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 1) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 1)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][1], null);
                        }
                        if(getSquares(getActive()+2, 0) != -1) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+2, 0)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+2][0], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][1].clearAnimation();
                        squaresImg[getActive()][1].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+2][0].clearAnimation();
                        squaresImg[getActive()+2][0].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                setActive(4);

                if(getActive() != 0 && getActive() != 1 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 3) == -1 || getIsChallenge() && getSquares(getActive()-1, 2) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 3));
                        changeSquare(getActive(), 3, getSquares(getActive()-1, 2));
                        changeSquare(getActive()-1, 2, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][3], null);
                        }
                        if(getSquares(getActive()-1, 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()-1, 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()-1][2], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][3].clearAnimation();
                        squaresImg[getActive()][3].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()-1][2].clearAnimation();
                        squaresImg[getActive()-1][2].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                setActive(4);

                if(getActive() != 0 && getActive() != 3 && getActive() != 4) {
                    if(getIsChallenge() && getSquares(getActive(), 2) == -1 || getIsChallenge() && getSquares(getActive()+1, 3) == -1) {
                        boostMoveCount();
                        moveCounter.setText(String.valueOf(getMoveCount()));
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    } else if(!getIsChallenge()) {
                        setHolder(getSquares(getActive(), 2));
                        changeSquare(getActive(), 2, getSquares(getActive()+1, 3));
                        changeSquare(getActive()+1, 3, getHolder());

                        animFadeOut.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeOut);
                        animFadeOut.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeOut);

                        if(getSquares(getActive(), 2) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive(), 2)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()][2], null);
                        }
                        if(getSquares(getActive()+1, 3) != -1 ) {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(getSquares(getActive()+1, 3)))));
                        }
                        else {
                            ImageViewCompat.setImageTintList(squaresImg[getActive()+1][3], null);
                        }

                        animFadeIn.reset();
                        squaresImg[getActive()][2].clearAnimation();
                        squaresImg[getActive()][2].startAnimation(animFadeIn);
                        animFadeIn.reset();
                        squaresImg[getActive()+1][3].clearAnimation();
                        squaresImg[getActive()+1][3].startAnimation(animFadeIn);

                        testGameFinish();
                        for(int x = 0; x < getSmall(); x++) {
                            if (getFinished(x)) {
                                square[x].setBackgroundColor(Color.parseColor("#0000FF"));
                            } else {
                                square[x].setBackgroundColor(Color.parseColor("#000000"));
                            }
                        }
                        if(getGameFinished()) {
                            victoryScreen(true);
                        }
                    }
                }
            }
        });
    }

    //This method is called when the already active square is clicked again
    //Called in the onClick event
    public void rotate() {
        setHolder(getSquares(getActive(), 2));
        changeSquare(getActive(), 2, getSquares(getActive(), 0));
        changeSquare(getActive(), 0, getSquares(getActive(), 3));
        changeSquare(getActive(), 3, getSquares(getActive(), 1));
        changeSquare(getActive(), 1, getHolder());
    }

    //This method runs through all the squares
    //check that the int value is the same for all triangles inside of each square to determine if the square is individual square is finished (true)
    //If all squares are marked finished then the game is marked as complete (true)
    public void testGameFinish() {
        int counter = 0, finishedSquareCount = 0;
        boolean squareFinished = false;
        for(int x = 0; x < getSmall(); x++) {
            for(int y = 1; y < getSides(); y++) {
                if(getSquares(x, y)==-1) {
                    squareFinished = false;
                    break;
                }
                if(((getSmall()-1)/2)>x && getSquares(x, y) == getSquares(x, 0) && ((getSmall()-1)/2) != x) { //Before the middle square
                    counter++;
                    if(counter == 3) {
                        squareFinished = true;
                    } else {
                        squareFinished = false;
                    }
                }
                else if(((getSmall()-1)/2) == x) { //This is the middle square
                    squareFinished = false;
                }
                else if(x>((getSmall()-1)/2) && getSquares(x, y) == getSquares(x, 0) && ((getSmall()-1)/2) != x) { //After the middle square
                    counter++;
                    if (counter == 3) {
                        squareFinished = true;
                    } else {
                        squareFinished = false;
                    }
                } else {
                    break;
                }
            }
            counter = 0;
            setFinished(x, squareFinished);
            if(squareFinished) {
                squareFinished = false;
                finishedSquareCount++;
            }
        }
        //The middle square does not need to be finished so the number of squares involved -1 is what needs to be tested
        if(finishedSquareCount == getSmall() -1) {
            setGameFinished(true);
        }
    }

    //This method will display the victory screen on the return of true in the finished field
    public void victoryScreen(boolean finished) {
        EndGame endGame;
        TextView victoryText = (TextView) findViewById(R.id.victoryText);
        ConstraintLayout victoryScreen = (ConstraintLayout) findViewById(R.id.victoryScreen);
        final Button playAgain = findViewById(R.id.playAgain), mainMenu = findViewById(R.id.mainMenu);

        if(finished) {
            endGame = new EndGame();
            victoryText.setText(endGame.EndGame(true));
            victoryScreen.setVisibility(View.VISIBLE);
            victoryText.setVisibility(View.VISIBLE);
            playAgain.setVisibility(View.VISIBLE);
            playAgain.setClickable(true);
            mainMenu.setVisibility(View.VISIBLE);
            mainMenu.setClickable(true);
            setAdCounterEasy(getAdCounterEasy()+1);
            gameData('w'); //This will right data to the gameData file to save the number of plays the user has gone through
        } else {
            victoryScreen.setVisibility(View.INVISIBLE);
            victoryText.setVisibility(View.INVISIBLE);
            playAgain.setVisibility(View.INVISIBLE);
            playAgain.setClickable(false);
            mainMenu.setVisibility(View.INVISIBLE);
            mainMenu.setClickable(false);
        }
    }

    //This will display the victory screen as a menu screen
    public void onBackPressed() {
        TextView victoryText = (TextView) findViewById(R.id.victoryText);
        ConstraintLayout victoryScreen = (ConstraintLayout) findViewById(R.id.victoryScreen);
        final Button playAgain = findViewById(R.id.playAgain), mainMenu = findViewById(R.id.mainMenu), settings = findViewById(R.id.settings);

        if (getGameFinished()) {
            Intent intent = new Intent(this, LandingPage.class);
            finish(); //This finishes the activity so that it is removed from the stack
            startActivity(intent);
        }

        if(getIsPressed()) {
            victoryText.setText("Square Color Fill!");
            victoryScreen.setVisibility(View.VISIBLE);
            victoryText.setVisibility(View.VISIBLE);
            playAgain.setVisibility(View.VISIBLE);
            playAgain.setClickable(true);
            mainMenu.setVisibility(View.VISIBLE);
            mainMenu.setClickable(true);
            settings.setVisibility(View.VISIBLE);
            settings.setClickable(true);
            setBackPressed(false);
        } else {
            victoryText.setText("Square Color Fill!");
            victoryScreen.setVisibility(View.INVISIBLE);
            victoryText.setVisibility(View.INVISIBLE);
            playAgain.setVisibility(View.INVISIBLE);
            playAgain.setClickable(false);
            mainMenu.setVisibility(View.INVISIBLE);
            mainMenu.setClickable(false);
            settings.setVisibility(View.INVISIBLE);
            settings.setClickable(false);
            setBackPressed(true);
        }

        int oldTheme;
        oldTheme = getPallet();
        gameData('r');
        if(oldTheme != getPallet()) {
            game();
        }
    }

    //This buttons onClick event is set in the xml sheet
    public void playAgain(View view) {
        Intent intent = new Intent(this, GamePageSmall.class);
        finish(); //This finishes the activity so that it is removed from the stack
        startActivity(intent);
    }

    //This buttons onClick event is set in the xml sheet
    public void mainMenu(View view) {
        Intent intent = new Intent(this, LandingPage.class);
        finish(); //This finishes the activity so that it is removed from the stack
        startActivity(intent);
    }

    public void settings(View view) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    protected void onDestroy() {
        super.onDestroy();
        gameData('w'); //This will right data to the gameData file to save the number of plays the user has gone through
    }

    //This method will either read or write from txt file that hold JSON object
    public void gameData(char action) {
        File file = new File(this.getFilesDir(), this.FILE_NAME);//set a variable for the file path and file name
        JSONObject obj = new JSONObject(); //initialize JSON object

        int adCounterMedium = 0, adCounterHard = 0;

        FileReader fileReader = null;
        FileWriter fileWriter = null;
        BufferedReader bufferedReader = null;
        StringBuffer sb = new StringBuffer();
        BufferedWriter bufferedWriter = null;

        String response = null;

        if(action == 'r') {
            //check to see if the required file exists
            if (!file.exists()) {
                /*
                 * This try and catch block sets the default values for a JSON object
                 * Default values are 0 and false
                 */
                try {
                    obj.put("Pallet", 0);
                    obj.put("Easy", 0);
                    obj.put("Medium", 0);
                    obj.put("Hard", 0);
                    obj.put("ColorBlind", 0);
                    obj.put("Challenge", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //This try block will create the file for the JSON object to be saved into
                try {
                    file.createNewFile();
                    fileWriter = new FileWriter(file.getAbsoluteFile());
                    bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(String.valueOf(obj));
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    //These three lines allow for java to open a txt file and read the JSON object in the txt
                    StringBuffer output = new StringBuffer();
                    String holder = null;
                    fileReader = new FileReader(file.getAbsolutePath());
                    bufferedReader = new BufferedReader(fileReader);

                    String line = "";

                    while ((line = bufferedReader.readLine()) != null) {
                        output.append(line); //add a delimiter character to separate items in the object
                    }

                    response = output.toString();

                    String[] splitter = response.split(":");//Create an array that splits each element in the string by the : character

                    for(int x = 0; x < splitter.length; x++) {
                        sb.append(splitter[x] + ",");//Rebuild the entire String from the String array using a , as the delimiter character
                    }

                    holder = sb.toString();

                    String[] splitAgain = holder.split(",");//with each piece being separated by the "," delimiter character the String can be split into a

                    setPallet(Integer.parseInt(splitAgain[1]));
                    setAdCounterEasy(Integer.parseInt(splitAgain[3]));
                    adCounterMedium = Integer.parseInt(splitAgain[5]);
                    adCounterHard = Integer.parseInt(splitAgain[7]);
                    setColorBlind(Integer.parseInt(splitAgain[9]));
                    setIsChallenge(Integer.parseInt(splitAgain[11].substring(0,1)));

                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if(action == 'w') {
            //When appending the txt file
            try {
                obj.put("Pallet", getPallet());
                obj.put("Easy", getAdCounterEasy());
                obj.put("Medium", adCounterMedium);
                obj.put("Hard", adCounterHard);
                if(getColorBlind()) {
                    obj.put("ColorBlind", 1);
                } else {
                    obj.put("ColorBlind", 0);
                }
                if(getIsChallenge()) {
                    obj.put("Challenge", 1);
                } else {
                    obj.put("Challenge", 0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                file.createNewFile();
                fileWriter = new FileWriter(file.getAbsoluteFile());
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(String.valueOf(obj));
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void runAd() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {

                }
        });

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
            new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");

                if (mInterstitialAd != null) {
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                        @Override
                        public void onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d(TAG, "Ad was clicked.");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d(TAG, "Ad dismissed fullscreen content.");
                            mInterstitialAd = null;
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            // Called when ad fails to show.
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            mInterstitialAd = null;
                        }

                        @Override
                        public void onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d(TAG, "Ad recorded an impression.");
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d(TAG, "Ad showed fullscreen content.");
                        }
                    });

                    mInterstitialAd.show(GamePageSmall.this);

                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }

                setAdCounterEasy(0);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.d(TAG, loadAdError.toString());
                mInterstitialAd = null;

                setAdCounterEasy(getAdCounterEasy());
            }
        });
    }
}
