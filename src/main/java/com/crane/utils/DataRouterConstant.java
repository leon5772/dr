package com.crane.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataRouterConstant {

    public static final String NEURO_API = "/cube/api";

    public static final String TAG_MALE = "Male";
    public static final String TAG_FEMALE = "Female";
    public static final String TAG_LONG_HAIR = "Long_Hair";
    public static final String TAG_SHORT_HAIR = "Short_Hair";
    public static final String TAG_HAT = "Hat";
    public static final String TAG_NO_HAT = "No_Hat";
    public static final String TAG_BAG = "Bag";
    public static final String TAG_NO_BAG = "No_Bag";
    public static final String TAG_LONG_SLEEVE = "Long_Sleeve";
    public static final String TAG_SHORT_SLEEVE = "Short_Sleeve";
    public static final String TAG_SLEEVELESS = "Sleeveless";
    public static final String TAG_RED_CLOTHES = "Red_Clothes";
    public static final String TAG_GREEN_CLOTHES = "Green_Clothes";
    public static final String TAG_BLUE_CLOTHES = "Blue_Clothes";
    public static final String TAG_YELLOW_CLOTHES = "Yellow_Clothes";
    public static final String TAG_BLACK_CLOTHES = "Black_Clothes";
    public static final String TAG_WHITE_CLOTHES = "White_Clothes";
    public static final String TAG_GREY_CLOTHES = "Grey_Clothes";
    public static final String TAG_PINK_CLOTHES = "Pink_Clothes";
    public static final String TAG_LONG_PANTS = "Long_Pants";
    public static final String TAG_SHORT_PANTS = "Short_Pants";
    public static final String TAG_RED_PANTS = "Red_Pants";
    public static final String TAG_GREEN_PANTS = "Green_Pants";
    public static final String TAG_BLUE_PANTS = "Blue_Pants";
    public static final String TAG_YELLOW_PANTS = "Yellow_Pants";
    public static final String TAG_BLACK_PANTS = "Black_Pants";
    public static final String TAG_WHITE_PANTS = "White_Pants";
    public static final String TAG_GREY_PANTS = "Grey_Pants";
    public static final String TAG_PINK_PANTS = "Pink_Pants";
    public static final String TAG_FIGHTING = "Fighting";
    public static final String TAG_RUNNING = "Running";

    public static final String[] TAG_SET = {"Male", "Female", "Long_Hair", "Short_Hair", "Hat", "No_Hat", "Bag", "No_Bag", "Long_Sleeve", "Short_Sleeve", "Sleeveless", "Red_Clothes", "Green_Clothes", "Blue_Clothes", "Yellow_Clothes", "Black_Clothes", "White_Clothes", "Grey_Clothes", "Pink_Clothes", "Long_Pants", "Short_Pants", "Red_Pants", "Green_Pants", "Blue_Pants", "Yellow_Pants", "Black_Pants", "White_Pants", "Grey_Pants", "Pink_Pants", "Fighting", "Running"};

    public static List<String> TAG_LIST = Arrays.asList(TAG_SET);

    public static final Set<Integer> HAIR_STYLE_SHORT = new HashSet<>(Arrays.asList(2, 3, 4, 5, 6, 11, 13));
    public static final Set<Integer> HAIR_STYLE_LONG = new HashSet<>(Arrays.asList(8, 9, 10, 12, 14));


    public static final String MD_COLOR_RED = "Red";
    public static final String MD_COLOR_GREEN = "Green";
    public static final String MD_COLOR_BLUE = "Blue";
    public static final String MD_COLOR_YELLOW = "Yellow";
    public static final String MD_COLOR_BLACK = "Black";
    public static final String MD_COLOR_WHITE = "White";
    public static final String MD_COLOR_GREY = "Grey";
    public static final String MD_COLOR_PINK = "Pink";

}