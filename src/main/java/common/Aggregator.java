package common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Aggregator {
	public  Map<String, List<Double>> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
		CompletableFuture<byte[]>[] futures = new CompletableFuture[tasks.size()];
		for (int i = 0; i < workersAddresses.size(); i++) {
			futures[i] = Client.sendTask(workersAddresses.get(i), tasks.get(i).getBytes());
		}
		boolean bandera = true;
		while (bandera) {
			if (futures[0].isDone() && futures[1].isDone() && futures[2].isDone())
				bandera = false;
		}
		Map<String, List<Double>> libros = new HashMap<>();
		for (CompletableFuture<byte[]> future : futures) {
			libros.putAll((Map<String, List<Double>>) SerializationUtils.deserialize(future.join()));
		}
		return libros;
	}
	
	public static List<CompletableFuture<byte[]>> sendTasksAndGetFutures(List<Task> tasks) {
		return tasks.stream()
			.map(task -> Client.sendTask(task.getAddress(), task.getContent()))
			.collect(Collectors.toList());
	}
	
	@AllArgsConstructor @Getter
	public static class Task {
		private String address;
		private byte[] content;
	}
}
