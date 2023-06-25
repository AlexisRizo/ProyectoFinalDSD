package common;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Client {
	private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	
	public static String sendTaskAndGetString(String address, byte[] content)
	{
		HttpRequest.Builder builder = HttpRequest.newBuilder().POST(
			HttpRequest.BodyPublishers.ofByteArray(content));
		return client.sendAsync(builder.uri(URI.create(address)).build(),
			HttpResponse.BodyHandlers.ofString()
		).thenApply(HttpResponse::body).join();
	}
	
	public static CompletableFuture<byte[]> sendTask(String url, byte[] requestPayload) {
		HttpRequest request = HttpRequest.newBuilder()
			.POST(HttpRequest.BodyPublishers.ofByteArray(requestPayload))
			.uri(URI.create(url))
			.build();
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
			.thenApply(HttpResponse::body);
	}
}
