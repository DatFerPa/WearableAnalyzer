package com.detectorcaidas.utils;

import android.content.Intent;

public class InfoApp {

    private static InfoApp instancia;
    private Intent intentService;
    private boolean caidaBool;
    private boolean MainActivity;

    private InfoApp(){}

    public static InfoApp getInstance(){
        if(instancia==null){
            instancia = new InfoApp();
        }
        return instancia;
    }




}
