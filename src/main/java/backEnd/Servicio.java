package backEnd;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import common.Client;
import common.SerializationUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

public class Servicio {
	private static final String MAIN_SERVER_ADDRESS = "http://127.0.0.1:3000/registrar";
	private static final String BOOK_ANALISYS_ENDPOINT = "/analisisLibros";
	private static final String STATUS_ENDPOINT = "/status";
	
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
	}
	
	private static void handleBookAnalisysRequest(HttpExchange exchange) throws IOException {
		if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
			exchange.close();
			return;
		}
		
		Headers headers = exchange.getRequestHeaders();
		if (headers.containsKey("X-Test") && headers.get("X-Test").get(0).equalsIgnoreCase("true")) {
			String dummyResponse = "123\n";
			sendResponse(dummyResponse.getBytes(), exchange);
			return;
		}
		
		boolean isDebugMode = false;
		if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
			isDebugMode = true;
		}
		
		long startTime = System.nanoTime();
		
		byte[] requestBytes = exchange.getRequestBody().readAllBytes();
		byte[] responseBytes = calculateResponse(requestBytes);
		
		long finishTime = System.nanoTime();
		
		if (isDebugMode) {
			long tiempo_n = finishTime - startTime;
			double tiempo = (double)tiempo_n*Math.pow(10,-6);
			int min  = (int)tiempo/1000;
			int seg  = (int)tiempo%1000;
			String debugMessage = String.format("La operacion tomo %d nanosegundos = ", tiempo_n);
			debugMessage += min+" segundos con "+seg+" milisegundos";
			exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
		}
		
		sendResponse(responseBytes, exchange);
	}
	
	private static byte[] calculateResponse(byte[] requestBytes) {
		//Se asignan valores
		String bodyString = new String(requestBytes);
		System.out.println(bodyString);
		String[] stringNumbers = bodyString.split(",");
		int n_i = Integer.parseInt(stringNumbers[0]);
		int n_j = Integer.parseInt(stringNumbers[1]);
		String frase = stringNumbers[2].toLowerCase();
		String[] cadFrase = frase.split(" ");
		//Se busca y lee los elementos dentro del directorio libros/
		File directory = new File("libros/");
		File[] files = directory.listFiles();
		Map<String,Double> palabras = new HashMap<String,Double>();
		List<Map<String,Double>> Libros = new ArrayList<>();
		//Se inicializa el Map
		for(String s:cadFrase){
			if(!palabras.containsKey(s))
				palabras.put(s,0.0);
		}
		
		FileReader fileReader;
		BufferedReader bR;
		for(int i=n_i; i<=n_j; i++){
			double n_palabras = 0;
			try{
				fileReader = new FileReader(files[i]);
				bR = new BufferedReader(fileReader);
				String parrafo;
				while((parrafo=bR.readLine())!=null){
					String aux = parrafo.toLowerCase();
					if (!aux.isEmpty())
						n_palabras += aux.split(" ").length;
					for(Map.Entry<String,Double> palabra : palabras.entrySet())
						palabras.put(palabra.getKey(),palabras.get(palabra.getKey())+aux.split(palabra.getKey(),-1).length-1);
				}
				bR.close();
				fileReader.close();
			}catch(IOException e){
				System.out.println("Ocurrio un error");
			}
			for(Map.Entry<String,Double> palabra : palabras.entrySet())
				palabra.setValue(palabra.getValue()/n_palabras);
			Libros.add(new HashMap<>(palabras));
			for(Map.Entry<String,Double> palabra : palabras.entrySet())
				palabra.setValue(0.0);
		}
		Map<String, List<Double>> datos = new HashMap<>();
		int contador_libros = 0;
		for(Map<String,Double> map: Libros){
			String s = files[Libros.indexOf(map)+n_i].getName();
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
