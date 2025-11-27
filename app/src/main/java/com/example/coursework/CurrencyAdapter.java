package com.example.coursework;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.List;
import androidx.core.content.ContextCompat;
import java.util.Locale;

public class CurrencyAdapter extends ArrayAdapter<CurrencyThing> {

    public CurrencyAdapter(Context context, List<CurrencyThing> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CurrencyThing item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.currency_row, parent, false);
        }

        TextView codeText = convertView.findViewById(R.id.codeText);
        TextView rateText = convertView.findViewById(R.id.rateText);
        TextView nameText = convertView.findViewById(R.id.nameText);
        ImageView flagImage = convertView.findViewById(R.id.flagImage);

        codeText.setText(item.getCurrencyCode());
        rateText.setText(String.format("%.2f per GBP", item.getRateToGbp()));
        nameText.setText(item.getCurrencyName());
        flagImage.setImageResource(getFlagForCurrency(item.getCurrencyCode()));

        int bgColour = getColourForRate(item.getRateToGbp());
        convertView.setBackgroundColor(bgColour);

        return convertView;
    }

    private int getColourForRate(double rate) {
        if (rate < 1.5) {
            return ContextCompat.getColor(getContext(), R.color.rate_very_weak);
        } else if (rate < 5) {
            return ContextCompat.getColor(getContext(), R.color.rate_weak);
        } else if (rate < 50) {
            return ContextCompat.getColor(getContext(), R.color.rate_strong);
        } else {
            return ContextCompat.getColor(getContext(), R.color.rate_very_strong);
        }
    }

    private int getFlagForCurrency(String code) {
        if (code == null || code.length() < 2) {
            return R.drawable.gb;
        }

        String countryCode;
        switch (code.toUpperCase(Locale.ROOT)) {
            case "EUR":
                countryCode = "eu";
                break;
            case "GBP":
                countryCode = "gb";
                break;
            case "USD":
                countryCode = "us";
                break;
            case "AUD":
                countryCode = "au";
                break;
            case "NZD":
                countryCode = "nz";
                break;
            case "CAD":
                countryCode = "ca";
                break;
            case "CHF":
                countryCode = "ch";
                break;
            case "JPY":
                countryCode = "jp";
                break;
            case "CNY":
                countryCode = "cn";
                break;

            default:
                countryCode = code.substring(0, 2).toLowerCase(Locale.ROOT);
                break;
        }

        int resId = getContext()
                .getResources()
                .getIdentifier(countryCode, "drawable", getContext().getPackageName());

        if (resId == 0) {
            return R.drawable.gb;
        } else {
            return resId;
        }
    }
}

