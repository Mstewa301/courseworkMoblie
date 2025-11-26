package com.example.coursework;

public class CurrencyThing {

    // Raw RSS fields
    private String title;
    private String link;
    private String guid;
    private String pubDate;
    private String description;
    private String category;

    // Derived fields
    private String currencyCode;   // e.g. "USD"
    private String currencyName;   // e.g. "US Dollar"
    private String countryName;    // e.g. "United States"
    private double rateToGbp;      // how many of this currency for 1 GBP

    // ==== Getters for raw fields ====
    public String getTitle() { return title; }
    public String getLink() { return link; }
    public String getGuid() { return guid; }
    public String getPubDate() { return pubDate; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }

    // ==== Setters for raw fields ====
    public void setTitle(String title) { this.title = title; }
    public void setLink(String link) { this.link = link; }
    public void setGuid(String guid) { this.guid = guid; }
    public void setPubDate(String pubDate) { this.pubDate = pubDate; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }

    // ==== Getters for derived fields ====
    public String getCurrencyCode() { return currencyCode; }
    public String getCurrencyName() { return currencyName; }
    public String getCountryName() { return countryName; }
    public double getRateToGbp() { return rateToGbp; }

    /**
     * Call this AFTER all raw RSS fields have been set for one <item>.
     * It extracts: currency code, name, country and numeric rate.
     *
     * The feed items look roughly like:
     *
     *   <title>British Pound Sterling (GBP)/Japan Yen(JPY)</title>
     *   <description>1 British Pound Sterling = 200.0 Japanese Yen</description>
     *   <category>Japan Yen</category>
     */
    public void deriveFieldsFromRaw() {

        if (title == null || description == null) {
            return;
        }

        // ---------- 1) Extract currency code ----------
        // Title: "British Pound Sterling(GBP)/Japan Yen(JPY)"
        currencyCode = null;
        int lastOpen = title.lastIndexOf('(');
        int lastClose = title.lastIndexOf(')');
        if (lastOpen != -1 && lastClose != -1 && lastClose > lastOpen) {
            currencyCode = title.substring(lastOpen + 1, lastClose).trim();  // "JPY"
        }
        if (currencyCode == null || currencyCode.isEmpty()) {
            currencyCode = "GBP"; // safe fallback
        }

        // ---------- 2) Extract currency / country name ----------
        // We want the part after "/" and before the last "(".
        // Example: "British Pound Sterling(GBP)/Japan Yen(JPY)"
        String name = null;
        int slash = title.indexOf('/');
        if (slash != -1 && slash + 1 < title.length()) {
            // right side: "Japan Yen(JPY)"
            String right = title.substring(slash + 1).trim();
            int open2 = right.lastIndexOf('(');
            if (open2 != -1) {
                name = right.substring(0, open2).trim();   // "Japan Yen"
            } else {
                name = right;
            }
        }

        // If we still didn't get a name, fall back to category or empty string
        if (name == null || name.isEmpty()) {
            if (category != null) {
                name = category.trim();
            } else {
                name = "";
            }
        }

        currencyName = name;
        countryName = currencyName;  // so search() can match country/currency words

        // ---------- 3) Rate from description ----------
        // Example: "1 British Pound Sterling = 205.928 Japan Yen"
        rateToGbp = 0.0;
        try {
            String clean = description.replace(",", "");
            String[] split = clean.split("=");
            if (split.length == 2) {
                String right = split[1].trim();           // "205.928 Japan Yen"
                String firstNum = right.split("\\s+")[0]; // "205.928"
                rateToGbp = Double.parseDouble(firstNum);
            }
        } catch (Exception e) {
            rateToGbp = 0.0;
        }
    }
}