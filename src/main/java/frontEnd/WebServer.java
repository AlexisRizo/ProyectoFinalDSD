package frontEnd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import frontEnd.auxiliares.Ordena;
import frontEnd.auxiliares.Text;
import frontEnd.networking.Aggregator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WebServer {
   
    private static final String STATUS_ENDPOINT = "/status";
    private static final String HOME_PAGE_ENDPOINT = "/";
    private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/ui_assets/";
    private static final String ENDPOINT_PROCESS = "/procesar_datos";
    private static final String WORKER_ADDRESS_1 = "http://127.0.0.1:8081/analisis_libros";
    private static final String WORKER_ADDRESS_2 = "http://127.0.0.1:8082/analisis_libros";
    private static final String WORKER_ADDRESS_3 = "http://127.0.0.1:8083/analisis_libros";
    private static final int NUM_BOOKS = 46;

    private final int port; 
    private HttpServer server; 
    private final ObjectMapper objectMapper;

    public WebServer(int port) {
        this.port = port;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT); 
        HttpContext taskContext = server.createContext(ENDPOINT_PROCESS);
        HttpContext homePageContext = server.createContext(HOME_PAGE_ENDPOINT);
        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);
        homePageContext.setHandler(this::handleRequestForAsset);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleRequestForAsset(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        byte[] response;

        String asset = exchange.getRequestURI().getPath(); 

        if (asset.equals(HOME_PAGE_ENDPOINT)) { 
            response = readUiAsset(HOME_PAGE_UI_ASSETS_BASE_DIR + "index.html");
        } else {
            response = readUiAsset(asset); 
        }
        addContentType(asset, exchange);
        sendResponse(response, exchange);
    }

    private byte[] readUiAsset(String asset) throws IOException {
        InputStream assetStream = getClass().getResourceAsStream(asset);

        if (assetStream == null) {
            return new byte[]{};
        }
        return assetStream.readAllBytes(); 
    }

    private static void addContentType(String asset, HttpExchange exchange) {

        String contentType = "text/html";  
        if (asset.endsWith("js")) {
            contentType = "text/javascript";
        } else if (asset.endsWith("css")) {
            contentType = "text/css";
        }
        else if (asset.endsWith("ico")) {
            contentType = "image/x-icon";
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { 
            exchange.close();
            return;
        }

        try {
            FrontendSearchRequest frontendSearchRequest = objectMapper.readValue(exchange.getRequestBody().readAllBytes(), FrontendSearchRequest.class); 
            String string = frontendSearchRequest.getSearchQuery();
            int division = NUM_BOOKS/3;
            int inicial = 0;
            String task1 = new String(inicial+","+(division-1)+","+string);
            String task2 = new String(division+","+((division*2)-1)+","+string);
            String task3 = new String((division*2)+","+(NUM_BOOKS-1)+","+string);
            System.out.println("Los intervalos son:");
            System.out.println("Intervalo 1: "+task1);
            System.out.println("Intervalo 2: "+task2);
            System.out.println("Intervalo 3: "+task3);
            Aggregator aggregator = new Aggregator();
            Map<String, List<Double>> Libros = aggregator.sendTasksToWorkers(Arrays.asList(WORKER_ADDRESS_1,WORKER_ADDRESS_2,WORKER_ADDRESS_3),Arrays.asList(task1,task2,task3));
            int n_frase = string.split(" ").length;
            double[] nt = new double[n_frase];
            for(double d: nt)
                d = 0.0;
            //Numero de apariciones en libros
            for (Map.Entry<String, List<Double>> e : Libros.entrySet()){
                int contador = 0;
                for(Double d : e.getValue()){
                    if(d>0.0)
                        nt[contador] += 1;
                    contador++;
                }
            }
            //Calculo de la tercer propuesta
            List<Text> books = new ArrayList<>();
            for (Map.Entry<String, List<Double>> e : Libros.entrySet()){
                int contador = 0;
                double fitness = 0;
                for(Double d : e.getValue()){
                    fitness += Math.log10(Libros.size()/nt[contador])*d;
                }
                books.add(new Text(e.getKey(),fitness));
            }

            Collections.sort(books,new Ordena());
            Collections.reverse(books);

            List<Text> Resultados = books.stream().filter(e -> e.fitness != 0).collect(Collectors.toList());

            String cadena = " ";
            for(Text t: Resultados)
                cadena += t.toString()+"\n";

            StringTokenizer st = new StringTokenizer(cadena);
            FrontendSearchResponse frontendSearchResponse = new FrontendSearchResponse(cadena, st.countTokens());
            byte[] responseBytes = objectMapper.writeValueAsBytes(frontendSearchResponse);
            sendResponse(responseBytes, exchange);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage.getBytes(), exchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }
}


