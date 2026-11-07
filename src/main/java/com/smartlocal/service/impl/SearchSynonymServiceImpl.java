package com.smartlocal.service.impl;

import com.smartlocal.service.SearchSynonymService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of SearchSynonymService using an in-memory rule-based synonym dictionary.
 *
 * TRANSPARENCY NOTICE:
 * This rule-based synonym dictionary provides deterministic keyword expansion for
 * demonstration and user convenience. It is NOT a trained machine-learning model.
 */
@Service
public class SearchSynonymServiceImpl implements SearchSynonymService {

    // Maps canonical product keyword -> list of user synonym phrases
    private final Map<String, List<String>> dictionary = new LinkedHashMap<>();

    public SearchSynonymServiceImpl() {
        initDictionary();
    }

    private void initDictionary() {
        // Tech & Electronics
        dictionary.put("Mouse", Arrays.asList("pointer", "clicker", "optical mouse", "trackpad", "cordless mouse"));
        dictionary.put("Cable", Arrays.asList("wire", "charger", "cord", "lead", "type-c", "type c", "usbc", "fast charging", "charging cable"));
        dictionary.put("Keyboard", Arrays.asList("keypad", "keys", "typewriter", "gaming keyboard", "mechanical keyboard"));

        // Groceries & Essentials
        dictionary.put("Atta", Arrays.asList("wheat", "flour", "gehu", "wheat flour", "sharbati", "chakki", "aashirvaad"));
        dictionary.put("Rice", Arrays.asList("chawal", "grain", "paddy", "basmati", "white rice", "brown rice", "fortune"));
        dictionary.put("Mustard Oil", Arrays.asList("oil", "tel", "mustard oil", "cooking oil", "sarson", "kachi ghani"));
        dictionary.put("Sunflower Cooking Oil", Arrays.asList("oil", "tel", "cooking oil", "sunflower oil", "refined oil", "edible oil"));
        dictionary.put("Organic Farm Fresh Eggs", Arrays.asList("egg", "eggs", "ande", "anda", "poultry", "brown eggs"));
        dictionary.put("Mango", Arrays.asList("aam", "mango pulp", "alphonso", "fruit pulp", "tin"));

        // Dairy & Beverages
        dictionary.put("Milk", Arrays.asList("doodh", "dairy", "toned milk", "fresh milk", "amul", "taaza", "cow milk"));
        dictionary.put("Ghee", Arrays.asList("tup", "desi ghee", "buffalo ghee", "clarified butter", "pure ghee"));
        dictionary.put("Paneer", Arrays.asList("cottage cheese", "fresh paneer", "malai paneer", "cheese"));
        dictionary.put("Coffee", Arrays.asList("filter coffee", "kaapi", "coffee powder", "brew"));

        // Healthcare & Pharmacy
        dictionary.put("Paracetamol", Arrays.asList("painkiller", "fever tablet", "crocin", "dolo", "calpol", "fever medicine", "headache", "pills", "paracitamol"));
        dictionary.put("Thermometer", Arrays.asList("temp gun", "temperature check", "fever check", "scanner", "digital thermometer", "temp"));
        dictionary.put("First Aid Antiseptic", Arrays.asList("antiseptic", "dettol", "savlon", "disinfectant", "wound cleaner", "antiseptic liquid"));

        // Bakery & Confectionery
        dictionary.put("Bread", Arrays.asList("pav", "loaf", "bun", "toast", "sourdough", "multigrain", "wheat bread", "bakery bread"));
        dictionary.put("Croissant", Arrays.asList("pastry", "puff", "roll", "butter croissant", "french pastry"));
    }

    @Override
    public List<String> getSearchTokens(String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String cleaned = rawQuery.trim().toLowerCase();
        Set<String> tokens = new LinkedHashSet<>();
        tokens.add(cleaned); // Always keep the raw user term

        // Scan dictionary for matching synonyms
        for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
            String canonical = entry.getKey().toLowerCase();
            List<String> synonyms = entry.getValue();

            // Check if user query matches the canonical term
            if (canonical.contains(cleaned) || cleaned.contains(canonical)) {
                tokens.add(canonical);
            }

            // Check if user query matches any synonym phrase
            for (String syn : synonyms) {
                if (syn.equalsIgnoreCase(cleaned) || cleaned.contains(syn.toLowerCase()) || syn.toLowerCase().contains(cleaned)) {
                    tokens.add(canonical);
                    tokens.add(syn.toLowerCase());
                    break;
                }
            }
        }

        return new ArrayList<>(tokens);
    }

    @Override
    public Optional<String> getSynonymExplanation(String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return Optional.empty();
        }

        String cleaned = rawQuery.trim().toLowerCase();
        for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
            String canonical = entry.getKey();
            for (String syn : entry.getValue()) {
                if (syn.equalsIgnoreCase(cleaned) || cleaned.contains(syn.toLowerCase())) {
                    return Optional.of(String.format("AI-Assisted Synonym Expansion: \"%s\" mapped to \"%s\" (Rule-Based Dictionary — Not a trained ML model)",
                            rawQuery.trim(), canonical));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Map<String, List<String>> getSynonymDictionary() {
        return Collections.unmodifiableMap(dictionary);
    }
}
