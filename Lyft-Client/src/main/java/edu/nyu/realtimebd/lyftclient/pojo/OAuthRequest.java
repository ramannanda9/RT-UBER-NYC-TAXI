package edu.nyu.realtimebd.lyftclient.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ramandeep Singh on 14-04-2016.
 */
public class OAuthRequest {
      //The scopeTypes for the lyft API
      public enum ScopeType {
      PUBLIC ("public"),
      RIDES_READ("rides.read"),
      OFFLINE ("offline"),
      RIDES_REQUEST ("rides.request");
      private final String scopeName;
      private ScopeType(String value){
        this.scopeName=value;
        }

          public String getScopeName() {
              return scopeName;
          }
      }
    @SerializedName("grant_type")
    private String grantType;
    @SerializedName("scope")
    private String scope;

    /**
     * @param grantType The grantType that you are seeking refresh, client_credentials
     * @param scope public, rides.read, offline, rides.request
     */
    public OAuthRequest(String grantType,String scope) {

        this.grantType = grantType;
        this.scope=scope;
    }


    public String getGrantType() {

        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }



}
