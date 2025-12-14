package com.example.makefoods.model;

import java.util.ArrayList;
import java.util.List;

public class Message {
    public enum Sender { USER, BOT }

    public enum Type {
        NORMAL,          // 일반 메시지
        RECIPE_LIST,     // 레시피 추천 리스트 (버튼 포함)
        RECIPE_CARD      // 레시피 카드 (상세 정보)
    }

    private final String text;
    private final Sender sender;
    private final Type type;
    private final List<String> recipeOptions;  // 레시피 옵션 리스트
    private final List<Recipe> recipes;        // 레시피 카드 데이터

    // 일반 메시지 생성자
    public Message(String text, Sender sender) {
        this.text = text;
        this.sender = sender;
        this.type = Type.NORMAL;
        this.recipeOptions = new ArrayList<>();
        this.recipes = new ArrayList<>();
    }


    public Message(String text, Sender sender, List<String> recipeOptions) {
        this.text = text;
        this.sender = sender;
        this.type = Type.RECIPE_LIST;
        this.recipeOptions = recipeOptions != null ? recipeOptions : new ArrayList<>();
        this.recipes = new ArrayList<>();
    }


    public Message(String text, Sender sender, Recipe recipe) {
        this.text = text;
        this.sender = sender;
        this.type = Type.RECIPE_CARD;
        this.recipeOptions = new ArrayList<>();
        this.recipes = new ArrayList<>();
        if (recipe != null) {
            this.recipes.add(recipe);
        }
    }


    public static Message createRecipeCardMessage(String text, Sender sender, List<Recipe> recipes) {
        Message msg = new Message(text, sender);

        return new Message(text, sender, recipes, Type.RECIPE_CARD);
    }


    private Message(String text, Sender sender, List<Recipe> recipes, Type type) {
        this.text = text;
        this.sender = sender;
        this.type = type;
        this.recipeOptions = new ArrayList<>();
        this.recipes = recipes != null ? new ArrayList<>(recipes) : new ArrayList<>();
    }

    public String getText() { return text; }
    public Sender getSender() { return sender; }
    public Type getType() { return type; }
    public List<String> getRecipeOptions() { return recipeOptions; }
    public List<Recipe> getRecipes() { return recipes; }
}