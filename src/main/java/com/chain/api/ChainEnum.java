package com.chain.api;

public enum ChainEnum {

    BTC("BTC"),
    ETH("ETH");

    ChainEnum(String chainName) {
        this.chainName = chainName;
    }

    private String chainName;

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }
}
