package com.chain.api;


import com.google.gson.Gson;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BtcCoinApi implements BlockChainApi {

    public static void main(String[] args) {

    }

    private static String URL = "http://btc:btc@127.0.0.1:18888/";

    //检查并返回转入的数值
    private static BigDecimal checkIfTargetTx(String address, BitcoindRpcClient.RawTransaction transaction) {

        if (transaction.vOut() != null && transaction.vOut().size() > 0) {
            for (int i = 0; i < transaction.vOut().size(); i++) {
                BitcoindRpcClient.RawTransaction.Out out = transaction.vOut().get(i);
                if (out.scriptPubKey() != null && out.scriptPubKey().addresses() != null && out.scriptPubKey().addresses().size() > 0) {
                    for (String s : out.scriptPubKey().addresses()) {

                        if (s.equals(address)) {
                            System.out.println((new Gson()).toJson(transaction));
                            return out.value();
                        }
//                        return out.value();
                    }
                }
            }
        }

        return null;
    }
    private static BtcCoinApi instance;


    public static String rpcUrl = "http://btc:btc@127.0.0.1:18888/";
    public static BtcCoinApi getInstance() {
        if (instance == null) {
            instance = new BtcCoinApi();
        }
        return instance;
    }


    BitcoinJSONRPCClient bitcoinClient;

    private BtcCoinApi() {
        try {
            bitcoinClient = new BitcoinJSONRPCClient(rpcUrl);
            System.out.println("默认RPC URL IS: " + rpcUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initRPC(String user, String pass, String url, Integer port) {
        rpcUrl = "http://" + user + ':' + pass + "@" + url + ":" + port + "/";
        try {
            bitcoinClient = new BitcoinJSONRPCClient(rpcUrl);
            System.out.println("初始化RPC URL IS: " + rpcUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Long getBlockNumber() {
        String bestBlockHash = bitcoinClient.getBestBlockHash();
        BitcoindRpcClient.Block block = bitcoinClient.getBlock(bestBlockHash);
        if (block != null) {
            return block.height() * 1L;
        }
        return 0L;
    }

    @Override
    public Long getPeerCount() {
        return bitcoinClient.getPeerInfo().size() * 1L;
    }

    @Override
    public Long getConfirmedBlock(String txId) {
        BitcoindRpcClient.RawTransaction transaction =  bitcoinClient.getRawTransaction(txId);
        return transaction.confirmations().longValue();
    }

//    @Override
//    public TransactionInfo getTx(String txId) {
//
//        return null;
//    }

    public TransactionInfo wrapperTransaction(BitcoindRpcClient.Block block, BitcoindRpcClient.RawTransaction transaction, BigDecimal value) {

        System.out.println((new Gson()).toJson(block) + " +++ " + (new Gson()).toJson(transaction));

        TransactionInfo transactionInfo = new TransactionInfo();
        //块高度
        transactionInfo.setBlockNumber(BigInteger.valueOf(block.height()));
        transactionInfo.setValue(String.valueOf(value));
        transactionInfo.setTxId(transaction.txId());
        //用户地址
        transactionInfo.setToken("BTC");
        transactionInfo.setDecimal(18);
        // -1 冲突 0=待打包 >0 打包成功
        Integer status = transaction.confirmations() ;
        if (transaction.confirmations() > 0) {
            status = 1;
        }
        transactionInfo.setStatus(status);
        return transactionInfo;

    }

    @Override
    public List<TransactionInfo> getTxList(String address, Long start, Long end) {

        List<TransactionInfo> list = new ArrayList<>();
        try {
            // 0: 传了两个区块高度 1: 只传了一个区块高度
            Integer isSingleHeight = 0;
            // 如果只传一个区块高度或没传
            Long maxHeight = 0L;

            if (start == null && end == null) {
                throw new Exception("请至少上传一个高度值");
            }

            if (start != null && end == null) {
                isSingleHeight = 1;
                maxHeight = start;
            }

            if (start == null) {
                isSingleHeight = 1;
                maxHeight = end;
            }

            if (start != null && end != null) {
                if (start > end) {
                    throw new Exception("结束高度不能大于开始高度");
                }
                if (start.equals(end)) {
                    isSingleHeight = 1;
                    maxHeight = start;
                } else {
                    isSingleHeight = 0;
                    maxHeight = end;
                }
            }

            Long currentMaxHeight = getBlockNumber();
            //判断传入的最大高度是否大于链的最大高度
            if (currentMaxHeight < maxHeight) {
               maxHeight = currentMaxHeight;
               end = maxHeight;
            }

            //如果只传入一个高度,无需循环高度
            if (isSingleHeight == 1) {

                BitcoindRpcClient.Block block = bitcoinClient.getBlock(maxHeight.intValue());
                list = handleBlockTransactions(list, block, address);

            } else {

                for (Long i = start; i <= end; i++) {

                    BitcoindRpcClient.Block block = bitcoinClient.getBlock(i.intValue());
                    list = handleBlockTransactions(list, block, address);
                }
            }

            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<TransactionInfo> handleBlockTransactions(List<TransactionInfo> list, BitcoindRpcClient.Block block, String address) {
        if (block != null) {
            List<String> txs = block.tx();
            for (String tx : txs) {
                BitcoindRpcClient.RawTransaction transaction = bitcoinClient.getRawTransaction(tx);
                BigDecimal value = checkIfTargetTx(address, transaction);
                //符合地址条件
                if (value != null) {
                    TransactionInfo transactionInfo = wrapperTransaction(block, transaction, value);
                    list.add(transactionInfo);
                }
            }
        }
        return list;
    }


    @Override
    public void initToken(Map<String, TokenInfo> tokens) {
        /**
         * do nothing
         */
    }
}
