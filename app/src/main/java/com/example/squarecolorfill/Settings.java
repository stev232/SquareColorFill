package com.example.squarecolorfill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Settings extends AppCompatActivity {
    private static final String FILE_NAME="GameData.txt";
    private int themeID, isColorBlind, isChallenge;

    public int getThemeID() { return this.themeID; }
    public void setThemeID(int themeID) { this.themeID = themeID; }
    public boolean getColorBlind() {
        if(isColorBlind == 0)
            return false;
        else
            return true;
    }
    public void setColorBlind(int isColorBlind) { this.isColorBlind = isColorBlind; }
    public void setIsChallenge(int isChallenge) { this.isChallenge = isChallenge; }
    public boolean getIsChallenge() {
        if(this.isChallenge == 1)
            return true;
        else
            return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //Removes the title bar from the app
        getSupportActionBar().hide(); //Hides the title bar from the app
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Sets the xml page to full screen removing the space that was being used for the title bar

        setContentView(R.layout.settings_menu);

        gameData('r');

        settingsMenu();
    }

    public void settingsMenu() {
        Colors color = new Colors();

        Switch challengeMode = (Switch) findViewById(R.id.challengeMode);

        FrameLayout[] palletFrame = {(FrameLayout) findViewById(R.id.frameStandard), (FrameLayout) findViewById(R.id.framePastel)};

        FrameLayout[] palletFrameBorder = {(FrameLayout) findViewById(R.id.frameBorderStandard), (FrameLayout) findViewById(R.id.frameBorderPastel)};

        ImageView[][] colorPallets = {{(ImageView) findViewById(R.id.standardOne), (ImageView) findViewById(R.id.standardTwo), (ImageView) findViewById(R.id.standardThree),
                (ImageView) findViewById(R.id.standardFour), (ImageView) findViewById(R.id.standardFive), (ImageView) findViewById(R.id.standardSix), (ImageView) findViewById(R.id.standardSeven),
                (ImageView) findViewById(R.id.standardEight), (ImageView) findViewById(R.id.standardNine), (ImageView) findViewById(R.id.standardTen), (ImageView) findViewById(R.id.standardEleven),
                (ImageView) findViewById(R.id.standardTwelve), (ImageView) findViewById(R.id.standardThirteen), (ImageView) findViewById(R.id.standardFourteen), (ImageView) findViewById(R.id.standardFifteen)},
                {(ImageView) findViewById(R.id.pastelOne), (ImageView) findViewById(R.id.pastelTwo), (ImageView) findViewById(R.id.pastelThree),
                        (ImageView) findViewById(R.id.pastelFour), (ImageView) findViewById(R.id.pastelFive), (ImageView) findViewById(R.id.pastelSix), (ImageView) findViewById(R.id.pastelSeven),
                        (ImageView) findViewById(R.id.pastelEight), (ImageView) findViewById(R.id.pastelNine), (ImageView) findViewById(R.id.pastelTen), (ImageView) findViewById(R.id.pastelEleven),
                        (ImageView) findViewById(R.id.pastelTwelve), (ImageView) findViewById(R.id.pastelThirteen), (ImageView) findViewById(R.id.pastelFourteen), (ImageView) findViewById(R.id.pastelFifteen)}};

        for(int x = 0; x<2; x++) {
            for(int y = 0; y<15; y++) {
                color.setThemeID(x);
                ImageViewCompat.setImageTintList(colorPallets[x][y], ColorStateList.valueOf(Color.parseColor(color.getColorCodeByID(y))));
            }
        }

        if(getIsChallenge()) {
            challengeMode.setText("Challenge Mode On");
            challengeMode.setChecked(true);
        } else {
            challengeMode.setText("Challenge Mode Off");
            challengeMode.setChecked(false);
        }
        palletFrameBorder[getThemeID()].setBackgroundColor(Color.parseColor("#0000FF"));

        palletFrame[0].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                palletFrameBorder[getThemeID()].setBackgroundColor(Color.parseColor("#7D7D7D"));
                setThemeID(0);
                palletFrameBorder[getThemeID()].setBackgroundColor(Color.parseColor("#0000FF"));
                gameData('w');
            }
        });

        palletFrame[1].setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                palletFrameBorder[getThemeID()].setBackgroundColor(Color.parseColor("#7D7D7D"));
                setThemeID(1);
                palletFrameBorder[getThemeID()].setBackgroundColor(Color.parseColor("#0000FF"));
                gameData('w');
            }
        });

        challengeMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    challengeMode.setText("Challenge Mode On");
                    setIsChallenge(1);
                } else {
                    challengeMode.setText("Challenge Mode Off");
                    setIsChallenge(0);
                }
                gameData('w');
            }
        });
    }

    //These buttons are onClick events set in the xml sheet
    public void mainMenu(View view) {
        Intent intent = new Intent(this, LandingPage.class);
        finish(); //This finishes the activity so that it is removed from the stack
        startActivity(intent);
    }

    public void returnToGame(View view) {
        finish();
    }

    //This method will either read or write from txt file that hold JSON object
    public void gameData(char action) {
        File file = new File(this.getFilesDir(), this.FILE_NAME);//set a variable for the file path and file name
        JSONObject obj = new JSONObject(); //initialize JSON object

        int adCounterMedium = 0, adCounterHard = 0, adCounterEasy = 0;

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

                    setThemeID(Integer.parseInt(splitAgain[1]));
                    adCounterEasy = Integer.parseInt(splitAgain[3]);
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
                obj.put("Pallet", getThemeID());
                obj.put("Easy", adCounterEasy);
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
}