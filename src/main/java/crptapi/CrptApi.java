package crptapi;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Date;
public class CrptApi extends AbstractCrtpApi{
    private final int reuestsLimit;
    private static int currentRequests;
    private Gson gson;

    public CrptApi(int requestsLimit){
        this.reuestsLimit = requestsLimit;
        this.gson = new Gson();
    }
    //получение uuid и data из ответа на  запрос авторизации
    private AuthorizationAnswer getAuthorizationAnswer() throws IOException, URISyntaxException, InterruptedException {
        HttpResponse<String> response = getResponse();
        return gson.fromJson(response.body(), AuthorizationAnswer.class);
    }
    //получение токена из ответа на запрос получения токена
    private Token getToken(AuthorizationAnswer answer) throws URISyntaxException, IOException, InterruptedException {
        HttpResponse<String> response = getResponse(answer);
        return gson.fromJson(response.body(), Token.class);
    }
    //запрос
        public synchronized MainAnswer sentRequest (LP_INTRODUCE_GOODS document, String sign) {
            currentRequests++;
            HttpResponse<String> response = null;
            if (currentRequests >= reuestsLimit) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {

                HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
                HttpRequest request = null;
                try {
                    AuthorizationAnswer answer = getAuthorizationAnswer();
                    Token token = getToken(answer);
                    request = HttpRequest.newBuilder()
                            .uri(new URI("localhost:8080/api/v2/{extension}/rollout?omsId=123456789"))
                            .method("post", HttpRequest.BodyPublishers.ofString(document.toString() + sign))
                            .header("bearer", token.getToken())
                            .header("accept", "application/json")
                            .build();

                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                currentRequests--;
                notify();
            }
            return gson.fromJson(response.body(), MainAnswer.class);
        }
}
interface HttpExchange{
    HttpRequest sentRequest() throws URISyntaxException;
    HttpRequest sentRequest(AuthorizationAnswer answer) throws URISyntaxException;
    HttpResponse<String> getResponse() throws IOException, InterruptedException, URISyntaxException;
    HttpResponse<String> getResponse(AuthorizationAnswer answer) throws URISyntaxException, IOException, InterruptedException;
}
abstract class AbstractCrtpApi implements HttpExchange{
    //отправка запроса авторизации
    @Override
    public HttpRequest sentRequest() throws URISyntaxException {
        HttpRequest request = null;
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://ismp.crpt.ru/api/v3/auth/cert/key"))
                    .build();
        return request;
    }
    //запрос на получение токена
    @Override
    public HttpRequest sentRequest(AuthorizationAnswer answer) throws URISyntaxException {
        HttpRequest request = null;
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://ismp.crpt.ru/api/v3/auth/cert/"))
                    .header("accept", "application/json")
                    .method("post", HttpRequest.BodyPublishers.ofString(answer.toString()))
                    .build();
        return request;
    }
    // ответ на запрос авторизации
    @Override
    public HttpResponse<String> getResponse() throws IOException, InterruptedException, URISyntaxException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = sentRequest();
        HttpResponse<String> response = null;
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }
    //ответ на запрос по получению токена
    @Override
    public HttpResponse<String> getResponse(AuthorizationAnswer answer) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = sentRequest(answer);
        HttpResponse<String> response = null;
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }
}
class MainAnswer{
    private String omsId;
    private String reportId;

    public MainAnswer(String omsId, String reportId) {
        this.omsId = omsId;
        this.reportId = reportId;
    }

    public String getOmsId() {
        return omsId;
    }

    public void setOmsId(String omsId) {
        this.omsId = omsId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}
class Token{
    private String token;

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                '}';
    }
}
class AuthorizationAnswer {
    private String uuid;
    private String data;

    @Override
    public String toString() {
        return "\"uuid=\"" + "\"" + uuid + "\"" +
                "\"data=\"" + "\"" + data + "\"";
    }

