package com.alstromergymnasiet.stoppacorona.ui.compare;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alstromergymnasiet.stoppacorona.R;
import com.alstromergymnasiet.stoppacorona.ui.country.CovidCountry;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CompareFragment extends Fragment {

    private static final String TAG = CompareFragment.class.getSimpleName();
    private static final String PREFS_NAME = "ComparePrefs";
    private static final String KEY_SELECTED_COUNTRIES = "selectedCountries";
    private static final String KEY_CHART_TYPE = "chartType";
    private static final String KEY_DATA_TYPE = "dataType";

    private Spinner spinnerCountries;
    private Button btnAddCountry;
    private Button btnClearAll;
    private Button btnCompare;
    private Button btnSaveComparison;
    private TextView tvSelectedCountries;
    private RadioGroup radioChartType;
    private RadioGroup radioDataType;
    private BarChart barChart;
    private LineChart lineChart;
    private ProgressBar progressBar;
    private RecyclerView rvSelectedCountries;

    private List<CovidCountry> allCountries;
    private List<CovidCountry> selectedCountries;
    private List<String> countryNames;
    private SelectedCountriesAdapter selectedCountriesAdapter;
    private SharedPreferences sharedPreferences;
    private boolean dataLoaded = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_compare, container, false);

        // Initialize views
        spinnerCountries = root.findViewById(R.id.spinnerCountries);
        btnAddCountry = root.findViewById(R.id.btnAddCountry);
        btnClearAll = root.findViewById(R.id.btnClearAll);
        btnCompare = root.findViewById(R.id.btnCompare);
        btnSaveComparison = root.findViewById(R.id.btnSaveComparison);
        tvSelectedCountries = root.findViewById(R.id.tvSelectedCountries);
        radioChartType = root.findViewById(R.id.radioChartType);
        radioDataType = root.findViewById(R.id.radioDataType);
        barChart = root.findViewById(R.id.barChart);
        lineChart = root.findViewById(R.id.lineChart);
        progressBar = root.findViewById(R.id.progressBar);
        rvSelectedCountries = root.findViewById(R.id.rvSelectedCountries);

        // Initialize SharedPreferences
        sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize lists
        allCountries = new ArrayList<>();
        selectedCountries = new ArrayList<>();
        countryNames = new ArrayList<>();

        // Set up RecyclerView for selected countries
        rvSelectedCountries.setLayoutManager(new LinearLayoutManager(getContext()));
        selectedCountriesAdapter = new SelectedCountriesAdapter(selectedCountries, position -> {
            selectedCountries.remove(position);
            selectedCountriesAdapter.notifyDataSetChanged();
            updateSelectedCountriesText();
        });
        rvSelectedCountries.setAdapter(selectedCountriesAdapter);

        // Fetch country data
        fetchCountryData();

        // Set up button click listeners
        btnAddCountry.setOnClickListener(v -> addSelectedCountry());
        btnClearAll.setOnClickListener(v -> clearAllCountries());
        btnCompare.setOnClickListener(v -> updateChart());
        btnSaveComparison.setOnClickListener(v -> saveComparison());

        return root;
    }

    private void fetchCountryData() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://disease.sh/v3/covid-19/countries";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                if (response != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);
                            JSONObject countryInfo = data.getJSONObject("countryInfo");

                            String countryName = data.getString("country");
                            int cases = data.getInt("cases");
                            String todayCases = data.getString("todayCases");
                            String deaths = data.getString("deaths");
                            String todayDeaths = data.getString("todayDeaths");
                            String recovered = data.getString("recovered");
                            String active = data.getString("active");
                            String critical = data.getString("critical");
                            String flag = countryInfo.getString("flag");

                            allCountries.add(new CovidCountry(countryName, cases, todayCases, deaths, todayDeaths, recovered, active, critical, flag));
                            countryNames.add(countryName);
                        }

                        // Set up spinner with country names
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, countryNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCountries.setAdapter(adapter);

                        // Data is now loaded, try to load saved comparison
                        dataLoaded = true;
                        loadSavedComparison();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error fetching data: " + error);
                Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(getContext()).add(stringRequest);
    }

    private void addSelectedCountry() {
        if (!dataLoaded) {
            Toast.makeText(getContext(), "Please wait for data to load", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedCountryName = spinnerCountries.getSelectedItem().toString();
        
        // Check if country is already selected
        for (CovidCountry country : selectedCountries) {
            if (country.getmCovidCountry().equals(selectedCountryName)) {
                Toast.makeText(getContext(), selectedCountryName + " is already selected", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Find the country in allCountries and add to selectedCountries
        for (CovidCountry country : allCountries) {
            if (country.getmCovidCountry().equals(selectedCountryName)) {
                selectedCountries.add(country);
                break;
            }
        }

        selectedCountriesAdapter.notifyDataSetChanged();
        updateSelectedCountriesText();
    }

    private void clearAllCountries() {
        selectedCountries.clear();
        selectedCountriesAdapter.notifyDataSetChanged();
        updateSelectedCountriesText();
        Toast.makeText(getContext(), "All countries cleared", Toast.LENGTH_SHORT).show();
    }

    private void updateSelectedCountriesText() {
        if (selectedCountries.isEmpty()) {
            tvSelectedCountries.setText(R.string.no_countries_selected);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Selected: ");
            for (int i = 0; i < selectedCountries.size(); i++) {
                sb.append(selectedCountries.get(i).getmCovidCountry());
                if (i < selectedCountries.size() - 1) {
                    sb.append(", ");
                }
            }
            tvSelectedCountries.setText(sb.toString());
        }
    }

    private void updateChart() {
        if (selectedCountries.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one country", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine chart type
        boolean isBarChart = radioChartType.getCheckedRadioButtonId() == R.id.radioBarChart;
        barChart.setVisibility(isBarChart ? View.VISIBLE : View.GONE);
        lineChart.setVisibility(isBarChart ? View.GONE : View.VISIBLE);

        // Determine data type
        int dataTypeId = radioDataType.getCheckedRadioButtonId();
        List<Float> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (CovidCountry country : selectedCountries) {
            labels.add(country.getmCovidCountry());
            
            float value = getDataValue(country, dataTypeId);
            values.add(value);
        }

        if (isBarChart) {
            showBarChart(labels, values);
        } else {
            showLineChart(labels, values);
        }
    }

    private float getDataValue(CovidCountry country, int dataTypeId) {
        try {
            if (dataTypeId == R.id.radioTotalCases) {
                return (float) country.getmCases();
            } else if (dataTypeId == R.id.radioTodayCases) {
                return country.getmTodayCases() != null ? Float.parseFloat(country.getmTodayCases()) : 0f;
            } else if (dataTypeId == R.id.radioDeaths) {
                return country.getmDeaths() != null ? Float.parseFloat(country.getmDeaths()) : 0f;
            }
        } catch (NumberFormatException e) {
            return 0f;
        }
        return 0f;
    }

    private void showBarChart(List<String> labels, List<Float> values) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new BarEntry(i, values.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Countries");
        dataSet.setColors(Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.CYAN, Color.MAGENTA);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Customize X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());
        xAxis.setLabelRotationAngle(-45f);

        // Customize Y-axis
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);

        // Refresh chart
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    private void showLineChart(List<String> labels, List<Float> values) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new Entry(i, values.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Countries");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());
        xAxis.setLabelRotationAngle(-45f);

        // Customize Y-axis
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);

        // Refresh chart
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private void saveComparison() {
        if (selectedCountries.isEmpty()) {
            Toast.makeText(getContext(), "No countries to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save selected country names (not the full objects)
        StringBuilder countryNamesBuilder = new StringBuilder();
        for (CovidCountry country : selectedCountries) {
            if (countryNamesBuilder.length() > 0) {
                countryNamesBuilder.append(",");
            }
            countryNamesBuilder.append(country.getmCovidCountry());
        }
        sharedPreferences.edit().putString(KEY_SELECTED_COUNTRIES, countryNamesBuilder.toString()).apply();

        // Save chart type
        int chartTypeId = radioChartType.getCheckedRadioButtonId();
        sharedPreferences.edit().putInt(KEY_CHART_TYPE, chartTypeId).apply();

        // Save data type
        int dataTypeId = radioDataType.getCheckedRadioButtonId();
        sharedPreferences.edit().putInt(KEY_DATA_TYPE, dataTypeId).apply();

        Toast.makeText(getContext(), R.string.comparison_saved, Toast.LENGTH_SHORT).show();
    }

    private void loadSavedComparison() {
        // Load selected country names
        String savedCountryNames = sharedPreferences.getString(KEY_SELECTED_COUNTRIES, null);
        if (savedCountryNames != null && !savedCountryNames.isEmpty() && dataLoaded) {
            String[] countryNameArray = savedCountryNames.split(",");
            for (String countryName : countryNameArray) {
                for (CovidCountry country : allCountries) {
                    if (country.getmCovidCountry().equals(countryName)) {
                        selectedCountries.add(country);
                        break;
                    }
                }
            }
            selectedCountriesAdapter.notifyDataSetChanged();
            updateSelectedCountriesText();
        }

        // Load chart type
        int chartTypeId = sharedPreferences.getInt(KEY_CHART_TYPE, R.id.radioBarChart);
        radioChartType.check(chartTypeId);

        // Load data type
        int dataTypeId = sharedPreferences.getInt(KEY_DATA_TYPE, R.id.radioTotalCases);
        radioDataType.check(dataTypeId);

        // If there are saved countries, update the chart
        if (!selectedCountries.isEmpty()) {
            Toast.makeText(getContext(), R.string.comparison_loaded, Toast.LENGTH_SHORT).show();
        }
    }
}
