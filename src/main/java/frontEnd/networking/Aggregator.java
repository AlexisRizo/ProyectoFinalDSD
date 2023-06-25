package frontEnd.networking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Aggregator {
    private WebClient webClient;
    public Aggregator() {
        this.webClient = new WebClient();
    }
    public  Map<String, List<Double>> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
        CompletableFuture<byte[]>[] futures = new CompletableFuture[tasks.size()];
        int ntask = 0;
        for(int i=0; i<workersAddresses.size(); i++){
            String workerAddress = workersAddresses.get(i);
            String task = tasks.get(i);
            byte[] requestPayload = task.getBytes();
            futures[i] = webClient.sendTask(workerAddress, requestPayload);
        }
        boolean bandera = true;
        while(bandera){
            if (true == futures[0].isDone()&&true == futures[1].isDone()&&true == futures[2].isDone())
                bandera=false;
        }
        Map<String, List<Double>> Libros = new HashMap<String, List<Double>>();
        for(int i=0; i<futures.length; i++){
            Map<String, List<Double>> aux = (Map<String, List<Double>>) SerializationUtils.deserialize(futures[i].join());
            aux.forEach((key, value) -> {Libros.put(key, value);});
        }
        return Libros;
    }
}