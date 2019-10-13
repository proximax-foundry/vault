package io.proximax.app.recovery;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author thcao
 */
public class ProxiLicenseHttp {

    protected final WebClient client;
    protected final URL url;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    public ProxiLicenseHttp(String host) throws MalformedURLException {
        this.url = new URL(host);
        final Vertx vertx = Vertx.vertx();
        this.client = WebClient.create(vertx);
        objectMapper.configure(DeserializationFeature.USE_LONG_FOR_INTS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static JsonObject mapJsonObjectOrError(final HttpResponse<JsonObject> response) {
        if (response.statusCode() < 200 || response.statusCode() > 299) {
            throw new RuntimeException(response.statusMessage());
        }
        return response.body();
    }

    static JsonArray mapJsonArrayOrError(final HttpResponse<JsonArray> response) {
        if (response.statusCode() < 200 || response.statusCode() > 299) {
            throw new RuntimeException(response.statusMessage());
        }
        return response.body();
    }

    public Observable<AccountInfo> getAccountInfo(String email) {
        return this.client
                .getAbs(this.url + "/api/recovery/get?email=" + email)
                .as(BodyCodec.jsonObject())
                .rxSend()
                .toObservable()
                .map(ProxiLicenseHttp::mapJsonObjectOrError)
                .map(json -> objectMapper.readValue(json.toString(), AccountInfoDTO.class))
                .map(AccountInfoDTO::getAccountInfo);
    }

    public Observable<AccountStatus> saveAccountInfo(AccountInfo account) {
        JsonObject requestBody = JsonObject.mapFrom(account);        
        return this.client
                .postAbs(this.url + "/api/recovery/save")
                .as(BodyCodec.jsonObject())
                .rxSendJson(requestBody)
                .toObservable()
                .map(ProxiLicenseHttp::mapJsonObjectOrError)
                .map(json -> objectMapper.readValue(json.toString(), AccountStatusDTO.class))
                .map(AccountStatusDTO::getAccountStatus);
    }

}
