import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class PrimeServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/checkPrime", new CheckPrimeHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:8000");
    }

    // Serves the HTML page
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <title>Prime Checker</title>
                    <style>
                      body {
                        font-family: Arial, sans-serif;
                        background: #282c34;
                        color: white;
                        display: flex;
                        height: 100vh;
                        justify-content: center;
                        align-items: center;
                        flex-direction: column;
                        margin: 0;
                      }
                      input, button {
                        font-size: 1.2rem;
                        padding: 0.5rem;
                        margin: 0.5rem;
                        border-radius: 5px;
                        border: none;
                      }
                      button {
                        background-color: #61dafb;
                        cursor: pointer;
                      }
                      #result {
                        margin-top: 1rem;
                        font-size: 1.4rem;
                        font-weight: bold;
                      }
                    </style>
                </head>
                <body>

                  <h1>Prime Number Checker</h1>
                  <input id="numberInput" type="number" min="1" placeholder="Enter a positive integer" />
                  <button id="checkBtn">Check</button>
                  <div id="result"></div>

                  <script>
                    document.getElementById('checkBtn').addEventListener('click', () => {
                      const num = parseInt(document.getElementById('numberInput').value);
                      const resultDiv = document.getElementById('result');

                      if (!num || num < 1) {
                        resultDiv.textContent = "Please enter a positive integer!";
                        return;
                      }

                      fetch('/checkPrime', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                        body: 'number=' + encodeURIComponent(num)
                      })
                      .then(response => response.text())
                      .then(text => resultDiv.textContent = text)
                      .catch(err => resultDiv.textContent = "Error: " + err);
                    });
                  </script>

                </body>
                </html>
                """;

            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // Handles POST /checkPrime requests
    static class CheckPrimeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine(); // something like "number=17"

            String numberStr = "";
            if (formData != null && formData.startsWith("number=")) {
                numberStr = formData.substring("number=".length());
            }

            String response;

            try {
                int number = Integer.parseInt(numberStr);
                if (number < 1) {
                    response = "Please enter a positive integer!";
                } else if (isPrime(number)) {
                    response = number + " is a prime number! 🎉";
                } else {
                    response = number + " is not a prime number.";
                }
            } catch (NumberFormatException e) {
                response = "Invalid number format!";
            }

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // Your existing isPrime method with zero changes
    public static boolean isPrime(int number) {
        if (number <= 1) return false;

        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) return false;
        }
        return true;
    }
}

