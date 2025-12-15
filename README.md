# 🧊 냉장고 비서

**AI 기반 식재료 관리 + 레시피 추천 안드로이드 앱**

냉장고에 있는 식재료를 쉽게 관리하고, AI가 추천하는 맛있는 레시피를 받아보세요.

---

## 📱 주요 기능

### 1️⃣ **카메라로 식재료 인식**
- 📸 카메라로 식재료 사진 촬영
- 🤖 Google Gemini Vision AI로 자동 인식
- ✏️ 인식 결과 수정 및 확인
- 📦 냉장고에 한 번에 추가

### 2️⃣ **냉장고 식재료 관리**
- 📋 모든 식재료 목록 조회
- ➕ 수동으로 식재료 추가
- 🗑️ 식재료 삭제 (다중 선택)
- 📅 소비기한 관리
- 🔄 **중복 식재료는 자동으로 수량 증가**

### 3️⃣ **AI 레시피 추천**
- 💬 채팅으로 자연스럽게 대화
- 🍳 냉장고의 식재료 기반 레시피 추천
- 📖 **레시피 상세 정보 확인** (재료, 조리법 등)
- 🎯 정확한 추천 (DB에 저장된 레시피만)

---

## 🎬 앱 화면

### 📸 카메라 & 인식 화면
```
[여기에 카메라 화면 스크린샷 추가]
[여기에 인식 결과 화면 스크린샷 추가]
```

### 🧊 냉장고 관리 화면
```
[여기에 냉장고 목록 화면 스크린샷 추가]
[여기에 식재료 추가 화면 스크린샷 추가]
[여기에 삭제 모드 화면 스크린샷 추가]
```

### 💬 채팅 & 레시피 추천 화면
```
[여기에 채팅 화면 스크린샷 추가]
[여기에 레시피 추천 화면 스크린샷 추가]
[여기에 레시피 상세 정보 화면 스크린샷 추가]
```

---

## 🎥 동작 영상

### 📱 전체 앱 사용 흐름
```
[여기에 전체 동작 영상 추가]
```

**영상 구성:**
1. 카메라로 식재료 촬영 및 인식
2. 냉장고에 식재료 추가
3. 채팅으로 레시피 추천 받기
4. 레시피 상세 정보 확인

---

## 🛠️ 기술 스택

### **프론트엔드**
- **Language**: Java
- **Framework**: Android (API Level 28+)
- **Architecture**: MVVM (ViewModel, LiveData)
- **UI Components**: Fragment, RecyclerView, BottomSheetDialog

### **데이터베이스**
- **Room**: 로컬 SQLite 데이터베이스
- **Entities**: Ingredient, Recipe, Message

### **API 통합**
- **Google Gemini Vision API**: 식재료 이미지 인식
- **OpenAI API**: 레시피 추천 (ChatGPT)

### **주요 라이브러리**
```gradle
// Architecture
implementation "androidx.lifecycle:lifecycle-viewmodel:2.5.1"
implementation "androidx.lifecycle:lifecycle-livedata:2.5.1"

// Database
implementation "androidx.room:room-runtime:2.5.2"
annotationProcessor "androidx.room:room-compiler:2.5.2"

// Networking
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"

// UI
implementation "com.google.android.material:material:1.9.0"
implementation "androidx.recyclerview:recyclerview:1.3.0"
```

---

## 📦 프로젝트 구조

```
app/src/main/
├── java/com/example/makefoods/
│   ├── database/
│   │   ├── AppDatabase.java
│   │   ├── IngredientDao.java
│   │   ├── RecipeDao.java
│   │   └── MessageDao.java
│   ├── model/
│   │   ├── Ingredient.java
│   │   ├── Recipe.java
│   │   └── Message.java
│   ├── network/
│   │   ├── GeminiService.java
│   │   ├── OpenAIService.java
│   │   └── RetrofitClient.java
│   ├── repository/
│   │   ├── IngredientRepository.java
│   │   ├── RecipeRepository.java
│   │   └── ChatRepository.java
│   └── ui/
│       ├── camera/
│       │   ├── CameraFragment.java
│       │   └── ImageResultFragment.java
│       ├── fridge/
│       │   ├── FridgeFragment.java
│       │   ├── FridgeViewModel.java
│       │   └── IngredientAdapter.java
│       └── chat/
│           ├── ChatFragment.java
│           ├── ChatViewModel.java
│           ├── ChatAdapter.java
│           └── RecipeDetailFragment.java
└── res/
    ├── layout/
    ├── drawable/
    └── values/
```

---

## 🚀 설치 및 실행

### **필수 요구사항**
- Android Studio (2022.1 이상)
- Android SDK 28 이상
- Gradle 7.0 이상

### **API 키 설정**
1. **Google Gemini API** 키 발급 ([여기](https://aistudio.google.com/app/apikey))
2. **OpenAI API** 키 발급 ([여기](https://platform.openai.com/api-keys))
3. `local.properties` 또는 환경 변수에 추가:
```properties
GEMINI_API_KEY=your_gemini_api_key
OPENAI_API_KEY=your_openai_api_key
```

### **실행 방법**
```bash
# 1. 프로젝트 클론
git clone https://github.com/your-username/refrigerator_secretary.git
cd refrigerator_secretary

# 2. API 키 설정 (위 과정)

# 3. 앱 빌드 및 실행
./gradlew build
# Android Studio에서 Run 버튼 클릭
```

---

## 💡 주요 구현 사항

### **카메라 식재료 인식**
- Gemini Vision API를 사용한 이미지 분석
- 인식된 텍스트 자동 파싱
- 사용자 수정 가능한 바텀시트 UI

### **냉장고 관리**
- Room Database를 통한 로컬 저장
- **중복 식재료 자동 감지 및 수량 증가**
- 소비기한 기반 정렬
- 다중 삭제 UI

### **AI 레시피 추천**
- OpenAI ChatGPT API 통합
- 냉장고 식재료 기반 필터링
- **DB에 존재하는 레시피만 추천**
- 실시간 채팅 UI

---

## 📝 개발 과정

### **초기 기획**
- 사용자 니즈 분석
- 주요 기능 정의
- UI/UX 설계

### **구현 순서**
1. ✅ 데이터베이스 설계 및 구현 (Room)
2. ✅ 카메라 및 Gemini API 통합
3. ✅ 냉장고 화면 UI 구현
4. ✅ 채팅 기능 및 OpenAI API 통합
5. ✅ 레시피 상세 정보 화면
6. ✅ 중복 식재료 수량 증가 기능
7. ✅ UI/UX 개선 (커스텀 메시지, 로딩 다이얼로그 등)

### **현재 상태**
- ✅ 핵심 기능 완성
- ✅ API 통합 완료
- ✅ 데이터 관리 기능 구현

### **향후 계획** (예정)
- [ ] 소비기한 임박 알림
- [ ] 즐겨찾기 레시피 기능
- [ ] 영양가 정보 표시
- [ ] 쇼핑 목록 생성
- [ ] 푸시 알림
- [ ] 다크 모드

---

## 📚 학습 내용

이 프로젝트를 통해 배운 기술:
- **Android Architecture**: MVVM 패턴, ViewModel, LiveData
- **Database**: Room ORM, 쿼리 최적화
- **Networking**: Retrofit, REST API 통합
- **API 활용**: Gemini Vision API, OpenAI API
- **UI/UX**: Fragment, RecyclerView, 커스텀 디자인
- **Git**: 협업 개발 및 버전 관리

---

**마지막 업데이트**: 2025년 12월
