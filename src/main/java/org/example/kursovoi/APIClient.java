package org.example.kursovoi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class APIClient {
    Gson gson;
    public APIClient(){
        this.gson = new Gson();
    }
    /*public void start() throws Exception {
        List<Category> categories = fetchData("http://localhost:8080/categories",Category.class);
        for (Category ct:categories){
            System.out.println(ct.getName());
        }
    }*/

    public <T> List<T> fetchGetData(String url, Class<T> tClass) throws Exception {
        // Создаем HttpClient
        HttpClient client = HttpClient.newHttpClient();

        // Создаем запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET() // Если нужен POST, можно заменить на .POST()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());



        Type listType = TypeToken.getParameterized(List.class, tClass).getType();

        return gson.fromJson(response.body(), listType);
    }


}


