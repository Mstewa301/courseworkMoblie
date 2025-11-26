package com.example.coursework;

/*  Starter project for Mobile Platform Development - 1st diet 25/26
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 Matthew Stewart
// Student ID           S2216188
// Programme of Study   Mobile PlatForm Development
//

// UPDATE THE PACKAGE NAME to include your Student Identifier

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private TextView rawDataDisplay;
    private Button startButton;
    private ListView currencyListView;
    private EditText searchEditText;
    private String result = "";
    private String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";
    private ArrayList<CurrencyThing> allItems;
    private ArrayList<CurrencyThing> displayItems;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL_MS = 10 * 60 * 1000;
    private TextView usdValueText;
    private TextView eurValueText;
    private TextView jpyValueText;
    private CurrencyAdapter adapter;
    private LinearLayout usdRow;
    private LinearLayout eurRow;
    private LinearLayout jpyRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link UI components
        rawDataDisplay = findViewById(R.id.rawDataDisplay);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        currencyListView = findViewById(R.id.currencyListView);
        searchEditText = findViewById(R.id.searchEditText);

        usdValueText = findViewById(R.id.usdValueText);
        eurValueText = findViewById(R.id.eurValueText);
        jpyValueText = findViewById(R.id.jpyValueText);

        usdRow = findViewById(R.id.usdRow);
        eurRow = findViewById(R.id.eurRow);
        jpyRow = findViewById(R.id.jpyRow);

        usdRow.setOnClickListener(v -> {
            CurrencyThing usd = findCurrencyByCode("USD");
            if (usd != null) {
                showConvertDialog(usd);
            } else {
                Toast.makeText(MainActivity.this, "USD rate not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });

        eurRow.setOnClickListener(v -> {
            CurrencyThing eur = findCurrencyByCode("EUR");
            if (eur != null) {
                showConvertDialog(eur);
            } else {
                Toast.makeText(MainActivity.this, "EUR rate not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });

        jpyRow.setOnClickListener(v -> {
            CurrencyThing jpy = findCurrencyByCode("JPY");
            if (jpy != null) {
                showConvertDialog(jpy);
            } else {
                Toast.makeText(MainActivity.this, "JPY rate not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });
        allItems = new ArrayList<>();
        displayItems = new ArrayList<>();

        // Set up adapter using displayItems (this is what the ListView shows)
        adapter = new CurrencyAdapter(this, displayItems);
        currencyListView.setAdapter(adapter);

        // Handle clicks on list items → open converter dialog
        currencyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CurrencyThing clicked = (CurrencyThing) parent.getItemAtPosition(position);
                showConvertDialog(clicked);
            }
        });

        // Search behaviour – filter as user types
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCurrencies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                startProgress(); // refresh now
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS); // schedule next
            }
        };

    }
    protected void onResume() {
        super.onResume();
        // Start auto-refresh when activity becomes visible
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-refresh when activity is no longer in foreground
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
    @Override
    public void onClick(View v) {
        startProgress();
    }

    public void startProgress() {
        new Thread(new Task(urlSource)).start();
    }

    // Background thread to fetch RSS
    private class Task implements Runnable {
        private String url;

        public Task(String aurl) {
            url = aurl;
        }

        @Override
        public void run() {
            URL aurl;
            URLConnection yc;
            BufferedReader in;
            String inputLine;

            result = ""; // reset

            try {
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                while ((inputLine = in.readLine()) != null) {
                    result += inputLine;
                }
                in.close();
            } catch (IOException e) {
                Log.e("MyTask", "IO Exception: " + e);
            }

            if (result.isEmpty()) return;

            // Clean leading/trailing garbage
            int i = result.indexOf("<?");
            if (i >= 0) result = result.substring(i);

            i = result.indexOf("</rss>");
            if (i >= 0) result = result.substring(0, i + 6);

            // PARSE XML
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(result));

                int eventType = xpp.getEventType();
                CurrencyThing item = null;
                boolean insideItem = false;

                allItems.clear();

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_TAG) {
                        String tag = xpp.getName();

                        if (tag.equalsIgnoreCase("item")) {
                            insideItem = true;
                            item = new CurrencyThing();

                        } else if (insideItem && tag.equalsIgnoreCase("title")) {
                            item.setTitle(xpp.nextText());

                        } else if (insideItem && tag.equalsIgnoreCase("link")) {
                            item.setLink(xpp.nextText());

                        } else if (insideItem && tag.equalsIgnoreCase("guid")) {
                            item.setGuid(xpp.nextText());

                        } else if (insideItem && tag.equalsIgnoreCase("pubDate")) {
                            item.setPubDate(xpp.nextText());

                        } else if (insideItem && tag.equalsIgnoreCase("description")) {
                            item.setDescription(xpp.nextText());

                        } else if (insideItem && tag.equalsIgnoreCase("category")) {
                            item.setCategory(xpp.nextText());
                        }

                    } else if (eventType == XmlPullParser.END_TAG) {

                        if (xpp.getName().equalsIgnoreCase("item")) {
                            if (item != null) {
                                item.deriveFieldsFromRaw();
                                allItems.add(item);
                            }
                            insideItem = false;
                        }
                    }

                    eventType = xpp.next();
                }

            } catch (XmlPullParserException | IOException e) {
                Log.e("Parsing", "Error: " + e);
            }

            // Update UI
            MainActivity.this.runOnUiThread(() -> {
                // copy full list into displayItems (initial no-filter state)
                displayItems.clear();
                displayItems.addAll(allItems);
                adapter.notifyDataSetChanged();
                updateTopThreePanel();
            });
        }
    }
    private void showConvertDialog(CurrencyThing currency) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Convert GBP ↔ " + currency.getCurrencyCode());

        // Container to hold both input boxes vertically
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        // Input for GBP amount
        final EditText gbpInput = new EditText(MainActivity.this);
        gbpInput.setHint("Amount in GBP");
        gbpInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(gbpInput);

        // Input for foreign currency amount
        final EditText curInput = new EditText(MainActivity.this);
        curInput.setHint("Amount in " + currency.getCurrencyCode());
        curInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(curInput);

        builder.setView(layout);

        builder.setPositiveButton("Convert", (dialog, which) -> {
            String gbpText = gbpInput.getText().toString().trim();
            String curText = curInput.getText().toString().trim();

            double rate = currency.getRateToGbp();   // 1 GBP = rate * currency

            // If user entered GBP, convert GBP -> currency
            if (!gbpText.isEmpty()) {
                double gbpAmount;
                try {
                    gbpAmount = Double.parseDouble(gbpText);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Invalid GBP amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (gbpAmount < 0) {
                    Toast.makeText(MainActivity.this, "Amount cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
                double converted = gbpAmount * rate;
                String msg = String.format("%.2f GBP = %.2f %s",
                        gbpAmount, converted, currency.getCurrencyCode());
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                return;
            }

            // Else if user entered foreign currency, convert currency -> GBP
            if (!curText.isEmpty()) {
                double curAmount;
                try {
                    curAmount = Double.parseDouble(curText);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Invalid " + currency.getCurrencyCode() + " amount",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (curAmount < 0) {
                    Toast.makeText(MainActivity.this, "Amount cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
                // reverse conversion: GBP = foreign / rate
                double gbpAmount = curAmount / rate;
                String msg = String.format("%.2f %s = %.2f GBP",
                        curAmount, currency.getCurrencyCode(), gbpAmount);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                return;
            }

            // Neither box filled
            Toast.makeText(MainActivity.this, "Enter an amount in one box", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void filterCurrencies(String query) {
        if (allItems == null || adapter == null) {
            return;
        }

        String lower = query.toLowerCase().trim();

        displayItems.clear();

        if (lower.isEmpty()) {
            // show everything if search is empty
            displayItems.addAll(allItems);
        } else {
            for (CurrencyThing c : allItems) {
                String code = c.getCurrencyCode() != null ? c.getCurrencyCode().toLowerCase() : "";
                String name = c.getCurrencyName() != null ? c.getCurrencyName().toLowerCase() : "";
                String country = c.getCountryName() != null ? c.getCountryName().toLowerCase() : "";

                if (code.contains(lower) || name.contains(lower) || country.contains(lower)) {
                    displayItems.add(c);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
    private CurrencyThing findCurrencyByCode(String code) {
        if (allItems == null) return null;

        for (CurrencyThing c : allItems) {
            if (c.getCurrencyCode() != null &&
                    c.getCurrencyCode().equalsIgnoreCase(code)) {
                return c;
            }
        }
        return null;
    }

    private void updateTopThreePanel() {
        if (usdValueText == null || eurValueText == null || jpyValueText == null) {
            return; // views not ready
        }
        CurrencyThing usd = findCurrencyByCode("USD");
        CurrencyThing eur = findCurrencyByCode("EUR");
        CurrencyThing jpy = findCurrencyByCode("JPY");

        if (usd != null) {
            usdValueText.setText(String.format("%.3f", usd.getRateToGbp()));
        } else {
            usdValueText.setText("N/A");
        }

        if (eur != null) {
            eurValueText.setText(String.format("%.3f", eur.getRateToGbp()));
        } else {
            eurValueText.setText("N/A");
        }

        if (jpy != null) {
            jpyValueText.setText(String.format("%.3f", jpy.getRateToGbp()));
        } else {
            jpyValueText.setText("N/A");
        }
    }
}