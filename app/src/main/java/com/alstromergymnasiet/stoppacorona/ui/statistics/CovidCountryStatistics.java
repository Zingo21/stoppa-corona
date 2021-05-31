package com.alstromergymnasiet.stoppacorona.ui.statistics;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.fragment.app.FragmentActivity;

import java.util.List;

public class CovidCountryStatistics implements Parcelable {
    String mCovidCountry, mTotalCases, mFlags;
    int mTodayCases;

    public CovidCountryStatistics(String mCovidCountry, int mTodayCases, String todayCases, String deaths, String todayDeaths, String recovered, String active, String mTotalCases, String mFlags){
        this.mCovidCountry = mCovidCountry;
        this.mTodayCases = mTodayCases;
        this.mTotalCases = mTotalCases;
        this.mFlags = mFlags;
    }

    public CovidCountryStatistics(List<CovidCountryStatistics> covidCountriesStatistics, FragmentActivity activity) {
    }

    public String getmCovidCountry(){return mCovidCountry;}

    public int getmTodayCases(){return mTodayCases;}

    public String getmTotalCases(){return mTotalCases;}

    public String getmFlags(){return mFlags;}


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mCovidCountry);
        dest.writeInt(this.mTodayCases);
        dest.writeString(this.mTotalCases);
        dest.writeString(this.mFlags);
    }

    protected CovidCountryStatistics(Parcel in){
        this.mCovidCountry = in.readString();
        this.mTodayCases = in.readInt();
        this.mTotalCases = in.readString();
        this.mFlags = in.readString();
    }

    public static final Creator<CovidCountryStatistics> CREATOR = new Creator<CovidCountryStatistics>() {
        @Override
        public CovidCountryStatistics createFromParcel(Parcel source) {
            return new CovidCountryStatistics(source);
        }

        @Override
        public CovidCountryStatistics[] newArray(int size) {
            return new CovidCountryStatistics[size];
        }
    };


}
