package com.evranger.soulevspy.util;

import android.content.Context;
import android.util.Log;

import com.evranger.soulevspy.R;

/**
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-07.
 */
public class KiaVinParser {

    final static String YEARS = "EFGHJKLMNPRSTVWXY123456789";
    final static int FIRST_YEAR = 2014;

    private String mVIN;
    private boolean mIsValid = false;

    private String mBrand;
    private String mModel;
    private String mTrim;
    private String mEngine;
    private String mYear;
    private String mSequentialNumber;
    private String mProductionPlant;


    public KiaVinParser(Context context, String vehicleIdentificationNumber ) {
        if ( vehicleIdentificationNumber == null || vehicleIdentificationNumber.length() != 17 )
        {
            // The string must be 17 characters!
            Log.d("KiaVinParser", "Invalid String!");
            return;
        }

        // Make sure it's in uppercase
        vehicleIdentificationNumber = vehicleIdentificationNumber.toUpperCase();

        // World Manufacturer Identifier (WMI)
        final String wmi = vehicleIdentificationNumber.substring(0, 3);
        if ( wmi.equals("KNA") || wmi.equals("KNC") || wmi.equals("KND") || wmi.equals("KNH")) {
            mBrand = context.getString(R.string.car_kia);

            // Vehicle line (J = Soul)
            final Character vehicleLine = vehicleIdentificationNumber.charAt(3);
            if ( vehicleLine.equals('J')) {
                mModel = context.getString(R.string.car_soulev);
            } else if (vehicleLine.equals('C')) {
                mModel = context.getString(R.string.car_eniro);
            } else {
                mModel = "Not a Soul EV";
                Log.d("KiaVinParser", "Not a Soul EV! " + vehicleLine);
                return;
            }

            // Motor type
            final Character motorType = vehicleIdentificationNumber.charAt(7);
            if (motorType.equals('E')) {
                mEngine = context.getString(R.string.car_engine_e);
            } else if (motorType.equals('1')) {
                mEngine = context.getString(R.string.car_engine_150kw);
            } else if (motorType.equals('H')) {
                mEngine = context.getString(R.string.car_engine_100kw);
            } else {
                mEngine = "Not a Soul EV";
                Log.d("KiaVinParser", "Unrecognized Soul EV motortype! " + motorType);
                return;
            }

            // Model & series
            final Character trim = vehicleIdentificationNumber.charAt(4);
            switch(trim)
            {
                case 'P': // Soul EV 2015
                    mTrim = context.getString(R.string.car_trim_base);
                    break;

                case 'X': // Soul EV 2015
                    mTrim = context.getString(R.string.car_trim_plus);
                    break;

                case '3': // e-Soul 2020
                    mTrim = context.getString(R.string.car_trim_exclusive);
                    break;

                case 'B': // Peters e-Niro
                    mTrim = context.getString(R.string.car_trim_eniro_first_edition_base);
                    break;

                default:
                    mTrim = context.getString(R.string.car_unknown);
                    break;
            }

            // Body/Cabin type, Gross vehicle weight rating (UNUSED)
            // final Character type = vehicleIdentificationNumber.charAt(5);

            // Restraint system, brake system (UNUSED)
            //final Character brakeSystem = vehicleIdentificationNumber.charAt(6);

            // Check digit (UNUSED)
            //final Character checkDigit = vehicleIdentificationNumber.charAt(8);
        } else if (wmi.equals("KMH")) {
            mBrand = context.getString(R.string.car_hyundai);

            // Vehicle line (C = Ioniq EV)
            final Character vehicleLine = vehicleIdentificationNumber.charAt(3);
            if ( vehicleLine.equals('C')) {
                mModel = context.getString(R.string.car_ioniqev);
                // Model & series
                final Character trim = vehicleIdentificationNumber.charAt(4);
                switch(trim)
                {
                    case '6': // Hyundai Ioniq PHEV SE
                        mModel = context.getString(R.string.car_ioniqphev);
                        mTrim = context.getString(R.string.car_trim_ioniq_se);
                        break;

                    case '7': // Kona Trend
                        mTrim = context.getString(R.string.car_trim_ioniq_trend);
                        break;

                    case '8': // Hyundai Ioniq HEV
                        mModel = context.getString(R.string.car_ioniqhev);
                        mTrim = context.getString(R.string.car_trim_ioniq_se);
                        break;

                    default:
                        mTrim = context.getString(R.string.car_unknown);
                        break;
                }
            } else if (vehicleLine.equals('K')) {
                mModel = context.getString(R.string.car_konaev);
                // Model & series
                final Character trim = vehicleIdentificationNumber.charAt(4);
                switch(trim)
                {
                    case '7': // Kona exclusive
                        mTrim = context.getString(R.string.car_trim_exclusive);
                        break;

                    case '3': // Kona Trend
                        mTrim = context.getString(R.string.car_trim_ioniq_trend);
                        break;

                    default:
                        mTrim = context.getString(R.string.car_unknown);
                        break;
                }
            } else {
                mModel = "Not a known Hyundai EV";
                Log.d("KiaVinParser", "Not a known Hyundai EV! " + vehicleLine);
                return;
            }

            // Motor type
            final Character motorType = vehicleIdentificationNumber.charAt(7);
            if (motorType.equals('H')) {
                mEngine = context.getString(R.string.car_engine_ioniq_88kW);
            } else if (motorType.equals('G')) {
                mEngine = context.getString(R.string.car_engine_150kw);
            } else if (motorType.equals('D')) {
                mEngine = context.getString(R.string.car_engine_ioniq_phev_45kW);
            } else if (motorType.equals('C')) {
                mEngine = context.getString(R.string.car_engine_ioniq_hev_32kW);
            } else {
                mEngine = "Unrecognized Hyundai Electric motortype";
                Log.d("KiaVinParser", "Unrecognized Hyundai Electric motortype! " + motorType);
                return;
            }

        } else {
            // Not a HKMC vehicle!
            mBrand = "Not a Kia or a Hyundai";
            Log.d("KiaVinParser", "Not a Kia or Hyundai! " + wmi);
            return;
        }

        // At this point, we are sure that it's a known make!
        mVIN = vehicleIdentificationNumber;
        mIsValid = true;

        // Model year
        final Character year = vehicleIdentificationNumber.charAt(9);
        final int yearValue = YEARS.indexOf(year);
        if ( yearValue != -1 )
        {
            mYear = Integer.toString(yearValue + FIRST_YEAR);
        } else {
            mYear = context.getString(R.string.car_unknown);
        }

        // Production plant
        final Character plant = vehicleIdentificationNumber.charAt(10);
        switch (plant)
        {
            case '5':
                mProductionPlant = context.getString(R.string.car_plant_hwaseong) + " (" + context.getString(R.string.car_south_korea) + ')';
                break;
            case '6':
                mProductionPlant = context.getString(R.string.car_plant_soha_ri) + " (" + context.getString(R.string.car_south_korea) + ')';
                break;
            case '7':
                mProductionPlant = context.getString(R.string.car_plant_gwangju) + " (" + context.getString(R.string.car_south_korea) + ')';
                break;
            case 'T':
                mProductionPlant = context.getString(R.string.car_plant_seosan) + " (" + context.getString(R.string.car_south_korea) + ')';
                break;
            case 'U':
                mProductionPlant = context.getString(R.string.car_plant_ulsan) + " (" + context.getString(R.string.car_south_korea) + ')';
                break;
            default:
                mProductionPlant = context.getString(R.string.car_unknown) + " (" + plant + ')';
                break;
        }

        // Vehicle production sequence number
        mSequentialNumber = vehicleIdentificationNumber.substring(11, 17);
    }

    /**
     *
     * @return
     */
    public boolean isValid() { return mIsValid; }

    public String getVIN() { return mVIN; }
    // Manufacturer
    public String getBrand() { return mBrand; }
    public String getModel() { return mModel; }
    public String getTrim() { return mTrim; }
    public String getEngine() { return mEngine; }
    public String getYear() { return mYear; }
    public String getSequentialNumber() { return mSequentialNumber; }
    public String getProductionPlant() { return mProductionPlant; }

}
