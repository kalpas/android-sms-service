package kalpas.testservice.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;

import com.google.gson.Gson;

public class PersistentCard {

    private static final String CARDS_FILE_NAME = "card.json";
    private Gson                gson            = new Gson();
    private Card                card;

    @SuppressWarnings("unused")
    private PersistentCard() {
    }

    public PersistentCard(Card card) {
        this.card = card;
    }

    public void persist(Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(getFileName(), Context.MODE_PRIVATE);
            fos.write(gson.toJson(this.card).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(Context context){
        FileInputStream fis;
        try {
            fis = context.openFileInput(getFileName());
            card = gson.fromJson(new InputStreamReader(fis), Card.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Card getCard() {
        return card;
    }

    private String getFileName() {
        return card.id + CARDS_FILE_NAME;
    }

}
