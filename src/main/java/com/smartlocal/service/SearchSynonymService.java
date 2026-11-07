package com.smartlocal.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service providing rule-based synonym expansion for AI-assisted customer search.
 * NOTE: This is a rule-based dictionary implementation, not a trained machine-learning model.
 */
public interface SearchSynonymService {

    /**
     * Expands an input query into a set of searchable terms using predefined synonyms.
     *
     * @param rawQuery the raw user search keyword
     * @return list of search tokens (original token plus expanded synonyms)
     */
    List<String> getSearchTokens(String rawQuery);

    /**
     * Returns an explanation message if a synonym rule was triggered for the given query.
     *
     * @param rawQuery the raw user search keyword
     * @return Optional containing transparency explanation of the synonym expansion
     */
    Optional<String> getSynonymExplanation(String rawQuery);

    /**
     * Returns the full rule-based dictionary map for transparency and auditing.
     *
     * @return map of canonical term to list of recognized synonyms
     */
    Map<String, List<String>> getSynonymDictionary();
}
