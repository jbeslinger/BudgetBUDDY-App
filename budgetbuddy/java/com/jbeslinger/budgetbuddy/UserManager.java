package com.jbeslinger.budgetbuddy;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class UserManager {

    public static User currentUser = null;

    public static void createNewUser(Context context, String username) {
        try {
            String filepath = context.getFilesDir() + "\\" + username + ".dat";
            File file = new File(filepath);
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveUserData(Context context) {
        try {
            String filepath = context.getFilesDir() + "\\" + currentUser.username + ".dat";
            FileOutputStream file = new FileOutputStream(filepath);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(currentUser);
            out.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Returns 'true' or 'false' based on if a User was successfully loaded
    public static void loadUserData(Context context, String username) throws IOException, ClassNotFoundException {
        String filepath = context.getFilesDir() + "\\" + username + ".dat";
        FileInputStream file = null;
        file = new FileInputStream(filepath);

        //Deserialize the User object
        ObjectInputStream in = new ObjectInputStream(file);
        currentUser = (User)in.readObject();
        in.close();
        file.close();
    }

}
