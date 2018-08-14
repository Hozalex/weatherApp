package com.example.ahozyainov.adapters;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ahozyainov.activities.R;
import com.example.ahozyainov.models.Cities;

import java.util.ArrayList;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private OnCityClickListener onCityClickListener;
    private ArrayList<Cities> citiesArrayList;

    public CityAdapter(ArrayList<Cities> citiesArrayList, OnCityClickListener onCityClickListener) {
        this.citiesArrayList = citiesArrayList;
        this.onCityClickListener = onCityClickListener;

    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final CityViewHolder viewHolder = new CityViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCityClickListener.onCityClick(viewHolder.getAdapterPosition());
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        holder.tvTitle.setText((citiesArrayList.get(position).name).toUpperCase());

    }


    @Override
    public int getItemCount() {
        return citiesArrayList.size();
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle;


        public CityViewHolder(final View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tvTitle.setClipToOutline(true);
            }
            tvTitle.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    MenuInflater menuInflater = new MenuInflater(itemView.getContext());
                    menuInflater.inflate(R.menu.context_menu, contextMenu);

                }

            });


        }


    }


    public interface OnCityClickListener {
        void onCityClick(int cityPosition);
    }
}


