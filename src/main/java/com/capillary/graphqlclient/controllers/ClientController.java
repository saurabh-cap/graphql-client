package com.capillary.graphqlclient.controllers;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.capillary.graphqlclient.AuthorQuery;
import okhttp3.OkHttpClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/author", produces = "application/json")
public class ClientController {
    private static final String BASE_URL = "http://localhost:8090/graphql";

    @RequestMapping(method = RequestMethod.GET, path = "/rest/all")
    public String sampleQueryPost() throws IOException {
        final String POST_PARAMS = "{\"query\":\"{\\n  allAuthors{\\n    id\\n  }\\n}\",\"variables\":null,\"operationName\":null}";
        System.out.println(POST_PARAMS);
        URL obj = null;
        try {
            obj = new URL(BASE_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
        try {
            postConnection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        postConnection.setRequestProperty("Content-Type", "application/json");
        postConnection.setDoOutput(true);
        OutputStream os = null;
        try {
            os = postConnection.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        int responseCode = postConnection.getResponseCode();
        System.out.println("POST Response Code :  " + responseCode);
        System.out.println("POST Response Message : " + postConnection.getResponseMessage());
        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    postConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            return "error";
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/all")
    public String sampleQueryApollo() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();

        AuthorQuery authorQuery = AuthorQuery.builder()
                .build();

        apolloClient.query(authorQuery).enqueue(new ApolloCall.Callback<Optional<AuthorQuery.Data>>() {
            @Override
            public void onResponse(@Nonnull Response<Optional<AuthorQuery.Data>> response) {
                if (response.data().isPresent()) {
                    final AuthorQuery.Data data = response.data().get();
                    final Optional<List<AuthorQuery.AllAuthor>> allAuthors = data.allAuthors();
                    if (allAuthors.isPresent()) {
                        final List<AuthorQuery.AllAuthor> allAuthors1 = allAuthors.get();
                        for (AuthorQuery.AllAuthor allAuthor : allAuthors1) {
                            System.out.println(allAuthor.id());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

            }
        });

        return "works";
    }
}
