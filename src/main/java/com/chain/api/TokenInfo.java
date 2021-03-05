package com.chain.api;

public class TokenInfo {
    /**
     * 合约地址
     */
    private String txId;
    /**
     * Token名称
     */
    private String tokenName;
    /**
     * 精度
     */
    private Integer decimal;

    public TokenInfo(String txId, String tokenName, Integer decimal) {
        this.txId = txId;
        this.tokenName = tokenName;
        this.decimal = decimal;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public Integer getDecimal() {
        return decimal;
    }

    public void setDecimal(Integer decimal) {
        this.decimal = decimal;
    }
}
