package com.movie_collection.be;

public class Category {
    private int id;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Category2{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}