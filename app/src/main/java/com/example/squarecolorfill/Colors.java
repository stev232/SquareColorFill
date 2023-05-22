package com.example.squarecolorfill;
/*
* colorCode[0][] = base color set
* colorCode[1][] = neutral color set
* colorCode[2][] = pastel color set
*/
public class Colors {
    private int numColors, themeID;
    private int[] counters = new int[15];
    //This two dimensional array holds hex codes for the color values that will be used in the squares
    final private String[][] colorCode = { { "#FF0000", "#0000FF", "#00FF00", "#FFFF00",
            "#FFA500", "#800080", "#FF00FF", "#008080", "#228B22",
            "#00FFFF", "#808080", "#964B00", "#800000", "#000080", "#000000" },
            { "#F48B94", "#507E65", "#FFD17C", "#A1EAF2",
            "#6C5B7B", "#355C7D", "#B2BEC3", "#BF6C85", "#DBEBC2",
            "#ADDCA9", "#F8B196", "#7FC7DB", "#FFF4E8", "#A8E9A5", "#CBE7DF" } };

    //This array will use symbols to replace colors for people who can not see color
    final private String[] colorSubstitutes = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "+", "=", "-", "*" };

    //This constructor sets the number of colors that will be used
    public void Colors(int numColors) {
        setNumColors(numColors);
        resetCounters();
    }

    //This method resets all counters for when a new game is started
    public void resetCounters() {
        for(int x = 0; getNumColors() < x-1; x++) {
            this.counters[x] = 0;
        }
    }

    //This section holds get set methods
    public void boostCounters(int x) { this.counters[x] = getCounters(x) + 1; }
    public int getCounters(int x) { return this.counters[x]; }

    public void setThemeID(int themeID) { this.themeID = themeID; }
    public int getThemeID() { return this.themeID; }

    public void setNumColors(int numColors) { this.numColors = numColors; }
    public int getNumColors() { return this.numColors; }

    //This section returns an int to the build squares file and will only return an int if that color is still available
    public int getColor() {
        int colorID = 0;
        boolean validColor = false;
        //if the color is not valid then the random number generator will run until a valid integer is selected to be sent to the Build Squares file
        while(!validColor) {
            colorID = (int) (Math.random() * getNumColors() - 0 + 1) -1;
            if(getCounters(colorID) < 4) {
                validColor = true;
                boostCounters(colorID);//When a colorID is chosen then the counter for that ID is boosted so that that id can only be used 4 times for each time the squares are built
            }
            else {
                validColor = false;
            }
        }
        return colorID; //return the valid colorID to the build squares file
    }

    //This method will return a symbol for each section of the squares
    public String getColorSubstitute(int colorID) { return this.colorSubstitutes[colorID]; }
    //This method will return a hex code based off of the theme that is set in this file and the colorID that is sent from the game page file
    public String getColorCodeByID(int colorID) { return this.colorCode[getThemeID()][colorID]; }
}
