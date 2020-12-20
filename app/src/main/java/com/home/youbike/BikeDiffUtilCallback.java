package com.home.youbike;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class BikeDiffUtilCallback extends DiffUtil.Callback {
    private List<UBike> oldList;
    private List<UBike> newList;

    public BikeDiffUtilCallback(List<UBike> oldList, List<UBike> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldPosition, int newPosition) {
        return oldPosition == newPosition;
    }

    @Override
    public boolean areContentsTheSame(int oldPosition, int newPosition) {
        return oldList.get(oldPosition)  == newList.get(newPosition);
    }
}
