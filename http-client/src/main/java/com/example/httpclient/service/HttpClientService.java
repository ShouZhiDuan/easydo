package com.example.httpclient.service;

import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface HttpClientService {

    /**
     * Executes a GET request.
     *
     * @param url         The URL to send the request to.
     * @param headers     Optional request headers.
     * @param queryParams Optional query parameters.
     * @return The response body as a String.
     * @throws IOException If an I/O error occurs.
     */
    String get(String url, Map<String, String> headers, Map<String, String> queryParams) throws IOException;

    /**
     * Executes a POST request with a JSON body.
     *
     * @param url     The URL to send the request to.
     * @param headers Optional request headers.
     * @param jsonBody The JSON body as a String.
     * @return The response body as a String.
     * @throws IOException If an I/O error occurs.
     */
    String postJson(String url, Map<String, String> headers, String jsonBody) throws IOException;

    /**
     * Executes a POST request with form data.
     *
     * @param url      The URL to send the request to.
     * @param headers  Optional request headers.
     * @param formData A map representing the form data.
     * @return The response body as a String.
     * @throws IOException If an I/O error occurs.
     */
    String postForm(String url, Map<String, String> headers, Map<String, String> formData) throws IOException;

    /**
     * Executes a PUT request with a JSON body.
     *
     * @param url     The URL to send the request to.
     * @param headers Optional request headers.
     * @param jsonBody The JSON body as a String.
     * @return The response body as a String.
     * @throws IOException If an I/O error occurs.
     */
    String putJson(String url, Map<String, String> headers, String jsonBody) throws IOException;

    /**
     * Executes a DELETE request.
     *
     * @param url     The URL to send the request to.
     * @param headers Optional request headers.
     * @return The response body as a String.
     * @throws IOException If an I/O error occurs.
     */
    String delete(String url, Map<String, String> headers) throws IOException;

    /**
     * Uploads a file using a POST request (multipart/form-data).
     *
     * @param url           The URL to upload the file to.
     * @param headers       Optional request headers.
     * @param fileParamName The name of the file parameter in the multipart request.
     * @param file          The file to upload.
     * @param otherParams   Other form parameters to include in the request.
     * @return The response body as a String.
     * @throws IOException If an I/O error occurs.
     */
    String uploadFile(String url, Map<String, String> headers, String fileParamName, File file, Map<String, String> otherParams) throws IOException;

    /**
     * Downloads a file.
     *
     * @param url The URL to download the file from.
     * @param headers Optional request headers.
     * @return An InputStream of the response body.
     * @throws IOException If an I/O error occurs.
     */
    InputStream downloadFile(String url, Map<String, String> headers) throws IOException;

    /**
     * Executes a generic HTTP request.
     *
     * @param url           The URL to send the request to.
     * @param method        The HTTP method (GET, POST, PUT, DELETE, etc.).
     * @param headers       Optional request headers.
     * @param queryParams   Optional query parameters (for GET requests).
     * @param requestBody   Optional request body (for POST, PUT requests).
     * @return The full OkHttp Response object.
     * @throws IOException If an I/O error occurs.
     */
    Response execute(String url, String method, Map<String, String> headers, Map<String, String> queryParams, RequestBody requestBody) throws IOException;
}