package com.chain.api;

public class BlockChainApiFactory {

    public static BlockChainApi getApi(ChainEnum chainEnum) throws Exception {

        if (chainEnum.equals(ChainEnum.ETH)) {
            return EthCoinApi.getInstance();
        } else if (chainEnum.equals(ChainEnum.BTC)){
            return BtcCoinApi.getInstance();
        }
        throw new Exception("接口尚未支持");
    }


}
