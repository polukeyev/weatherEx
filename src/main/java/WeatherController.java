import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WeatherController {

    public static Map<String, Object> json2Map(String json) {
        Map<String, Object> map = new Gson().fromJson(
                json, new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );
        return map;
    }

    public static void main(String[] args) {
        String API_KEY = "5efaa1387f04c3d66c1e9fad14fe6f04";
        String LAT = "55.02";
        String LON = "82.93";
        String URL_STR = "https://api.openweathermap.org/data/2.5/onecall?" +
                "lat=" + LAT + "&lon=" + LON +
                "&exclude=current,minutely,hourly,alerts&units=metric" +
                "&appid=" + API_KEY;

        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(URL_STR);
            URLConnection urlConnection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            reader.close();

            Map<String, Object> respMap = json2Map(result.toString());
            List<Map<String, Object>> daily = (List<Map<String, Object>>) respMap.get("daily");

            System.out.println(respMap.get("timezone"));

            List<Double> pressList = new ArrayList<>();
            List<Double> tempList = new ArrayList<>();

            for (int i=0; i<5; i++) {
                double pres = (double) daily.get(i).get("pressure");
                pressList.add(pres);

                Map<String, Object> tempMap = json2Map(daily.get(i).get("temp").toString());
                double temp = (double) tempMap.get("night") - (double) tempMap.get("morn");
                tempList.add(temp);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");

            Double maxPress = pressList.stream().max(Comparator.comparing(Double::valueOf)).get();
            Double date = (Double) daily.get(pressList.indexOf(maxPress)).get("dt");
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(date.longValue()),TimeZone.getDefault().toZoneId());

            System.out.println(String.format("Max pressure in 5 days: %.0f mm of mercury column (date: %s)", maxPress, dateTime.format(formatter)));

            Double minTempDif = tempList.stream().min(Comparator.comparing(Double::valueOf)).get();
            date = (Double) daily.get(tempList.indexOf(minTempDif)).get("dt");
            dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(date.longValue()),TimeZone.getDefault().toZoneId());

            System.out.println(String.format("Min diff between night and morning temperature: %.2f degrees Celsius (date: %s)"
                    , minTempDif
                    , dateTime.format(formatter)));

        } catch (IOException e) {

        }
    }
}