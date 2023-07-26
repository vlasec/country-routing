package cz.vlasec.countryrouting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryData(@JsonProperty("cca3") String cca3, @JsonProperty("borders") List<String> borders) { }
