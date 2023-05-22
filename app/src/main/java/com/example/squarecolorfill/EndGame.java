package com.example.squarecolorfill;

public class EndGame {
    private String[] victoryText = { "Amazing!", "Awesome!", "You Did It!", "Good Job!", "Spectacular!" };
    public String EndGame(boolean finished) {
        int x = 0;
        if(finished) {
            x = (int) (Math.random() * (this.victoryText.length - 1));
        }
        return victoryText[x];
    }
}
