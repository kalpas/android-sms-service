package kalpas.testservice.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;

import com.google.gson.Gson;

public class CardDAO {

    private static final String CARDS_FILE_NAME = "card.json";
    private Gson                gson            = new Gson();


    public void save(Card card, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(getFileName(card.id), Context.MODE_PRIVATE);
            fos.write(gson.toJson(card).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Card load(String cardId, Context context){
        Card card = null;
        FileInputStream fis;
        try {
            fis = context.openFileInput(getFileName(cardId));
            card = gson.fromJson(new InputStreamReader(fis), Card.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return card;
    }


    private String getFileName(String cardId) {
        return cardId + CARDS_FILE_NAME;
    }

}
