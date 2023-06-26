package frontEnd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import common.Aggregator;
import frontEnd.auxiliares.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class WebServer {
	private static final String STATUS_ENDPOINT = "/status";
	private static final String HOME_PAGE_ENDPOINT = "/";
	private static final String PROCESS_ENDPOINT = "/procesar_datos";
	private static final String REGISTER_ENDPOINT = "/registrar";
	
	private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/ui_assets/";
	private static final int NUM_BOOKS = 46;
	
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
	
	private static void handleTaskRequest(HttpExchange exchange) throws IOException {
		if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
			exchange.close();
			return;
		}
		
		FrontendSearchRequest frontendSearchRequest = objectMapper.readValue(
			exchange.getRequestBody().readAllBytes(),
			FrontendSearchRequest.class
		);
		String query = frontendSearchRequest.getSearchQuery();
		
		List<String> servidores = ServerContainer.applyAction(ServerContainer.Action.Update, null);
		if (servidores.isEmpty()) {
			System.out.println("No hay servidores disponibles.");
			sendResponse("No hay servidores disponibles.".getBytes(), exchange);
			return;
		}
		List<String> tasks = new ArrayList<>(servidores.size());
		System.out.println("Los intervalos son:");
		for (int i = 0; i < servidores.size(); i++) {
			tasks.add(
				i * NUM_BOOKS / servidores.size()
				+ "," + ((i + 1) * NUM_BOOKS / servidores.size() - 1)
				+ "," + query
			);
			
			System.out.println("\tIntervalo " + i + ": " + tasks.get(i));
		}
		Map<String, List<Double>> libros = new Aggregator().sendTasksToWorkers(
			servidores,
			tasks
		);
		
		double[] nt = new double[query.split("\\W").length];
		
		//Numero de apariciones en libros
		for (Map.Entry<String, List<Double>> e : libros.entrySet()) {
			int contador = 0;
			for (Double d : e.getValue()) {
				if (d > 0.0)
					nt[contador] += 1;
				contador++;
			}
		}
		//Calculo de la tercer propuesta
		List<Text> books = new ArrayList<>();
		for (Map.Entry<String, List<Double>> e : libros.entrySet()) {
			int contador = 0;
			double fitness = 0;
			for (Double d : e.getValue()) {
				fitness += Math.log10(libros.size() / nt[contador]) * d;
			}
			books.add(new Text(e.getKey(), fitness));
		}
		
		books.sort(Comparator.comparingDouble((Text text) -> text.fitness).reversed());
		
		
		StringBuilder cadena = new StringBuilder(" ");
		books.stream()
			.filter(e -> e.fitness != 0)
			.map(t -> t + "\n")
			.forEach(cadena::append);
		
		StringTokenizer st = new StringTokenizer(cadena.toString());
		FrontendSearchResponse response = new FrontendSearchResponse(cadena.toString(), st.countTokens());
		sendResponse(objectMapper.writeValueAsBytes(response), exchange);
	}
	
	private static void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
		sendResponse("El servidor está vivo\n".getBytes(), exchange);
	}
	
	private static void handleRegisterRequest(HttpExchange exchange) throws IOException {
		String port = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		
		ServerContainer.applyAction(
			ServerContainer.Action.Add,
			"http://" + exchange.getRemoteAddress().getHostName()
			+ ":" + new DecimalFormat("0000").format(Integer.parseInt(port))
			+ "/analisisLibros"
		);
		
		sendResponse("OK".getBytes(), exchange);
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

class ServerContainer {
	private static final int MAX_WAIT = 1;
	private static final TimeUnit MAX_WAIT_UNIT = TimeUnit.SECONDS;
	private static final List<String> servidores = new ArrayList<>();
	enum Action {
		Add,
		Update,
		Get
	};
	
	public static List<String> applyAction(Action action, String elem) {
		if (action.equals(Action.Add)) {
			if (!servidores.contains(elem)) {
				servidores.add(elem);
				System.out.println("Nuevo servidor encontrado: " + elem);
			} else {
				System.out.println("El servidor " + elem + " sigue registrado.");
			}
		} else if (action.equals(Action.Update)) {
			System.out.println("Revisando servidores.");
			List<Boolean> isHearing = Aggregator.sendTasksAndGetFutures(
					servidores.stream().map(s -> new Aggregator.Task(
						s.replace("analisisLibros", "status"),
						"".getBytes()
					)).collect(Collectors.toList())
				).stream()
				.map(future -> {
					try {
						return new String(
							future.get(MAX_WAIT, MAX_WAIT_UNIT),
							StandardCharsets.UTF_8
						).equalsIgnoreCase("OK");
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						return false;
					}
				}).collect(Collectors.toList());
			for (int i = isHearing.size() - 1; i >= 0; i--) {
				if (!isHearing.get(i)) {
					System.out.println(
						"El servidor " + servidores.remove(i) + " dejó de responder."
					);
				}
			}
		}
		return new ArrayList<>(servidores);
	}
}