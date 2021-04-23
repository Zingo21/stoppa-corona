package com.alstromergymnasiet.stoppacorona.ui.statistics;

// Importerar funktioner:
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
import com.alstromergymnasiet.stoppacorona.ui.country.CovidCountryDetail;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// TO-DO grejer:

// TODO Försöka implementera Python Covid API i denna Java class fil
// TODO Komma på en lösning som gör att informationen om flagga, land, fall, ändring i procent och ändring i pilar visas när man kör appen.

public class StatisticsFragment extends Fragment {

    ProgressBar progressBar;                                                // Lägger till Progress bar
    RecyclerView rvCovidStatistics;                                         // Lägger till vyn
    TextView tvCovidStatistics;                                             // Lägger till Text View
    TextView tvCovidInfoFlag;                                               // Lägger till Text View
    TextView tvCovidInfoCountry;                                            // Lägger till Text View
    TextView tvCovidInfoCases;                                              // Lägger till Text View
    TextView tvCovidInfoPercent;                                            // Lägger till Text View
    TextView tvCovidInfoArrow;                                              // Lägger till Text View
    Drawable dpArrowUp, dpArrowDown, dpArrowSide;                           // Lägger till pilar som ska visa hur coronan utvecklar sig. Troligtvis kommer detta till användning, får bara försöka komma på koden till dessa så att man kan använda de.
    CovidStatisticsCountryAdapter covidStatisticsCountryAdapter;            // Lägger till en adapter som hämtar data från nätet via JSON

    private static final String TAG = StatisticsFragment.class.getSimpleName();
    List<CovidStatisticsCountry> covidCountriesStatistics;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        setHasOptionsMenu(true);

        // Kallar på vy
        rvCovidStatistics = root.findViewById(R.id.rvCovidCountryStatistics);
        progressBar = root.findViewById(R.id.progress_circular_statistics);
        rvCovidStatistics.setLayoutManager(new LinearLayoutManager(getActivity()));
        // tvCovidStatistics = root.findViewById(R.id.tvTotalCountriesStatistics); // Används ej just nu!
        tvCovidInfoFlag = root.findViewById(R.id.textViewFlag);                   // Försöker implementera denna vy
        tvCovidInfoCountry = root.findViewById(R.id.textViewCountry);             //Denna också
        tvCovidInfoCases = root.findViewById(R.id.textViewCases);                 // Denna också
        tvCovidInfoPercent = root.findViewById(R.id.tvChangeInPercent);           // Denna också
        tvCovidInfoArrow = root.findViewById(R.id.tvChangeInArrows);              // Denna också

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvCovidStatistics.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        rvCovidStatistics.addItemDecoration(dividerItemDecoration);

        // Kallar på lista
        covidCountriesStatistics = new ArrayList<>();

        // Kallar på volley metoden
        getDataFromServerSortTotalCases();

        return root;
    }

    private void showRecyclerView(){
        covidStatisticsCountryAdapter = new CovidStatisticsCountryAdapter(covidCountriesStatistics, getActivity());
        rvCovidStatistics.setAdapter(covidStatisticsCountryAdapter);

        ItemClickSupport.addTo(rvCovidStatistics).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                showSelectedCovidCountry(covidCountriesStatistics.get(position));
            }
        });
    }

    private void showSelectedCovidCountry(CovidStatisticsCountry covidCountry){
        Intent covidCovidCountryDetail = new Intent(getActivity(), CovidCountryDetail.class);
        covidCovidCountryDetail.putExtra("EXTRA_COVID", covidCountry);
        startActivity(covidCovidCountryDetail);
    }

    private void getDataFromServerSortTotalCases() {
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

                            covidCountriesStatistics.add(new CovidStatisticsCountry(data.getString("country"), data.getInt("cases"),
                                    data.getString("todayCases"), data.getString("deaths"),
                                    data.getString("todayDeaths"), data.getString("recovered"),
                                    data.getString("active"), data.getString("critical"),
                                    countryInfo.getString("flag")
                            ));
                        }

                        //Sorteras fallande
                        Collections.sort(covidCountriesStatistics, new Comparator<CovidStatisticsCountry>() {

                            @Override
                            public int compare(CovidStatisticsCountry o1, CovidStatisticsCountry o2) {
                                if (o1.getmTodayCases()>o2.getmTodayCases()){
                                    return -1;
                                }else {
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

                            covidCountriesStatistics.add(new CovidStatisticsCountry(data.getString("country"), data.getInt("cases"),
                                    data.getString("todayCases"), data.getString("deaths"),
                                    data.getString("todayDeaths"), data.getString("recovered"),
                                    data.getString("active"), data.getString("critical"),
                                    countryInfo.getString("flag")
                            ));
                        }


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
                covidCountriesStatistics.clear();
                progressBar.setVisibility(View.VISIBLE);
                getDataFromServerSortAlphabet();
                return true;
            case R.id.action_sort_cases:
                Toast.makeText(getContext(), "Sort by Total Cases", Toast.LENGTH_SHORT).show();
                covidCountriesStatistics.clear();
                progressBar.setVisibility(View.VISIBLE);
                getDataFromServerSortTotalCases();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

}
