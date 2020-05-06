package com.non.sleep.naver.android.src;

public class selectedMenu {
    int menuNo;
    String name;
    int price;

    public int getMenuNo() {
        return menuNo;
    }

    public void setMenuNo(int menuNo) {
        this.menuNo = menuNo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return 1000;
    }

    public selectedMenu(String name, int price, int menuNo) {
        this.name = name;
        this.price = price;
        this.menuNo = menuNo;
    }


}
