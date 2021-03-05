package com.chain.api;


import java.math.BigInteger;

public class TransactionInfo {

    private String txId;
    private String fromPk;
    private String toPk;
    private String value;
    private String token;
    private BigInteger blockNumber;
    private BigInteger fee;
//    private String nonce;
    private Integer status;
    private Integer decimal;



    public static void main(String[] args) {
        System.out.println("");
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getFromPk() {
        return fromPk;
    }

    public void setFromPk(String fromPk) {
        this.fromPk = fromPk;
    }

    public String getToPk() {
        return toPk;
    }

    public void setToPk(String toPk) {
        this.toPk = toPk;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
        this.fee = fee;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDecimal() {
        return decimal;
    }

    public void setDecimal(Integer decimal) {
        this.decimal = decimal;
    }
}
