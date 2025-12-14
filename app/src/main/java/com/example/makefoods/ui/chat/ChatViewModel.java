package com.example.makefoods.ui.chat;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.makefoods.BuildConfig;
import com.example.makefoods.data.chat.ChatRepository;
import com.example.makefoods.data.chat.ChatRepositoryImpl;
import com.example.makefoods.data.recipe.RecipeRepository;
import com.example.makefoods.database.AppDatabase;
import com.example.makefoods.database.IngredientDao;
import com.example.makefoods.model.Ingredient;
import com.example.makefoods.model.Message;
import com.example.makefoods.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatViewModel
 *
 * 채팅 UI의 비즈니스 로직을 담당하는 클래스
 *
 * 책임:
 * 1. 사용자 메시지 처리
 * 2. Gemini/GPT API 호출
 * 3. 재료 인식 결과로 레시피 DB 검색
 * 4. 메시지 목록 관리
 *
 * 모듈화:
 * - ChatRepository: OpenAI API 호출
 * - RecipeRepository: 레시피 DB 검색
 * - IngredientDao: 냉장고 재료 조회
 */
public class ChatViewModel extends AndroidViewModel {

    private static final String TAG = "ChatViewModel";

    // 채팅 메시지 목록
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());

    // 레시피 다이얼로그 이벤트
    private final MutableLiveData<RecipeDialogData> recipeDialogEvent = new MutableLiveData<>();

    // ChatRepository (OpenAI API 호출)
    private final ChatRepository chatRepo = new ChatRepositoryImpl(BuildConfig.OPENAI_API_KEY);

    // RecipeRepository (DB 검색)
    private final RecipeRepository recipeRepository;

    // IngredientDao (재료 조회)
    private final IngredientDao ingredientDao;

    public ChatViewModel(Application application) {
        super(application);

        // DB 인스턴스 획득
        AppDatabase db = AppDatabase.getInstance(application);
        this.ingredientDao = db.ingredientDao();

        // RecipeRepository 초기화 (레시피 검색용)
        this.recipeRepository = new RecipeRepository(application);

        Log.d(TAG, "ChatViewModel 초기화 완료");
        Log.d(TAG, "DB에 저장된 레시피: " + recipeRepository.getRecipeCount() + "개");
    }

    // Getter
    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public LiveData<RecipeDialogData> getRecipeDialogEvent() {
        return recipeDialogEvent;
    }

    /**
     * 사용자 메시지 전송 및 응답 생성
     *
     * 동작:
     * 1. 사용자 메시지 추가
     * 2. Gemini로 재료 인식
     * 3. 인식된 재료로 DB에서 레시피 검색
     * 4. 검색 결과를 메시지로 표시 (DB에 있는 것만!)
     */
    public void sendUserMessage(String text) {
        // 사용자 메시지 추가
        List<Message> list = new ArrayList<>(messages.getValue());
        list.add(new Message(text, Message.Sender.USER));
        messages.setValue(list);

        // 백그라운드에서 처리
        new Thread(() -> {
            try {
                // 냉장고 재료 조회
                List<Ingredient> ingredients = ingredientDao.getAllIngredientsSync();
                String ingredientInfo = buildIngredientInfoString(ingredients);
                List<Message> recentMessages = getRecentMessages(list, 10);

                // GPT에 요청
                chatRepo.askGpt(text, ingredientInfo, recentMessages, new ChatRepository.Callback() {
                    @Override
                    public void onSuccess(String reply) {
                        // ===== 응답 형식 확인 =====
                        if (reply.startsWith("RECIPE_LIST:")) {
                            // GPT가 "이런 음식들을 만들 수 있어요" 라고 응답
                            List<String> recipeNames = parseRecipeList(reply);
                            Log.d(TAG, "GPT가 추천한 음식: " + recipeNames);

                            // ===== DB에서 실제 레시피 검색 =====
                            List<Recipe> foundRecipes = new ArrayList<>();
                            for (String recipeName : recipeNames) {
                                List<Recipe> results = recipeRepository.searchRecipeByName(recipeName);
                                foundRecipes.addAll(results);
                            }

                            Log.d(TAG, "DB에서 찾은 레시피: " + foundRecipes.size() + "개");

                            // ===== DB에 있는 레시피만 추천 =====
                            if (foundRecipes.isEmpty()) {
                                // DB에서 찾은 레시피가 없으면 메시지만 표시
                                List<Message> cur = new ArrayList<>(messages.getValue());
                                cur.add(new Message("이 재료들로는 저장된 레시피가 없네요.", Message.Sender.BOT));
                                messages.postValue(cur);
                            } else {
                                // DB에서 찾은 레시피만 추천 (실제로 찾은 레시피들)
                                List<String> foundRecipeNames = new ArrayList<>();
                                for (Recipe recipe : foundRecipes) {
                                    foundRecipeNames.add(recipe.getName());
                                }
                                
                                String displayText = "이런 음식들을 만들 수 있어요!";
                                List<Message> cur = new ArrayList<>(messages.getValue());
                                cur.add(new Message(displayText, Message.Sender.BOT, foundRecipeNames));
                                messages.postValue(cur);
                            }

                        } else {
                            // 일반적인 텍스트 응답
                            List<Message> cur = new ArrayList<>(messages.getValue());
                            cur.add(new Message(reply, Message.Sender.BOT));
                            messages.postValue(cur);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "Gemini 요청 실패: " + t.getMessage());
                        List<Message> cur = new ArrayList<>(messages.getValue());
                        cur.add(new Message("죄송합니다. 다시 시도해주세요.", Message.Sender.BOT));
                        messages.postValue(cur);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "메시지 처리 중 오류: " + e.getMessage(), e);
                List<Message> cur = new ArrayList<>(messages.getValue());
                cur.add(new Message("오류가 발생했어요.", Message.Sender.BOT));
                messages.postValue(cur);
            }
        }).start();
    }

    /**
     * 재료 기반 레시피 검색
     * 
     * DB에 있는 레시피만 추천
     */
    public void searchRecipesByRecognizedIngredients(List<String> recognizedIngredients) {
        Log.d(TAG, "재료 기반 레시피 검색: " + recognizedIngredients);

        new Thread(() -> {
            try {
                // ===== RecipeRepository를 통해 DB 검색 =====
                List<Recipe> foundRecipes = recipeRepository.searchRecipesByIngredients(recognizedIngredients);

                Log.d(TAG, "검색 완료: " + foundRecipes.size() + "개 레시피 찾음");

                // ===== 검색 결과를 메시지로 변환 (DB에 있는 것만) =====
                if (foundRecipes.isEmpty()) {
                    // 검색 결과 없음
                    List<Message> cur = new ArrayList<>(messages.getValue());
                    cur.add(new Message("이 재료들로는 저장된 레시피가 없네요.", Message.Sender.BOT));
                    messages.postValue(cur);
                } else {
                    // 검색 결과 표시 (DB에서 찾은 것만)
                    List<String> recipeNames = new ArrayList<>();
                    for (Recipe recipe : foundRecipes) {
                        recipeNames.add(recipe.getName());
                    }

                    String displayText = foundRecipes.size() + "개의 음식을 만들 수 있어요!";
                    List<Message> cur = new ArrayList<>(messages.getValue());
                    cur.add(new Message(displayText, Message.Sender.BOT, recipeNames));
                    messages.postValue(cur);
                }

            } catch (Exception e) {
                Log.e(TAG, "레시피 검색 실패: " + e.getMessage(), e);
                List<Message> cur = new ArrayList<>(messages.getValue());
                cur.add(new Message("레시피 검색 중 오류가 발생했어요.", Message.Sender.BOT));
                messages.postValue(cur);
            }
        }).start();
    }

    /**
     * 레시피 상세 정보 조회
     *
     * 사용자가 추천된 음식을 클릭했을 때:
     * 1. RecipeRepository에서 해당 레시피 조회
     * 2. DB의 상세 정보를 UI에 표시
     */
    public void requestRecipeDetail(String recipeName) {
        Log.d(TAG, "레시피 상세 조회: " + recipeName);

        new Thread(() -> {
            try {
                // ===== DB에서 정확한 레시피 검색 =====
                List<Recipe> results = recipeRepository.searchRecipeByName(recipeName);

                if (results.isEmpty()) {
                    Log.w(TAG, "DB에서 '" + recipeName + "' 찾지 못함");
                    List<Message> cur = new ArrayList<>(messages.getValue());
                    cur.add(new Message("죄송해요. 레시피를 찾을 수 없어요.", Message.Sender.BOT));
                    messages.postValue(cur);
                } else {
                    // 첫 번째 검색 결과 사용
                    Recipe recipe = results.get(0);
                    Log.d(TAG, "레시피 찾음: " + recipe.getName());

                    List<Message> cur = new ArrayList<>(messages.getValue());
                    cur.add(new Message("", Message.Sender.BOT, recipe));
                    messages.postValue(cur);
                }

            } catch (Exception e) {
                Log.e(TAG, "레시피 상세 조회 실패: " + e.getMessage(), e);
                List<Message> cur = new ArrayList<>(messages.getValue());
                cur.add(new Message("레시피를 불러올 수 없어요.", Message.Sender.BOT));
                messages.postValue(cur);
            }
        }).start();
    }

    /**
     * 냉장고 재료를 문자열로 변환
     */
    private String buildIngredientInfoString(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "냉장고가 비어있습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("냉장고 재료:\n");

        for (Ingredient ingredient : ingredients) {
            sb.append("- ").append(ingredient.getName());
            sb.append(" (").append(ingredient.getQuantity()).append("개)");

            // 유통기한 정보
            long daysUntilExpiry = (ingredient.getExpiryDate() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
            if (daysUntilExpiry <= 3 && daysUntilExpiry >= 0) {
                sb.append(" [소비기한 임박: ").append(daysUntilExpiry).append("일]");
            } else if (daysUntilExpiry < 0) {
                sb.append(" [유통기한 지남]");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 최근 메시지 가져오기
     */
    private List<Message> getRecentMessages(List<Message> allMessages, int maxCount) {
        if (allMessages == null || allMessages.isEmpty()) {
            return new ArrayList<>();
        }

        int size = allMessages.size();
        int startIndex = Math.max(0, size - maxCount);

        return new ArrayList<>(allMessages.subList(startIndex, size));
    }

    /**
     * 레시피 목록 파싱
     */
    private List<String> parseRecipeList(String reply) {
        List<String> recipes = new ArrayList<>();

        String[] lines = reply.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("- ")) {
                String recipeName = line.substring(2).trim();
                if (!recipeName.isEmpty()) {
                    recipes.add(recipeName);
                }
            }
        }

        return recipes;
    }

    /**
     * 레시피 다이얼로그 데이터
     */
    public static class RecipeDialogData {
        public final String recipeName;
        public final String recipeDetail;

        public RecipeDialogData(String recipeName, String recipeDetail) {
            this.recipeName = recipeName;
            this.recipeDetail = recipeDetail;
        }
    }
}
