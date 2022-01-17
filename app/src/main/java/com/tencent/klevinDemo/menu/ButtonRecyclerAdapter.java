package com.tencent.klevinDemo.menu;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tencent.klevinDemo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ButtonRecyclerAdapter extends RecyclerView.Adapter<ButtonRecyclerAdapter.ButtonViewHolder> {
    private List<String> texts;
    private View.OnClickListener listener;

    @SuppressLint("NotifyDataSetChanged")
    public void setTexts(String[] texts) {
        this.texts = new ArrayList<>(texts.length);
        this.texts.addAll(Arrays.asList(texts));
        notifyDataSetChanged();
    }

    public void setClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public String getText(int i) {
        return texts.get(i);
    }

    @NonNull
    @Override
    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_ad_list_view, viewGroup, false);
        return new ButtonViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonViewHolder adTypeViewHolder, int i) {
        adTypeViewHolder.button.setText(texts.get(i));
        adTypeViewHolder.button.setTag(i);//用于listener处理
        adTypeViewHolder.button.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return texts.size();
    }

    static class ButtonViewHolder extends RecyclerView.ViewHolder {
        Button button;

        public ButtonViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.btn_ad_type);
        }
    }

}
