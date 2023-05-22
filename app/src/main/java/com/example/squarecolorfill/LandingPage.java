package com.example.squarecolorfill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ImageViewCompat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LandingPage extends AppCompatActivity {
    private static final String FILE_NAME="GameData.txt";
    private int counter, themeID, isColorBlind;
    private AdView mAdView;

    //This section has a series of get set and a boost methods
    public void setCounter(int count) { this.counter = count; }
    public void boostCounter() { this.counter++; }
    public int getCounter() { return this.counter; }
    public void setThemeID(int themeID) { this.themeID = themeID; }
    public int getThemeID() { return this.themeID; }
    public void setColorBlind(int isColorBlind) { this.isColorBlind = isColorBlind; }
    public boolean getColorBlind() {
        if(isColorBlind == 0)
            return false;
        else
            return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //Sets no title, to the best of my knowledge
        getSupportActionBar().hide(); //Hides the title bar, to the best of my knowledge
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Sets the xml page to full screen removing the space that was being used for the title bar

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.landing_page);

        gameData('r');

        homePage();
    }

    public void homePage() {
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
        color.setThemeID(getThemeID()); //This method call will set the app color theme

        //initialize the FrameLayout for 5 squares for the background of the landing page
        FrameLayout squares[] = { (FrameLayout) findViewById(R.id.squareOne), (FrameLayout) findViewById(R.id.squareTwo),
                (FrameLayout) findViewById(R.id.squareThree), (FrameLayout) findViewById(R.id.squareFour), (FrameLayout) findViewById(R.id.squareFive) };


        FrameLayout squareHolder[] = { (FrameLayout) findViewById(R.id.squareOneFrame), (FrameLayout) findViewById(R.id.squareTwoFrame),
                (FrameLayout) findViewById(R.id.squareThreeFrame), (FrameLayout) findViewById(R.id.squareFourFrame), (FrameLayout) findViewById(R.id.squareFiveFrame) };

        //This loop will take each frame layout view and set a pixel height to it that is related to screen size
        for(int x = 0; x < 5; x++) {
            squares[x].getLayoutParams().height = size.getHeightWidth();
            squares[x].getLayoutParams().width = size.getHeightWidth();
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
        for(int x = 0; x < 5; x++) {
            for(int y = 0; y < 5; y++) {
                if(y!=4) {
                    ImageViewCompat.setImageTintList(squaresImg[x][y], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(x)))); //Takes the earlier setImageTintList statement and sets it so that the image has color when the user plays the app
                }
            } //End nested loop for each triangle in the square
        } //End squares loop

        //ca-app-pub-3940256099942544/6300978111
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //declare handler and run event
        Handler delay = new Handler();
        Runnable background = null;
        delay.removeCallbacks(background);
        delay.removeCallbacksAndMessages(null);
        setCounter(0);//setCounter to start with the first square
        background = new Runnable() {
            @Override
            public void run() {
                squareHolder[getCounter()].animate().rotation(360).setStartDelay(30).setDuration(1000);//The counter will run through all available squares individually and rotate them clockwise
                boostCounter();//This method will boost the global counter variable
                //If the counter hits the final square then the counter will reset to 0
                if(getCounter() > 4) {
                    setCounter(0);
                }
                //This line will rotate the square counter clockwise
                squareHolder[getCounter()].animate().rotation(0).setStartDelay(30).setDuration(1000);
                delay.postDelayed(this, 1000); //set delay for the handler/run event
            }
        };
        //If the counter hits the final square then the counter will reset to 0
        if(getCounter() > 4) {
            setCounter(0);
        } else {
            delay.postDelayed(background, 1000);//set delay for the handler/run event
        }
    }

    protected void onPause() {
        super.onPause();
    }

    //This method is an onClick event from the landing_page.xml file and will start the GamePageSmall java file
    public void easy(View view) {
        Intent intent = new Intent(this, GamePageSmall.class);
        finish();
        startActivity(intent);
    }

    //This method is an onClick event from the landing_page.xml file and will start the GamePageMedium java file
    public void medium(View view) {
        Intent intent = new Intent(this, GamePageMedium.class);
        finish();
        startActivity(intent);
    }

    //This method is an onClick event from the landing_page.xml file and will start the GamePageLarge java file
    public void hard(View view) {
        Intent intent = new Intent(this, GamePageLarge.class);
        finish();
        startActivity(intent);
    }

    public void settings(View view) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    public void howToPlay(View view) {
        Intent intent = new Intent(this, HowToPlay.class);
        startActivity(intent);
    }

    public void onBackPressed() {
        Intent landingPage = new Intent(Intent.ACTION_MAIN);
        landingPage.addCategory(Intent.CATEGORY_HOME);
        landingPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(landingPage);
    }

    //This method will either read or write from txt file that hold JSON object
    public void gameData(char action) {
        File file = new File(this.getFilesDir(), this.FILE_NAME);//set a variable for the file path and file name
        JSONObject obj = new JSONObject(); //initialize JSON object

        int adCounterEasy = 0, adCounterMedium = 0, adCounterHard = 0, isChallenge = 0;

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
                    try {
                        setThemeID(Integer.parseInt(splitAgain[1]));
                        adCounterEasy = Integer.parseInt(splitAgain[3]);
                        adCounterMedium = Integer.parseInt(splitAgain[5]);
                        adCounterHard = Integer.parseInt(splitAgain[7]);
                        setColorBlind(Integer.parseInt(splitAgain[9]));
                        isChallenge = Integer.parseInt(splitAgain[11].substring(0,1));
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        this.isColorBlind = 0;
                        isChallenge = 0;
                        gameData('w');
                        gameData('r');
                    }
                    catch (NumberFormatException e) {
                        this.isColorBlind = 0;
                        isChallenge = 0;
                        gameData('w');
                        gameData('r');
                    }

                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if(action == 'w') {
            //When appending the txt file
            try {
                obj.put("Pallet", getThemeID());
                obj.put("Easy", adCounterEasy);
                obj.put("Medium", adCounterMedium);
                obj.put("Hard", adCounterHard);
                if(getColorBlind()) {
                    obj.put("ColorBlind", 1);
                } else {
                    obj.put("ColorBlind", 0);
                }
                obj.put("Challenge", isChallenge);
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
}