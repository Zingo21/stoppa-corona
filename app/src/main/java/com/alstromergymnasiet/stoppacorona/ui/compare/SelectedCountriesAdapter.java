package com.alstromergymnasiet.stoppacorona.ui.compare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alstromergymnasiet.stoppacorona.R;
import com.alstromergymnasiet.stoppacorona.ui.country.CovidCountry;

import java.util.List;

public class SelectedCountriesAdapter extends RecyclerView.Adapter<SelectedCountriesAdapter.ViewHolder> {

    private List<CovidCountry> selectedCountries;
    private OnRemoveClickListener onRemoveClickListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public SelectedCountriesAdapter(List<CovidCountry> selectedCountries, OnRemoveClickListener onRemoveClickListener) {
        this.selectedCountries = selectedCountries;
        this.onRemoveClickListener = onRemoveClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_country, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CovidCountry country = selectedCountries.get(position);
        holder.tvCountryName.setText(country.getmCovidCountry());
        
        holder.btnRemove.setOnClickListener(v -> {
            if (onRemoveClickListener != null) {
                onRemoveClickListener.onRemoveClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedCountries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCountryName;
        Button btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCountryName = itemView.findViewById(R.id.tvCountryName);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
