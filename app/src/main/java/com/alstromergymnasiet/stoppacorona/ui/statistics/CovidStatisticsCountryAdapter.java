package com.alstromergymnasiet.stoppacorona.ui.statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alstromergymnasiet.stoppacorona.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class CovidStatisticsCountryAdapter extends RecyclerView.Adapter<CovidStatisticsCountryAdapter.ViewHolder> implements Filterable {

    private List<CovidCountryStatistics> covidStatisticsCountries;
    private List<CovidCountryStatistics> covidStatisticsCountriesFull;
    private Context context;

    public CovidStatisticsCountryAdapter(List<CovidCountryStatistics> covidStatisticsCountries, Context context) {
        this.covidStatisticsCountries = covidStatisticsCountries;
        this.context = context;
        covidStatisticsCountriesFull = new ArrayList<>(covidStatisticsCountries);


    }

    @NonNull
    @Override
    public CovidStatisticsCountryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_covid_country, parent, false);

        return new CovidStatisticsCountryAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CovidStatisticsCountryAdapter.ViewHolder holder, int position) {
        CovidCountryStatistics covidCountryStatistics = covidStatisticsCountries.get(position);
        holder.tvTotalCases.setText(Integer.toString(covidCountryStatistics.getmTodayCases()));
        holder.tvCountryName.setText(covidCountryStatistics.getmCovidCountry());

        // Glide
        Glide.with(context)
                .load(covidCountryStatistics.getmFlags())
                .apply(new RequestOptions().override(240, 160))
                .into(holder.imgCountryFlag);


    }

    @Override
    public int getItemCount() {
        return covidStatisticsCountries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTotalCases, tvCountryName;
        ImageView imgCountryFlag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTotalCases = itemView.findViewById(R.id.tvTotalCases);
            tvCountryName = itemView.findViewById(R.id.tvCountryName);
            imgCountryFlag = itemView.findViewById(R.id.imgCountryFlag);

        }
    }

    @Override
    public Filter getFilter() {
        return covidStatisticsCountriesFilter;

    }

    private Filter covidStatisticsCountriesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CovidCountryStatistics> filteredCovidCountryStatistics = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredCovidCountryStatistics.addAll(covidStatisticsCountriesFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (CovidCountryStatistics itemCovidCountryStatistics : covidStatisticsCountriesFull) {
                    if (itemCovidCountryStatistics.getmCovidCountry().toLowerCase().contains(filterPattern)) {
                        filteredCovidCountryStatistics.add(itemCovidCountryStatistics);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredCovidCountryStatistics;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            covidStatisticsCountries.clear();
            covidStatisticsCountries.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}