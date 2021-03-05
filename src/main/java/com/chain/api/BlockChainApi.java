package com.chain.api;

import java.util.List;
import java.util.Map;

public interface BlockChainApi {

    void initRPC(String user, String pass, String url, Integer port);

    Long getBlockNumber();

    Long getPeerCount();

    Long getConfirmedBlock(String txId);

//    TransactionInfo getTx(String txId);

    List<TransactionInfo> getTxList(String pk, Long start, Long end);

    void initToken(Map<String, TokenInfo> tokens);

}
