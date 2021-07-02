package uk.ac.qub.qubcoin.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BalanceOf {

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("balance")
    @Expose
    private String balance;

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getBalance() { return balance; }

    public void setBalance(String balance) { this.balance = balance; }
}
