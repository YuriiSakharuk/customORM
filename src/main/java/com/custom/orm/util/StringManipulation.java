package com.custom.orm.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringManipulation {

    /*
     * This method takes a String that starts with a lowercase letter and returns it with an uppercase letter.
     * */
    public String firstLetterStringToUpperCase(String fieldName) {
        if(fieldName == null || fieldName.isEmpty()) return "";
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
