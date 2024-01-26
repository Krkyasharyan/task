package com.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Data {
    private List<Flight> flights;
    private Map<String, List<Weather>> forecast;
}


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Flight {
    String no;
    int departure;
    String from;
    String to;
    int duration;
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class Weather {
    int time;
    int wind;
    int visibility;
}

class Main {
    public static void main(String[] args) {
        Map<String, Integer> gmtOffsets = new HashMap<>();

        gmtOffsets.put("moscow", 3);
        gmtOffsets.put("novosibirsk", 7);
        gmtOffsets.put("omsk", 6);

        Data flightData = parseData();
        List<Flight> flights;
        Map<String, List<Weather>> weatherByCity;

        if (flightData != null) {
            flights = flightData.getFlights();
            weatherByCity = flightData.getForecast();

            for (Flight flight : flights) {
                String status = checkFlightStatus(flight, weatherByCity, gmtOffsets);
                System.out.println(flight.getNo() + " | " + flight.getFrom() + " -> " + flight.getTo() + " | " + status + "\n");
            }
        }
    }

    static String checkFlightStatus(Flight flight, Map<String, List<Weather>> weather, Map<String, Integer> gmtOffsets) {
        int departureTime = flight.getDeparture();
        int arrivalTime = (departureTime + flight.getDuration() + (gmtOffsets.get(flight.getTo()) - gmtOffsets.get(flight.getFrom()))) % 24;

        Weather depWeather = weather.get(flight.getFrom()).get(departureTime);
        Weather arrWeather = weather.get(flight.getTo()).get(arrivalTime);
        if (!isGoodWeather(depWeather) || !isGoodWeather(arrWeather)) {
            return "отменен";
        }

        return "по расписанию";
    }

    static Data parseData() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = Main.class.getResourceAsStream("/flights_and_forecast.json")) {
            return mapper.readValue(is, Data.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean isGoodWeather(Weather weather) {
        return weather.getWind() <= 30 && weather.getVisibility() >= 200;
    }
}
