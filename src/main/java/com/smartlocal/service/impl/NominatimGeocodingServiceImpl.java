package com.smartlocal.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlocal.dto.GeocodingResultDto;
import com.smartlocal.service.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GeocodingService implementation using OpenStreetMap Nominatim with
 * response caching, custom User-Agent, and graceful fallback.
 */
@Service
public class NominatimGeocodingServiceImpl implements GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeocodingServiceImpl.class);

    @Value("${app.geocoding.nominatim.url:https://nominatim.openstreetmap.org}")
    private String nominatimBaseUrl;

    @Value("${app.geocoding.user-agent:SmartLocal-Hyperlocal/1.0 (contact@smartlocal.com)}")
    private String userAgent;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // In-memory cache to respect Nominatim rate-limits and avoid duplicate HTTP requests
    private final Map<String, List<GeocodingResultDto>> searchCache = new ConcurrentHashMap<>();
    private final Map<String, String> reverseCache = new ConcurrentHashMap<>();

    // Curated local knowledge base for high-accuracy local Pune landmarks
    private static final List<GeocodingResultDto> LOCAL_PUNE_PLACES = List.of(
            new GeocodingResultDto("FC Road (Fergusson College Road), Deccan Gymkhana, Pune, Maharashtra", "FC Road", "F.C. Road", "Pune", 18.5204, 73.8436),
            new GeocodingResultDto("Deccan Gymkhana, Pune, Maharashtra", "Deccan Gymkhana", "Deccan Gymkhana", "Pune", 18.5175, 73.8415),
            new GeocodingResultDto("JM Road (Jangali Maharaj Road), Shivajinagar, Pune, Maharashtra", "JM Road", "J.M. Road", "Pune", 18.5246, 73.8505),
            new GeocodingResultDto("Shivajinagar, Pune, Maharashtra", "Shivajinagar", "Shivajinagar", "Pune", 18.5314, 73.8446),
            new GeocodingResultDto("Kothrud, Pune, Maharashtra", "Kothrud", "Kothrud", "Pune", 18.5074, 73.8077),
            new GeocodingResultDto("Viman Nagar, Nagar Road, Pune, Maharashtra", "Viman Nagar", "Viman Nagar", "Pune", 18.5679, 73.9143),
            new GeocodingResultDto("Kalyani Nagar, Pune, Maharashtra", "Kalyani Nagar", "Kalyani Nagar", "Pune", 18.5480, 73.9030),
            new GeocodingResultDto("Baner, Pune, Maharashtra", "Baner", "Baner", "Pune", 18.5590, 73.7868),
            new GeocodingResultDto("Aundh, Pune, Maharashtra", "Aundh", "Aundh", "Pune", 18.5580, 73.8075),
            new GeocodingResultDto("Swargate, Pune, Maharashtra", "Swargate", "Swargate", "Pune", 18.5018, 73.8586),
            new GeocodingResultDto("Pune Camp (MG Road), Pune, Maharashtra", "Camp / MG Road", "Pune Camp", "Pune", 18.5186, 73.8794),
            new GeocodingResultDto("Hadapsar (Magarpatta), Pune, Maharashtra", "Hadapsar", "Hadapsar", "Pune", 18.5089, 73.9260),
            new GeocodingResultDto("Appa Balwant Chowk, Budhwar Peth, Pune, Maharashtra", "Appa Balwant Chowk", "Appa Balwant Chowk", "Pune", 18.5162, 73.8558),
            new GeocodingResultDto("Kumthekar Road, Sadashiv Peth, Pune, Maharashtra", "Kumthekar Road", "Sadashiv Peth", "Pune", 18.5190, 73.8530)
    );

    public NominatimGeocodingServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public List<GeocodingResultDto> searchLocations(String query) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        String normalizedQuery = query.trim().toLowerCase();

        // 1. Check cache
        if (searchCache.containsKey(normalizedQuery)) {
            return searchCache.get(normalizedQuery);
        }

        List<GeocodingResultDto> results = new ArrayList<>();

        // 2. Query OpenStreetMap Nominatim API
        try {
            String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = nominatimBaseUrl + "/search?q=" + encodedQuery + "&format=json&addressdetails=1&limit=5";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    for (JsonNode item : root) {
                        double lat = item.path("lat").asDouble();
                        double lon = item.path("lon").asDouble();
                        String displayName = item.path("display_name").asText();
                        String name = item.path("name").asText(displayName);

                        JsonNode addressNode = item.path("address");
                        String locality = addressNode.path("suburb").asText(
                                addressNode.path("neighbourhood").asText(
                                        addressNode.path("road").asText(name)));
                        String city = addressNode.path("city").asText(
                                addressNode.path("town").asText(
                                        addressNode.path("county").asText("Pune")));

                        results.add(new GeocodingResultDto(displayName, name, locality, city, lat, lon));
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Nominatim geocoding lookup failed for query [{}]: {}", query, ex.getMessage());
        }

        // 3. Match against local curated places
        for (GeocodingResultDto place : LOCAL_PUNE_PLACES) {
            if (place.getName().toLowerCase().contains(normalizedQuery)
                    || place.getDisplayName().toLowerCase().contains(normalizedQuery)
                    || place.getLocality().toLowerCase().contains(normalizedQuery)
                    || normalizedQuery.contains(place.getName().toLowerCase())) {
                boolean alreadyPresent = results.stream().anyMatch(r ->
                        Math.abs(r.getLatitude() - place.getLatitude()) < 0.005
                        && Math.abs(r.getLongitude() - place.getLongitude()) < 0.005);
                if (!alreadyPresent) {
                    results.add(place);
                }
            }
        }

        // Prioritize Pune / Maharashtra results
        results.sort((a, b) -> {
            boolean aPune = (a.getDisplayName() != null && a.getDisplayName().toLowerCase().contains("pune"))
                    || (a.getCity() != null && a.getCity().toLowerCase().contains("pune"));
            boolean bPune = (b.getDisplayName() != null && b.getDisplayName().toLowerCase().contains("pune"))
                    || (b.getCity() != null && b.getCity().toLowerCase().contains("pune"));
            return Boolean.compare(bPune, aPune);
        });

        searchCache.put(normalizedQuery, results);
        return results;
    }

    @Override
    public Optional<String> reverseGeocode(double latitude, double longitude) {
        String cacheKey = String.format(Locale.US, "%.4f,%.4f", latitude, longitude);
        if (reverseCache.containsKey(cacheKey)) {
            return Optional.of(reverseCache.get(cacheKey));
        }

        // 1. Try real Nominatim reverse lookup
        try {
            String url = String.format(Locale.US, "%s/reverse?lat=%.6f&lon=%.6f&format=json&addressdetails=1",
                    nominatimBaseUrl, latitude, longitude);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode address = root.path("address");

                String road = address.path("road").asText("");
                String suburb = address.path("suburb").asText(address.path("neighbourhood").asText(""));
                String city = address.path("city").asText(address.path("town").asText("Pune"));

                StringBuilder sb = new StringBuilder();
                if (!road.isBlank()) sb.append(road).append(", ");
                if (!suburb.isBlank()) sb.append(suburb).append(", ");
                sb.append(city);

                String formatted = sb.toString().trim();
                if (!formatted.isBlank()) {
                    reverseCache.put(cacheKey, formatted);
                    return Optional.of(formatted);
                }
            }
        } catch (Exception ex) {
            log.warn("Nominatim reverse geocode failed for [{}, {}]: {}", latitude, longitude, ex.getMessage());
        }

        // 2. Fallback to nearest local place
        GeocodingResultDto nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (GeocodingResultDto place : LOCAL_PUNE_PLACES) {
            double d = haversineKm(latitude, longitude, place.getLatitude(), place.getLongitude());
            if (d < minDistance) {
                minDistance = d;
                nearest = place;
            }
        }

        if (nearest != null) {
            String label = nearest.getName() + " Area, Pune (" + String.format(Locale.US, "%.4f° N, %.4f° E", latitude, longitude) + ")";
            reverseCache.put(cacheKey, label);
            return Optional.of(label);
        }

        return Optional.of(String.format(Locale.US, "%.4f° N, %.4f° E", latitude, longitude));
    }

    @Override
    public Optional<GeocodingResultDto> geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return Optional.empty();
        }
        List<GeocodingResultDto> list = searchLocations(address);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
