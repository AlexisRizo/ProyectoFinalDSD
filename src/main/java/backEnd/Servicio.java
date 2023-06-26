package backEnd;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import common.Client;
import common.SerializationUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Servicio {
	private static final String MAIN_SERVER_ADDRESS = "http://127.0.0.1:3000/registrar";
	private static final String BOOK_ANALISYS_ENDPOINT = "/analisisLibros";
	private static final String STATUS_ENDPOINT = "/status";
	private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	public static void main(String[] args) throws IOException {
		int port = args.length == 1 ? Integer.parseInt(args[0]) : 8082;
		
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		
		server.createContext(BOOK_ANALISYS_ENDPOINT).setHandler(Servicio::handleBookAnalisysRequest);
		server.createContext(STATUS_ENDPOINT).setHandler(Servicio::handleStatusCheckRequest);
		
		server.setExecutor(Executors.newFixedThreadPool(8));
		server.start();
		System.out.println("Servidor escuchando en el puerto " + port + ".");
		
		String result = Client.sendTaskAndGetString(MAIN_SERVER_ADDRESS, ("" + port).getBytes());
		while (!result.equalsIgnoreCase("OK")) {
			System.out.println("Error de registro. Reintentando.");
			result = Client.sendTaskAndGetString(MAIN_SERVER_ADDRESS, ("" + port).getBytes());
		}
		System.out.println("Servidor registrado exitosamente.");
		
		executor.schedule(() -> keepRegistered(port), 1, TimeUnit.SECONDS);
	}
	
	private static void keepRegistered(int port) {
		String result = Client.sendTaskAndGetString(MAIN_SERVER_ADDRESS, ("" + port).getBytes());
		while (!result.equalsIgnoreCase("OK")) {
			System.out.println("Error de mantenimiento de registro. Reintentando.");
			result = Client.sendTaskAndGetString(MAIN_SERVER_ADDRESS, ("" + port).getBytes());
		}
		//System.out.println("Servidor sigue registrado.");
		executor.schedule(() -> keepRegistered(port), 1, TimeUnit.SECONDS);
	}
	
	private static void handleBookAnalisysRequest(HttpExchange exchange) throws IOException {
		try {
			sendResponse(calculateResponse(exchange.getRequestBody().readAllBytes()), exchange);
		} catch (Exception e) {
			System.out.println("Error reading service");
			e.printStackTrace();
			sendResponse(new byte[0], exchange);
		}
	}
	
	private static byte[] calculateResponse(byte[] requestBytes) throws URISyntaxException
	{
		String[] stringNumbers = new String(requestBytes, StandardCharsets.UTF_8).split(",");
		int inicio = Integer.parseInt(stringNumbers[0]);
		int fin = Integer.parseInt(stringNumbers[1]);
		String frase = stringNumbers[2].toLowerCase();
		
		//Se inicializa el Map
		Map<String,Double> palabras = Arrays.stream(frase.split(" "))
			.collect(Collectors.toMap(s -> s, s -> 0.0, (a, b) -> b));
		
		List<Map<String, Double>> libros = new ArrayList<>();
		//Se busca y lee los elementos dentro del directorio libros/
		File[] files = new File(Servicio.class.getClassLoader().getResource("libros/").toURI()).listFiles();
		for (int i = inicio; i <= fin; i++) {
			double n_palabras = 0;
			try (FileReader fileReader = new FileReader(files[i]); BufferedReader bR = new BufferedReader(fileReader)) {
				String parrafo;
				while ((parrafo = bR.readLine()) != null) {
					String aux = parrafo.toLowerCase();
					if (!aux.isEmpty())
						n_palabras += aux.split(" ").length;
					for (Map.Entry<String, Double> palabra : palabras.entrySet()) {
						palabras.put(palabra.getKey(),
							palabras.get(palabra.getKey()) + aux.split(palabra.getKey(), -1).length - 1
						);
					}
				}
			} catch (IOException e) {
				System.out.println("Ocurrio un error");
			}
			for(Map.Entry<String,Double> palabra : palabras.entrySet())
				palabra.setValue(palabra.getValue()/n_palabras);
			libros.add(new HashMap<>(palabras));
			for(Map.Entry<String,Double> palabra : palabras.entrySet())
				palabra.setValue(0.0);
		}
		Map<String, List<Double>> datos = new HashMap<>();
		for(Map<String,Double> map: libros){
			String s = files[libros.indexOf(map)+inicio].getName();
			List<Double> frecuencias = new ArrayList<>();
			for(Map.Entry<String, Double> e : map.entrySet())
				frecuencias.add(e.getValue());
			datos.put(s,new ArrayList<>(frecuencias));
			frecuencias.clear();
		}
		return SerializationUtils.serialize(datos);
	}
	
	private static void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(200, responseBytes.length);
		try (OutputStream outputStream = exchange.getResponseBody()) {
			outputStream.write(responseBytes);
		}
	}
	
	private static void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
		String responseMessage = "OK";
		sendResponse(responseMessage.getBytes(), exchange);
	}
}
