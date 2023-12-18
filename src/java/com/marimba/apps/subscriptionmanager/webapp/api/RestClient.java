package com.marimba.apps.subscriptionmanager.webapp.api;

import com.marimba.apps.subscriptionmanager.webapp.util.defensight.ILogger;
import java.io.Closeable;
import java.io.InputStream;
import java.security.spec.RSAOtherPrimeInfo;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicHeader;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class to call the REST api follows the builder design pattern
 *
 * @author Yogesh Pawar
 * @Date: 18/12/2023
 * @since 1.0
 */
public class RestClient {

  private String apiUrl;
  private List<BasicHeader> headers;
  private List<NameValuePair> parameters;

  private RestClient() {
    this.headers = new ArrayList<>();
    this.parameters = new ArrayList<>();
  }

  public static RestClient build(String apiUrl) {
    RestClient restClient = new RestClient();
    restClient.apiUrl = apiUrl;
    return restClient;
  }

  public RestClient addHeader(String name, String value) {
    headers.add(new BasicHeader(name, value));
    return this;
  }

  public RestClient addParameter(String name, String value) {
    if (value != null && !value.equalsIgnoreCase("0")) {
      parameters.add(new BasicNameValuePair(name, value));
    }
    return this;
  }

  int maxAttempt = 5;
  int i = 0;

  public JsonNode executeGetRequest() throws URISyntaxException {

    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(apiUrl);

    for (BasicHeader header : headers) {
      httpGet.addHeader(header.getName(), header.getValue());
    }

    URIBuilder uriBuilder = new URIBuilder(apiUrl);
    for (NameValuePair param : parameters) {
      uriBuilder.addParameter(param.getName(), param.getValue());
    }

    httpGet.setURI(uriBuilder.build());

    try {
      CloseableHttpResponse response = httpClient.execute(httpGet);
      int statusCode = response.getStatusLine().getStatusCode();
      InputStream responseStream = null;
      if (statusCode == 200) {
        responseStream = response.getEntity().getContent();
        return handleResponse(responseStream);
      } else if (statusCode == 503 || statusCode == 504) {
        //fall back mechanism
        for (i = 0; i < maxAttempt; i++) {
          Thread.sleep(6000);
          return executeGetRequest();
        }
      } else {
        System.out.println("Unexpected status code : " + statusCode);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private JsonNode handleResponse(InputStream responseStream) {
    return parseJson(responseStream);
  }

  private static JsonNode parseJson(InputStream responseStream) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readTree(responseStream);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;

  }
}

