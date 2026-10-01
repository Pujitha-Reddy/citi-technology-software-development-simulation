
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class App {

    // Queue that stores the market data
    private static Queue<MarketDataPoint> dataQueue = new LinkedList<>();

    public static void main(String[] args) {

        // Get the API key from the environment variable
        String apiKey = System.getenv("TWELVE_DATA_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("API key not found.");
            return;
        }

        System.out.println("Starting DIA market data monitor...");

        while (true) {
            try {

                // Get the latest DIA price
                MarketDataPoint dataPoint = getDIAPrice(apiKey);

                // Add the result to the queue
                dataQueue.add(dataPoint);

                System.out.println(
                    "Added data point: price=" +
                    dataPoint.price +
                    ", timestamp=" +
                    dataPoint.timestamp
                );

                System.out.println(
                    "Current queue size: " + dataQueue.size()
                );

                // Wait 15 seconds
                Thread.sleep(15000);

            } catch (Exception e) {

                System.out.println(
                    "Error retrieving market data: " +
                    e.getMessage()
                );

                // Wait 15 seconds before trying again
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private static MarketDataPoint getDIAPrice(String apiKey)
            throws Exception {

        String apiUrl =
            "https://api.twelvedata.com/price?symbol=DIA&apikey="
            + apiKey;

        URL url = new URL(apiUrl);

        HttpURLConnection connection =
            (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        if (responseCode != 200) {
            throw new RuntimeException(
                "API request failed. HTTP status: "
                + responseCode
            );
        }

        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(
                    connection.getInputStream()
                )
            );

        StringBuilder response = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        String jsonResponse = response.toString();

        // Extract the price from the API response
        String price = extractPrice(jsonResponse);

        if (price == null) {
            throw new RuntimeException(
                "Price not found in API response: "
                + jsonResponse
            );
        }

        return new MarketDataPoint(
            Double.parseDouble(price),
            Instant.now()
        );
    }

    private static String extractPrice(String json) {

        String searchText = "\"price\"";

        int keyIndex = json.indexOf(searchText);

        if (keyIndex == -1) {
            return null;
        }

        int colonIndex = json.indexOf(":", keyIndex);

        if (colonIndex == -1) {
            return null;
        }

        int start = colonIndex + 1;

        // Skip spaces
        while (
            start < json.length() &&
            Character.isWhitespace(json.charAt(start))
        ) {
            start++;
        }

        // Remove quotation marks if present
        if (
            start < json.length() &&
            json.charAt(start) == '"'
        ) {
            start++;
        }

        int end = start;

        while (
            end < json.length() &&
            (
                Character.isDigit(json.charAt(end)) ||
                json.charAt(end) == '.' ||
                json.charAt(end) == '-'
            )
        ) {
            end++;
        }

        if (start == end) {
            return null;
        }

        return json.substring(start, end);
    }

    // Represents one price + timestamp
    static class MarketDataPoint {

        double price;
        Instant timestamp;

        MarketDataPoint(
            double price,
            Instant timestamp
        ) {
            this.price = price;
            this.timestamp = timestamp;
        }
    }
}
