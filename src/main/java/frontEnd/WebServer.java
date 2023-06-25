package frontEnd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
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
	private static final String PROCESS_ENDPOINT = "/procesar_datos";
	private static final String REGISTER_ENDPOINT = "/registrar";
	
	private static final String WORKER_ADDRESS_1 = "http://127.0.0.1:8081/analisis_libros";
	private static final String WORKER_ADDRESS_2 = "http://127.0.0.1:8082/analisis_libros";
	private static final String WORKER_ADDRESS_3 = "http://127.0.0.1:8083/analisis_libros";
	private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/ui_assets/";
	private static final int NUM_BOOKS = 46;
	private static final List<String> servidores = new ArrayList<>();
	
	private static final ObjectMapper objectMapper = new ObjectMapper()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	public static void main(String[] args) throws IOException {
		int port = args.length == 1 ? Integer.parseInt(args[0]) : 3000;
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		
		server.createContext(HOME_PAGE_ENDPOINT).setHandler(WebServer::handleRequestForAsset);
		server.createContext(PROCESS_ENDPOINT).setHandler(WebServer::handleTaskRequest);
		server.createContext(STATUS_ENDPOINT).setHandler(WebServer::handleStatusCheckRequest);
		server.createContext(REGISTER_ENDPOINT).setHandler(WebServer::handleRegisterRequest);
		
		server.setExecutor(Executors.newFixedThreadPool(8));
		server.start();
		
		System.out.println("Servidor escuchando en el puerto: " + port);
	}
	
	private static void handleRequestForAsset(HttpExchange exchange) throws IOException {
		if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
			exchange.close();
			return;
		}
		
		String asset = exchange.getRequestURI().getPath();
		
		byte[] response = asset.equals(HOME_PAGE_ENDPOINT) ?
				readUiAsset(HOME_PAGE_UI_ASSETS_BASE_DIR + "index.html")
			: readUiAsset(asset);
		addContentType(asset, exchange);
		sendResponse(response, exchange);
	}
	
	private static void handleTaskRequest(HttpExchange exchange) {
		if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
			exchange.close();
			return;
		}
		
		try {
			FrontendSearchRequest frontendSearchRequest = objectMapper.readValue(
				exchange.getRequestBody().readAllBytes(),
				FrontendSearchRequest.class
			);
			String query = frontendSearchRequest.getSearchQuery();
			int division = NUM_BOOKS / 3;
			int inicial = 0;
			
			String task1 = inicial + "," + (division - 1) + "," + query;
			String task2 = division + "," + ((division * 2) - 1) + "," + query;
			String task3 = (division * 2) + "," + (NUM_BOOKS - 1) + "," + query;
			System.out.println("Los intervalos son:");
			System.out.println("Intervalo 1: "+task1);
			System.out.println("Intervalo 2: "+task2);
			System.out.println("Intervalo 3: "+task3);
			Map<String, List<Double>> libros = new Aggregator().sendTasksToWorkers(
				Arrays.asList(WORKER_ADDRESS_1,WORKER_ADDRESS_2,WORKER_ADDRESS_3),
				Arrays.asList(task1,task2,task3)
			);
			
			int n_frase = query.split(" ").length;
			double[] nt = new double[n_frase];
			
			//Numero de apariciones en libros
			for (Map.Entry<String, List<Double>> e : libros.entrySet()){
				int contador = 0;
				for (Double d : e.getValue()) {
					if (d > 0.0)
						nt[contador] += 1;
					contador++;
				}
			}
			//Calculo de la tercer propuesta
			List<Text> books = new ArrayList<>();
			for (Map.Entry<String, List<Double>> e : libros.entrySet()){
				int contador = 0;
				double fitness = 0;
				for(Double d : e.getValue()){
					fitness += Math.log10(libros.size()/nt[contador])*d;
				}
				books.add(new Text(e.getKey(),fitness));
			}
			
			books.sort(Comparator.comparingDouble((Text text) -> text.fitness).reversed());
			
			List<Text> resultados = books.stream().filter(e -> e.fitness != 0).collect(Collectors.toList());
			
			String cadena = " ";
			for(Text t: resultados)
				cadena += t.toString()+"\n";
			
			StringTokenizer st = new StringTokenizer(cadena);
			FrontendSearchResponse frontendSearchResponse = new FrontendSearchResponse(cadena, st.countTokens());
			byte[] responseBytes = objectMapper.writeValueAsBytes(frontendSearchResponse);
			sendResponse(responseBytes, exchange);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
		String responseMessage = "El servidor está vivo\n";
		sendResponse(responseMessage.getBytes(), exchange);
	}
	
	private static void handleRegisterRequest(HttpExchange exchange) {
	
	}
	
	private static byte[] readUiAsset(String asset) {
		try (InputStream assetStream = WebServer.class.getResourceAsStream(asset)) {
			return assetStream == null ? new byte[]{} : assetStream.readAllBytes();
		} catch (IOException e) {
			return new byte[]{};
		}
	}
	
	private static void addContentType(String asset, HttpExchange exchange) {
		exchange.getResponseHeaders().add(
			"Content-Type",
			asset.endsWith("js") ? "text/javascript"
				: asset.endsWith("css") ? "text/css"
				: "text/html"
		);
	}
	
	private static void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(200, responseBytes.length);
		try (OutputStream outputStream = exchange.getResponseBody()) {
			outputStream.write(responseBytes);
		}
	}
}


