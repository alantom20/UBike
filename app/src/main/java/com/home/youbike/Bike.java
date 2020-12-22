package com.home.youbike;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;
@Entity
public class Bike {
    @NotNull
    @PrimaryKey
    public String sno;
    @NotNull
    public Boolean star;

    public Bike(@NotNull String sno, @NotNull Boolean star) {
        this.sno = sno;
        this.star = star;
    }
}
