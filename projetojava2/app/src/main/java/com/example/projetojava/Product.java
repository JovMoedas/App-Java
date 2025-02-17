package com.example.projetojava;

public class Product {
    private int id;
    private String name;
    private double price; // 🔥 O tipo deve ser double

    public Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() { // 🔥 Certifica-te de que este método existe
        return price;
    }
}

