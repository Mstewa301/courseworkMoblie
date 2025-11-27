package com.example.coursework;

public class CurrencyThing {

    private String title;
    private String link;
    private String guid;
    private String pubDate;
    private String description;
    private String category;

    private String currencyCode;
    private String currencyName;
    private String countryName;
    private double rateToGbp;

    public String getTitle() { return title; }
    public String getLink() { return link; }
    public String getGuid() { return guid; }
    public String getPubDate() { return pubDate; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }

    public void setTitle(String title) { this.title = title; }
    public void setLink(String link) { this.link = link; }
    public void setGuid(String guid) { this.guid = guid; }
    public void setPubDate(String pubDate) { this.pubDate = pubDate; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }

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

        currencyCode = null;
        int lastOpen = title.lastIndexOf('(');
        int lastClose = title.lastIndexOf(')');
        if (lastOpen != -1 && lastClose != -1 && lastClose > lastOpen) {
            currencyCode = title.substring(lastOpen + 1, lastClose).trim();
        }
        if (currencyCode == null || currencyCode.isEmpty()) {
            currencyCode = "GBP";
        }


        String name = null;
        int slash = title.indexOf('/');
        if (slash != -1 && slash + 1 < title.length()) {
            String right = title.substring(slash + 1).trim();
            int open2 = right.lastIndexOf('(');
            if (open2 != -1) {
                name = right.substring(0, open2).trim();
            } else {
                name = right;
            }
        }

        if (name == null || name.isEmpty()) {
            if (category != null) {
                name = category.trim();
            } else {
                name = "";
            }
        }

        currencyName = name;
        countryName = currencyName;

        rateToGbp = 0.0;
        try {
            String clean = description.replace(",", "");
            String[] split = clean.split("=");
            if (split.length == 2) {
                String right = split[1].trim();
                String firstNum = right.split("\\s+")[0];
                rateToGbp = Double.parseDouble(firstNum);
            }
        } catch (Exception e) {
            rateToGbp = 0.0;
        }
    }
}