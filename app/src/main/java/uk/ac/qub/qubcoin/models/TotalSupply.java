package uk.ac.qub.qubcoin.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TotalSupply {

    @SerializedName("totalSupply")
    @Expose
    private String totalSupply;

    public String getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(String totalSupply) {
        this.totalSupply = totalSupply;
    }

}
