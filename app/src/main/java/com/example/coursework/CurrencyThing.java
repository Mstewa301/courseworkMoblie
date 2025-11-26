package com.example.coursework;

public class CurrencyThing {

    // Raw RSS fields (already used in your parser)
    private String title;
    private String link;
    private String guid;
    private String pubDate;
    private String description;
    private String category;

    // ✅ Derived fields we actually care about for the app
    // (these are what we’ll use later in lists, search, converter, etc.)
    private String currencyCode;   // e.g. "USD"
    private String currencyName;   // e.g. "US Dollar"
    private String countryName;    // e.g. "United States" / "American" etc.
    private double rateToGbp;      // how many of this currency for 1 GBP

    // ==== Getters for raw fields ====
    public String getTitle() { return title; }
    public String getLink() { return link; }
    public String getGuid() { return guid; }
    public String getPubDate() { return pubDate; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }

    // ==== Setters for raw fields ====
    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // ==== Getters for derived fields ====
    public String getCurrencyCode() { return currencyCode; }
    public String getCurrencyName() { return currencyName; }
    public String getCountryName() { return countryName; }
    public double getRateToGbp() { return rateToGbp; }

    /**
     * Call this AFTER all raw fields have been set for one <item>.
     * It pulls out code, name, country and numeric rate from title/description/category.
     */
    public void deriveFieldsFromRaw() {
        if (title == null || description == null) {
            return;
        }

        // --- 1) Currency code ---
        // Best source is usually <category>, but if missing, try from title "GBP/XXX"
        if (category != null && !category.trim().isEmpty()) {
            currencyCode = category.trim();
        } else {
            // fallback: look for "GBP/XXX"
            int slashIndex = title.indexOf("GBP/");
            if (slashIndex != -1 && title.length() >= slashIndex + 7) {
                currencyCode = title.substring(slashIndex + 4, slashIndex + 7).trim();
            }
        }

        // --- 2) Currency name / country (from text in brackets in the title) ---
        // Example: "GBP/USD (US Dollar)" -> "US Dollar"
        String nameFromTitle = null;
        int open = title.indexOf('(');
        int close = title.indexOf(')');
        if (open != -1 && close != -1 && close > open) {
            nameFromTitle = title.substring(open + 1, close).trim();
        }

        currencyName = nameFromTitle != null ? nameFromTitle : "";
        // For now we’ll just duplicate this into countryName – we can improve later
        countryName = currencyName;

        // --- 3) Numeric rate from description ---
        // Typical description: "1 GBP = 1.2345 USD"
        String desc = description.replace(",", "").trim();
        int equalsIndex = desc.indexOf('=');
        if (equalsIndex != -1 && desc.length() > equalsIndex + 1) {
            String afterEquals = desc.substring(equalsIndex + 1).trim(); // "1.2345 USD"
            String[] parts = afterEquals.split("\\s+");
            if (parts.length > 0) {
                try {
                    rateToGbp = Double.parseDouble(parts[0]);
                } catch (NumberFormatException e) {
                    rateToGbp = 0.0;
                }
            }
        }
    }

    // ==== toString for debugging / temporary display ====
    @Override
    public String toString() {
        // Show derived info first – this is what you’ll see in the TextView for now
        return currencyCode + " - " + currencyName +
                "\n1 GBP = " + rateToGbp + " " + currencyCode +
                "\nDate: " + pubDate;
    }

}