package kr.co.bootpay.bio.models;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.bootpay.android.models.BootExtra;
import kr.co.bootpay.android.models.BootItem;
import kr.co.bootpay.android.models.BootUser;
import kr.co.bootpay.android.models.Payload;

//import kr.co.bootpay.core.models.BootExtra;
//import kr.co.bootpay.core.models.BootItem;
//import kr.co.bootpay.android.models.BootUser;

public class BioPayload  {
    String applicationId = "";
    String pg = "";
    String method = "";
    List<String> methods = new ArrayList<>();
    String orderName = "";
    Double price = 0.0;
    Double taxFree = 0.0;
    String orderId = "";
    String subscriptionId = "";
    String authenticationId = "";
//    String otp = "";

    private String walletId;
    private String token;
    private String authenticateType;
    private String userToken;
    private Map<String, Object> metadata = new HashMap<>();

    private BioExtra extra;
    private BootUser user;
    List<BootItem> items = new ArrayList<>();
    private List<String> names;
    private List<BioPrice> prices;
    private int imageResources = -1;

    public String getApplicationId() {
        return applicationId;
    }

    public BioPayload setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public String getPg() {
        return pg;
    }

    public BioPayload setPg(String pg) {
        this.pg = pg;
        return this;
    }

//    public String getOtp() {
//        return otp;
//    }
//
//    public void setOtp(String otp) {
//        this.otp = otp;
//    }

    public String getMethod() {
        return method;
    }

    public BioPayload setMethod(String method) {
        this.method = method;
        return this;
    }

    public List<String> getMethods() {
        return methods;
    }

    public BioPayload setMethods(List<String> methods) {
        this.methods = methods;
        return this;
    }

    public String getOrderName() {
        return orderName;
    }

    public BioPayload setOrderName(String orderName) {
        this.orderName = orderName;
        return this;
    }

    public Double getPrice() {
        return price;
    }

    public BioPayload setPrice(Double price) {
        this.price = price;
        return this;
    }

    public Double getTaxFree() {
        return taxFree;
    }

    public BioPayload setTaxFree(Double taxFree) {
        this.taxFree = taxFree;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public BioPayload setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public BioPayload setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getAuthenticationId() {
        return authenticationId;
    }

    public BioPayload setAuthenticationId(String authenticationId) {
        this.authenticationId = authenticationId;
        return this;
    }

    public String getWalletId() {
        return walletId;
    }

    public BioPayload setWalletId(String walletId) {
        this.walletId = walletId;
        return this;
    }

    public String getToken() {
        return token;
    }

    public BioPayload setToken(String token) {
        this.token = token;
        return this;
    }

    public String getAuthenticateType() {
        return authenticateType;
    }

    public BioPayload setAuthenticateType(String authenticateType) {
        this.authenticateType = authenticateType;
        return this;
    }

    public String getUserToken() {
        return userToken;
    }

    public BioPayload setUserToken(String userToken) {
        this.userToken = userToken;
        return this;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public BioPayload setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public BioExtra getExtra() {
        return extra;
    }

    public BioPayload setExtra(BioExtra extra) {
        this.extra = extra;
        return this;
    }

    public BootUser getUser() {
        return user;
    }

    public BioPayload setUser(BootUser user) {
        this.user = user;
        return this;
    }

    public List<BootItem> getItems() {
        return items;
    }

    public BioPayload setItems(List<BootItem> items) {
        this.items = items;
        return this;
    }

    public List<String> getNames() {
        return names;
    }

    public BioPayload setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public List<BioPrice> getPrices() {
        return prices;
    }

    public BioPayload setPrices(List<BioPrice> prices) {
        this.prices = prices;
        return this;
    }

    public int getImageResources() {
        return imageResources;
    }

    public BioPayload setImageResources(int imageResources) {
        this.imageResources = imageResources;
        return this;
    }


//    public String toJsonUnderscore() {
//        Gson gson = new GsonBuilder()
//                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//                .create();
//        return gson.toJson(this);
//    }




    public String toJsonUnderscore() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("application_id", applicationId);
            jsonObject.put("pg", pg);
            if(methods.size() > 0) {
                jsonObject.put("method", new JSONArray(methods));
            } else {
                jsonObject.put("method", method);
            }
            jsonObject.put("order_name", orderName);
            jsonObject.put("price", price);
            jsonObject.put("tax_free", taxFree);

            jsonObject.put("order_id", orderId);
            jsonObject.put("subscription_id", subscriptionId);
            jsonObject.put("authentication_id", authenticationId);

            jsonObject.put("wallet_id", walletId);
            jsonObject.put("token", token);
            jsonObject.put("authenticate_type", authenticateType);
            jsonObject.put("user_token", userToken);

            jsonObject.put("extra", extra.toJsonObject());
            jsonObject.put("user", user.toJsonObject());

            if(items.size() > 0) {
                List<JSONObject> itemList = new ArrayList<>();
                for(BootItem item : items) {
                    itemList.add(item.toJsonObject());
                }
                jsonObject.put("items", new JSONArray(itemList));
            }

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            return gson.toJson(this);
        }
    }


    public String toJsonUnderscoreEasyPay() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("application_id", applicationId);
            jsonObject.put("pg", pg);
            if(methods.size() > 0) {
                jsonObject.put("method", new JSONArray(methods));
            } else {
                jsonObject.put("method", method);
            }
            jsonObject.put("order_name", orderName);
            jsonObject.put("price", price);
            jsonObject.put("tax_free", taxFree);

            jsonObject.put("order_id", orderId);
            jsonObject.put("subscription_id", subscriptionId);
            jsonObject.put("authentication_id", authenticationId);

            jsonObject.put("wallet_id", walletId);
            jsonObject.put("token", token);
            jsonObject.put("authenticate_type", authenticateType);
            jsonObject.put("user_token", userToken);

            jsonObject.put("extra", extra.toJsonObjectEasyPay());
            jsonObject.put("user", user.toJsonObject());

            if(items.size() > 0) {
                List<JSONObject> itemList = new ArrayList<>();
                for(BootItem item : items) {
                    itemList.add(item.toJsonObject());
                }
                jsonObject.put("items", new JSONArray(itemList));
            }

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            return gson.toJson(this);
        }
    }

//    public static BioPayload fromJson(String json) {
//        Gson gson = new GsonBuilder()
//                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//                .create();
//
//        BioPayload payload = gson.fromJson(json, BioPayload.class);
////        if(payload.metadata != null && payload.paramJson.length() > 0) {
////            payload.params = new Gson().fromJson(
////                    payload.paramJson, new TypeToken<HashMap<String, Object>>() {}.getType()
////            );
////        }
//
//        return payload;
//    }
}
