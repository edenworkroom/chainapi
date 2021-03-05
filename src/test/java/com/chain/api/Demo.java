package com.chain.api;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Demo {
    public static void main(String[] args) throws Exception {


        /**
         *  以太坊调用示例
         */

        /**
         * 获取对应的API （ChainEnum.BTC | ChainEnum.ETH)
         */
        BlockChainApi chainApi = BlockChainApiFactory.getApi(ChainEnum.ETH);


        /**
         *  针对ETH设置需要获取的TOKEN信息 （BTC 无需设置）
         *
         *  ***仅需配置一次***
         */
        Map<String, TokenInfo> tokens = new HashMap<String, TokenInfo>();
        tokens.put("0x51c3881d1c4fdced6304a921ece64aa2146e208a", new TokenInfo("0x51c3881d1c4fdced6304a921ece64aa2146e208a", "PIST", 9));
        chainApi.initToken(tokens);


        /**
         * 针对不同的节点地址进行RPC配置
         *
         * ***仅需配置一次***
         *
         * RPC用户（BTC) | RPC密码 (BTC) | RPC 地址 | RPC 端口
         */
        chainApi.initRPC("btc", "btc" , "127.0.0.1", 8545);


        System.out.println("peer count:" + chainApi.getPeerCount());


        System.out.println("block number:" + chainApi.getBlockNumber());


        System.out.println("confirmed block: " + chainApi.getConfirmedBlock("0x03f2b9b4deab37d8a713b7585c642c64dcf5c0aeec333db969ef237a29749c1e"));


        System.out.println(new Gson().toJson(chainApi.getTxList("0xd70432b9ea97833e7501c32886021f3190cb3cd5", 1L, 500L)));



        /***
         *  BTC 调用示例
         */

        /**
         * 获取对应的API （ChainEnum.BTC | ChainEnum.ETH)
         */
        chainApi = BlockChainApiFactory.getApi(ChainEnum.BTC);

        /**
         * 针对不同的节点地址进行RPC配置
         *
         * ***仅需配置一次***
         *
         * RPC用户（BTC) | RPC密码 (BTC) | RPC 地址 | RPC 端口
         */
        chainApi.initRPC("btc", "btc" , "127.0.0.1", 18888);


        System.out.println("peer count:" + chainApi.getPeerCount());


        System.out.println("block number:" + chainApi.getBlockNumber());


        System.out.println("confirmed block: " + chainApi.getConfirmedBlock("c7897278cd01e3434b384f9ae5ece19a4d385d313b4a2695f712fe211dfa81ab"));


        System.out.println(new Gson().toJson(chainApi.getTxList("0xd70432b9ea97833e7501c32886021f3190cb3cd5", 1L, 500L)));
    }

}
