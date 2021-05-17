package com.alstromergymnasiet.stoppacorona.ui.statistics;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alstromergymnasiet.stoppacorona.R;
import com.alstromergymnasiet.stoppacorona.ui.country.ItemClickSupport;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatisticsFragment2 extends Fragment {

    ProgressBar progressBar;
    RecyclerView rvCovidStatistics;
    TextView tvCovidInfoFlag;
    TextView tvCovidInfoCountry;
    TextView tvCovidInfoCases;
    TextView tvCovidInfoPercent;
    TextView tvCovidInfoArrow;
    Drawable dpArrowUp, dpArrowDown, dpArrowSide;
    CovidStatisticsCountryAdapter covidStatisticsCountryAdapter;

    private static final String TAG = StatisticsFragment2.class.getSimpleName();
    List<CovidStatisticsCountry> covidStatisticsCountries;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        setHasOptionsMenu(true);

        rvCovidStatistics = root.findViewById(R.id.rvCovidCountryStatistics);
        progressBar = root.findViewById(R.id.progress_circular_statistics);
        rvCovidStatistics.setLayoutManager(new LinearLayoutManager(getActivity()));
        tvCovidInfoFlag = root.findViewById(R.id.textViewFlag);
        tvCovidInfoCountry = root.findViewById(R.id.textViewCountry);
        tvCovidInfoCases = root.findViewById(R.id.textViewCases);
        tvCovidInfoPercent = root.findViewById(R.id.tvChangeInPercent);
        tvCovidInfoArrow = root.findViewById(R.id.tvChangeInArrows);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvCovidStatistics.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        rvCovidStatistics.addItemDecoration(dividerItemDecoration);

        covidStatisticsCountries = new ArrayList<>();
        
        getDataFromServerSortTotalCases();
        
        return root;
    }

    private void showRecyclerView(){
        covidStatisticsCountryAdapter = new CovidStatisticsCountryAdapter(covidStatisticsCountries, getActivity());
        rvCovidStatistics.setAdapter(covidStatisticsCountryAdapter);

        // ItemClickSupport.addTo(rvCovidStatistics).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
           // @Override
           // public void onItemClicked(RecyclerView recyclerView, int position, View v) {
             //   showSelectedCountry(covidStatisticsCountries.get(position));
        // }
        //});
    }

    // Raden nedan Kommer inte att behövas då det ändå bara kommer visas andra länder med statistik
    // TODO Ta bort raden nedanför
   // private void showSelectedCountry(CovidStatisticsCountry covidStatisticsCountry) {
    //    Intent covidCovidCountryStatisticsDtail = new Intent(getActivity(), Covi)
    // }

    private void getDataFromServerSortTotalCases() {
        String url = "https://disease.sh/v3/covid-19/countries";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                if (response !=null){
                    Log.e(TAG, "onResponse: " + response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);

                            JSONObject countryInfo = data.getJSONObject("countryInfo");

                            covidStatisticsCountries.add(new CovidStatisticsCountry(data.getString("country"), data.getInt("cases"),
                                    data.getString("todayCases"), data.getString("deaths"),
                                    data.getString("todayDeaths"), data.getString("recovered"),
                                    data.getString("active"), data.getString("critical"),
                                    countryInfo.getString("flag")
                            ));
                        }

                        Collections.sort(covidStatisticsCountries, new Comparator<CovidStatisticsCountry>() {

                            @Override
                            public int compare(CovidStatisticsCountry o1, CovidStatisticsCountry o2) {
                                if (o1.getmTodayCases()> o2.getmTodayCases()){
                                    return -1;
                                }else{
                                    return 1;
                                }
                            }
                        });

                        // Action Bar Title
                        getActivity().setTitle(jsonArray.length()+" countries");

                        showRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "onResponse: "+error);
                    }
                });
        Volley.newRequestQueue(getActivity()).add(stringRequest);
    }

    private void getDataFromServerSortAlphabet() {
        String url = "https://disease.sh/v3/covid-19/countries";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                if (response != null) {
                    Log.e(TAG, "onResponse: " + response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);

                            // Extrahera JSONObject inom ett JSONObject
                            JSONObject countryInfo = data.getJSONObject("countryInfo");

                            covidStatisticsCountries.add(new CovidStatisticsCountry(data.getString("country"), data.getInt("cases"),
                                    data.getString("todayCases"), data.getString("deaths"),
                                    data.getString("todayDeaths"), data.getString("recovered"),
                                    data.getString("active"), data.getString("critical"),
                                    countryInfo.getString("flag")
                            ));
                        }

                        getActivity().setTitle(jsonArray.length() + " countries");

                        showRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "onResponse: "+error);
                    }
                });
        Volley.newRequestQueue(getActivity()).add(stringRequest);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.country_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = new SearchView(getActivity());
        searchView.setQueryHint("Search...");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (covidStatisticsCountryAdapter!=null){
                    covidStatisticsCountryAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        searchItem.setActionView(searchView);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_sort_alpha:
                Toast.makeText(getContext(), "Sort Alphapetically", Toast.LENGTH_SHORT).show();
                covidStatisticsCountries.clear();
                progressBar.setVisibility(View.VISIBLE);
                getDataFromServerSortAlphabet();
                return true;
            case R.id.action_sort_cases:
                Toast.makeText(getContext(), "Sort by Total Cases", Toast.LENGTH_SHORT).show();
                covidStatisticsCountries.clear();
                progressBar.setVisibility(View.VISIBLE);
                getDataFromServerSortTotalCases();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

}
