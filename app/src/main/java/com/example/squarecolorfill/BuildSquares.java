package com.example.squarecolorfill;

//This java file sets an int to each section of a square and only allow for a single integer to be used 4 times

public class BuildSquares {
    private int[][] squares;
    final private int colorAmountPerSquareMax = 4;
    private int numSquares, numColors, themeID;

    //This constructor takes the number of squares and determines how many colors will be needed and how many squares will need to be built
    public int[][] BuildSquares(int numSquares) {
        setNumSquares(numSquares);
        setNumColors(numSquares);
        buildSquares();
        return getSquares();
    }

    //This section has a series of get set methods
    public void setNumSquares(int numSquares) { this.numSquares = numSquares; }
    public int getNumSquares() { return this.numSquares; }

    public void setNumColors(int numSquares) { this.numColors = numSquares - 1; }
    public int getNumColors() { return this.numColors; }

    public void setSquares(int[][] squares){ this.squares = squares; }
    public int[][] getSquares() { return this.squares; }

    public void buildSquares() {
        Colors colors = new Colors(); //initialize the Colors class
        colors.Colors(getNumColors());
        int emptySquare = ((getNumSquares()-1)/2); //this int determines where the center most square is located in the game page so that it can be set to -1 and keep the square blank for the player to utilize
        int[][] squares = new int[getNumSquares()][this.colorAmountPerSquareMax]; //initialize the squares array with first index holding the number of squares and the second index holding the number of sides per square
        //This loop will determine which square is being populated
        for(int x = 0; x<getNumSquares(); x++) {
            //This nested loop will determine which section of the square is being populated
            for(int y = 0; y<this.colorAmountPerSquareMax; y++) {
                //This if statement checks if the current square is suppose to be the empty square
                if(x==emptySquare) {
                    squares[x][y] = -1;
                }
                //If it is not the empty square then populate the square with an int
                else {
                    squares[x][y] = colors.getColor();
                }
            }
        }
        //Take the two dimensional array that is built in this method and set them to the global square variable
        setSquares(squares);
    }
}
