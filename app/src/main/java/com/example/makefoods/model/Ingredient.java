package com.example.makefoods.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 냉장고 식재료 데이터 모델
 *
 * 주요 필드:
 * - id: 고유 식별자 (자동 생성)
 * - name: 재료 이름
 * - quantity: 수량
 * - registeredDate: 등록일 (밀리초 단위 타임스탬프)
 * - expiryDate: 소비기한 (밀리초 단위 타임스탬프)
 */
@Entity(tableName = "ingredients")
public class Ingredient {

    // 고유 ID (자동 생성)
    @PrimaryKey(autoGenerate = true)
    private int id;

    // 재료 이름 (예: "계란", "우유", "당근")
    private String name;

    // 수량 (예: 5개, 1개)
    private int quantity;

    // 등록일
    private long registeredDate;

    // 소비기한
    // 날짜 선택 후 저장
    private long expiryDate;

    /**
     * 생성자
     * @param name 재료 이름
     * @param quantity 수량
     * @param registeredDate 등록일
     * @param expiryDate 소비기한
     */
    public Ingredient(String name, int quantity, long registeredDate, long expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.registeredDate = registeredDate;
        this.expiryDate = expiryDate;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(long registeredDate) {
        this.registeredDate = registeredDate;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }
}