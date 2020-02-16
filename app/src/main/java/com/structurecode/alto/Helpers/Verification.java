package com.structurecode.alto.Helpers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.structurecode.alto.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Guy on 6/2/2017.
 */

public class Verification {
    private static String emailCheckUrl="";
    private static String usernameCheckUrl="";
    public static final String emailCheckBroadcast="com.skilledhacker.developer.musiqx.email";
    public static final String usernameCheckBroadcast="com.skilledhacker.developer.musiqx.username";
    private static Context ctx;
    public static short isEmailFree=-1;
    public static short isUsernameFree=-1;

    public static short username_error=0;
    public static short email_error=0;

    public static String email_check(String email, Context ctx,boolean is_registration){
        String result="";
        email=email.trim();
        if (email==null || email.isEmpty()){
            result= ctx.getString(R.string.email_empty);
        }else if (!isEmailValid(email)){
            result= ctx.getString(R.string.email_invalid);
        }
        return result;
    }

    public static String name_check(String name,Context ctx){
        String result="";
        name=name.trim();
        if (name==null || name.isEmpty()){
            result= ctx.getString(R.string.fname_empty);
        }else if (name.length()<2){
            result= ctx.getString(R.string.fname_short);
        }else if (name.length()>50){
            result= ctx.getString(R.string.fname_long);
        }

        return result;
    }

    public static String password_check(String password,Context ctx){
        String result="";
        if (password==null || password.isEmpty()){
            result= ctx.getString(R.string.password_empty);
        }else if (password.length()>50){
            result= ctx.getString(R.string.password_long);
        }else if (password.length()<6){
            result= ctx.getString(R.string.password_short);
        }else if (!password_char_check(password)){
            result= ctx.getString(R.string.password_condition);
        }

        return result;
    }

    public static String cPassword_check(String password,String cpassword,Context ctx){
        String result="";
        if (!password.equals(cpassword)) result= ctx.getString(R.string.password_match);

        return result;
    }

    //PRIVATE FUNCTIONS

    private static boolean inArrayList(String tab[],String string){

        for(int i=0;i<tab.length;i++){
            if(tab[i].equals(string)){
                return true;
            }
        }

        return false;
    }

    private static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private static boolean password_char_check(String password){
        boolean majuscule = false;
        boolean miniscule = false;
        boolean carac_special = false;
        boolean number = false;
        char tab[] = password.toCharArray();

        for (int i = 0; i < password.length(); i++) {

            if (tab[i] <= '!' || tab[i] >= '~') {
                return false;
            }
            if (!majuscule) {
                if (tab[i] <= 'Z' && tab[i] >= 'A') {
                    majuscule = true;
                }
            }
            if (!miniscule) {
                if (tab[i] <= 'z' && tab[i] >= 'a') {
                    miniscule = true;
                }
            }

            if (!number) {
                if (Character.isDigit(tab[i])) {
                    number = true;
                }
            }

            if (!carac_special) {
                if (!Character.isDigit(tab[i])) {
                    if (!(tab[i] <= 'z' && tab[i] >= 'a')) {
                        if (!(tab[i] <= 'Z' && tab[i] >= 'A')) {
                            carac_special = true;
                        }
                    }
                }
            }

        }
        if(!miniscule || !majuscule || !number || !carac_special){
            return false;
        }

        return true;
    }
}