    public AuthorizationAnswer(String uuid, String data) {
        this.uuid = uuid;
        this.data = data;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
class LP_INTRODUCE_GOODS{
    private Description description;
    private String doc_id;
    private String doc_status;
    private boolean importRequest;
    private String owner_inn;
    private String participant_inn;
    private String producer_inn;
    private Date production_date;
    private String production_type;
    private Product[] products;
    private Date reg_date;
    private String reg_number;

    @Override
    public String toString() {
        return "LP_INTRODUCE_GOODS{" +
                "description=" + description +
                ", doc_id='" + doc_id + '\'' +
                ", doc_status='" + doc_status + '\'' +
                ", importRequest=" + importRequest +
                ", owner_inn='" + owner_inn + '\'' +
                ", participant_inn='" + participant_inn + '\'' +
                ", producer_inn='" + producer_inn + '\'' +
                ", production_date=" + production_date +
                ", production_type='" + production_type + '\'' +
                ", products=" + Arrays.toString(products) +
                ", reg_date=" + reg_date +
                ", reg_number='" + reg_number + '\'' +
                '}';
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(String doc_id) {
        this.doc_id = doc_id;
    }

    public String getDoc_status() {
        return doc_status;
    }

    public void setDoc_status(String doc_status) {
        this.doc_status = doc_status;
    }

    public boolean isImportRequest() {
        return importRequest;
    }

    public void setImportRequest(boolean importRequest) {
        this.importRequest = importRequest;
    }

    public String getOwner_inn() {
        return owner_inn;
    }

    public void setOwner_inn(String owner_inn) {
        this.owner_inn = owner_inn;
    }

    public String getParticipant_inn() {
        return participant_inn;
    }

    public void setParticipant_inn(String participant_inn) {
        this.participant_inn = participant_inn;
    }

    public String getProducer_inn() {
        return producer_inn;
    }

    public void setProducer_inn(String producer_inn) {
        this.producer_inn = producer_inn;
    }

    public Date getProduction_date() {
        return production_date;
    }

    public void setProduction_date(Date production_date) {
        this.production_date = production_date;
    }

    public String getProduction_type() {
        return production_type;
    }

    public void setProduction_type(String production_type) {
        this.production_type = production_type;
    }

    public Product[] getProducts() {
        return products;
    }

    public void setProducts(Product[] products) {
        this.products = products;
    }

    public Date getReg_date() {
        return reg_date;
    }

    public void setReg_date(Date reg_date) {
        this.reg_date = reg_date;
    }

    public String getReg_number() {
        return reg_number;
    }

    public void setReg_number(String reg_number) {
        this.reg_number = reg_number;
    }
}
class Description{
    private String participantInn;

    @Override
    public String toString() {
        return "Description{" +
                "participantInn='" + participantInn + '\'' +
                '}';
    }

    public String getParticipantInn() {
        return participantInn;
    }

    public void setParticipantInn(String participantInn) {
        this.participantInn = participantInn;
    }
}
class Product{
    private String certificate_document;
    private Date certificate_document_date;
    private String certificate_document_number;
    private String owner_inn;
    private String producer_inn;
    private Date production_date;
    private String tnved_code;
    private String uit_code;
    private String uitu_code;

    @Override
    public String toString() {
        return "Product{" +
                "certificate_document='" + certificate_document + '\'' +
                ", certificate_document_date=" + certificate_document_date +
                ", certificate_document_number='" + certificate_document_number + '\'' +
                ", owner_inn='" + owner_inn + '\'' +
                ", producer_inn='" + producer_inn + '\'' +
                ", production_date=" + production_date +
                ", tnved_code='" + tnved_code + '\'' +
                ", uit_code='" + uit_code + '\'' +
                ", uitu_code='" + uitu_code + '\'' +
                '}';
    }

    public String getCertificate_document() {
        return certificate_document;
    }

    public void setCertificate_document(String certificate_document) {
        this.certificate_document = certificate_document;
    }

    public Date getCertificate_document_date() {
        return certificate_document_date;
    }

    public void setCertificate_document_date(Date certificate_document_date) {
        this.certificate_document_date = certificate_document_date;
    }

    public String getCertificate_document_number() {
        return certificate_document_number;
    }

    public void setCertificate_document_number(String certificate_document_number) {
        this.certificate_document_number = certificate_document_number;
    }

    public String getOwner_inn() {
        return owner_inn;
    }

    public void setOwner_inn(String owner_inn) {
        this.owner_inn = owner_inn;
    }

    public String getProducer_inn() {
        return producer_inn;
    }

    public void setProducer_inn(String producer_inn) {
        this.producer_inn = producer_inn;
    }

    public Date getProduction_date() {
        return production_date;
    }

    public void setProduction_date(Date production_date) {
        this.production_date = production_date;
    }

    public String getTnved_code() {
        return tnved_code;
    }

    public void setTnved_code(String tnved_code) {
        this.tnved_code = tnved_code;
    }

    public String getUit_code() {
        return uit_code;
    }

    public void setUit_code(String uit_code) {
        this.uit_code = uit_code;
    }

    public String getUitu_code() {
        return uitu_code;
    }

    public void setUitu_code(String uitu_code) {
        this.uitu_code = uitu_code;
    }

    public Product (Builder builder){
        this.certificate_document = builder.certificate_document;
        this.production_date = builder.production_date;
        this.tnved_code = builder.tnved_code;
        this.certificate_document_number = builder.certificate_document_number;;
        this.certificate_document_date = builder.certificate_document_date;
        this.owner_inn = builder.owner_inn;
        this.uitu_code = builder.uitu_code;
        this.uit_code = builder.uit_code;
        this.producer_inn =builder.producer_inn;
    }
    static class Builder{
        private String certificate_document;
        private Date certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private Date production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
        public Builder(String owner_inn, String producer_inn, Date production_date, String tnved_code){
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.tnved_code = tnved_code;
        }
        public Builder setCertificate_document(String certificate_document){
            this.certificate_document = certificate_document;
            return this;
        }
        public Builder setCertificate_document_date(Date certificateDocumentDate){
            this.certificate_document_date = certificateDocumentDate;
            return this;
        }
        public Builder setCertificate_document_number(String  certificate_document_number){
            this.certificate_document_number = certificate_document_number;
            return this;
        }
        public Builder setUit_code(String uit_code){
            this.uit_code = uit_code;
            return this;
        }
        public Builder setUitu_code(String uitu_code){
            this.uitu_code = uitu_code;
            return this;
        }
        public Product built(){
            return new Product(this);
        }
    }
}


