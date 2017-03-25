package com.example.rachel.project3;

import java.io.File;

/**
 * Created by Rachel on 3/24/2017.
 */

public class Pet extends Object {

    private String name;
    private String pic;

    public Pet(String n, String p){
        name = n;
        pic = "http://www.tetonsoftware.com/pets/" + p;
    }

    public String getName(){
        return name;
    }

    public String getURL(){
        return pic;
    }

}
