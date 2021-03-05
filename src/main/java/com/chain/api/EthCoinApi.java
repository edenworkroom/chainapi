package com.chain.api;

import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EthCoinApi implements BlockChainApi {

    private static final String URL = "http://127.0.0.1:8545/";

    public List<TransactionInfo> handleBlockTransactions(List<TransactionInfo> list, EthBlock ethBlock, String address, Map<String, TokenInfo> wantedToken) {
        if (ethBlock != null && ethBlock.getBlock() != null) {
            List<EthBlock.TransactionResult> result = ethBlock.getBlock().getTransactions();

            if (result != null) {
                for (EthBlock.TransactionResult transactionResult : result) {
                    Transaction tx = (Transaction) transactionResult;
                    TransactionInfo targetTransaction = null;
                    try {
                        targetTransaction = isTargetTransaction(admin, address, wantedToken, tx);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (targetTransaction != null) {
                        list.add(targetTransaction);
                    }

                }
            }
        }
        return list;
    }

    private static boolean isNotEmpty(String str) {
        return str != null && str.trim().length() != 0;
    }

    private TransactionInfo wrapperTransaction(Transaction tx) {
        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setBlockNumber(tx.getBlockNumber());
        transactionInfo.setFee(tx.getGasPrice().multiply(tx.getGas()));
        transactionInfo.setFromPk(tx.getFrom());
        transactionInfo.setToken("");
        transactionInfo.setToPk(tx.getTo());
        transactionInfo.setTxId(tx.getHash());
        transactionInfo.setValue(String.valueOf(tx.getValue()));
        transactionInfo.setStatus(0);
        try {
            EthGetTransactionReceipt ethGetTransactionReceipt = admin.ethGetTransactionReceipt(tx.getHash()).send();
            TransactionReceipt transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt().get();
            if (transactionReceipt != null) {
                if (transactionReceipt.getStatus().equals("0x1")) {
                    transactionInfo.setStatus(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactionInfo;

    }


    private TransactionInfo isTargetTransaction(Admin admin, String address, Map<String, TokenInfo> wantedToken, Transaction tx) throws IOException {
        String from = tx.getFrom().toLowerCase();
        String to = tx.getTo() != null ? tx.getTo().toLowerCase() : "";
        BigInteger value = tx.getValue();
        String token = "ETH";
        Integer decimals = 18;
        String realTransactionTarget = "";
        String inputStr = tx.getInput().toLowerCase();

        if (isNotEmpty(to) && wantedToken.containsKey(to)) {
            TokenInfo tokenInfo = wantedToken.get(to);
            token = tokenInfo.getTokenName();
            decimals = tokenInfo.getDecimal();

            /**前10位代表操作， 在ERC20中 0xa9059cbb 0x23b872dd 表示两种转账 */
            String pre10CharActions = inputStr.substring(0, 10);
            if ("0xa9059cbb".equals(pre10CharActions)) {
                realTransactionTarget = "0x" + inputStr.substring(34, 74);
                value = BigInteger.valueOf(Long.parseLong(inputStr.substring(74), 16));
            } else if ("0x23b872dd".equals(pre10CharActions)) {
                from = "0x" + inputStr.substring(34, 74);
                realTransactionTarget = "0x" + inputStr.substring(98, 138);
                value = BigInteger.valueOf(Long.valueOf(inputStr.substring(138), 16));
            } else {
                return null;
            }

            if (realTransactionTarget.equals(address.toLowerCase())) {
                TransactionInfo transactionInfo = new TransactionInfo();
                transactionInfo.setBlockNumber(tx.getBlockNumber());
                transactionInfo.setFee(tx.getGasPrice().multiply(tx.getGas()));
                transactionInfo.setFromPk(from);
                transactionInfo.setToken(token);
                transactionInfo.setToPk(realTransactionTarget);
                transactionInfo.setTxId(tx.getHash());
                transactionInfo.setValue(String.valueOf(value));
                transactionInfo.setStatus(0);
                transactionInfo.setDecimal(tokenInfo.getDecimal());

                EthGetTransactionReceipt ethGetTransactionReceipt = admin.ethGetTransactionReceipt(tx.getHash()).send();

                if (ethGetTransactionReceipt != null) {
                    TransactionReceipt transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt().get();
                    if (transactionReceipt != null) {
                        if (transactionReceipt.getStatus().equals("0x1")) {
                            transactionInfo.setStatus(1);
                        } else {
                            transactionInfo.setStatus(-1);
                        }
                    }
                }

                return transactionInfo;
            }
        }
        return null;
    }

    private Map<String, TokenInfo> tokens = new HashMap<String, TokenInfo>();


    private EthCoinApi() {
        initAdmin();
    }

    public static Admin initAdmin(HttpService httpService) {
        return Admin.build(httpService);
    }

    private static HttpService getService(String url) {
        return new HttpService(url);
    }

    private static EthCoinApi instance;

    public void initAdmin() {
        this.admin = Admin.build(getService(rpcUrl));
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    private Admin admin;
    public static EthCoinApi getInstance() {
        if (instance == null) {
            instance = new EthCoinApi();
            System.out.println("默认RPC URL IS: " + rpcUrl);
        }
        return instance;
    }

    //    private static final String URL = "http://127.0.0.1:8545/";
    public static String rpcUrl = "http://127.0.0.1:8545/";

    @Override
    public void initRPC(String user, String pass, String url, Integer port) {
        rpcUrl = "http://" + url + ":" + port + "/";
        System.out.println("reset RPC as:" + rpcUrl);
        initAdmin();
    }

    @Override
    public Long getBlockNumber() {
        try {
            EthBlockNumber blockNumber = getAdmin().ethBlockNumber().send();
            return blockNumber.getBlockNumber().longValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    @Override
    public Long getPeerCount() {
        try {
            NetPeerCount peerCount = getAdmin().netPeerCount().send();
            return peerCount.getQuantity().longValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    @Override
    public Long getConfirmedBlock(String txId) {
        try {
            EthTransaction ethTransaction = getAdmin().ethGetTransactionByHash(txId).send();
            Transaction transaction = ethTransaction.getTransaction().get();
            if (transaction != null) {
                return getBlockNumber() - transaction.getBlockNumber().longValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0L;
    }


//    @Override
//    public TransactionInfo getTx(String txId) {
//        try {
//            EthTransaction ethTransaction = admin.ethGetTransactionByHash(txId).send();
//            return wrapperTransaction(ethTransaction.getTransaction().get());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    @Override
    public List<TransactionInfo> getTxList(String address, Long start, Long end) {

        try {
            Admin admin = getAdmin();
            // 0: 传了两个区块高度 1: 只传了一个区块高度
            Long isSingleHeight = 0L;
            // 如果只传一个区块高度或没传
            Long maxHeight = 0L;

            if (start == null && end == null) {
                throw new Exception("请至少上传一个高度值");
            }

            if (start != null && end == null) {
                isSingleHeight = 1L;
                maxHeight = start;
            }

            if (start == null) {
                isSingleHeight = 1L;
                maxHeight = end;
            }

            if (start != null && end != null) {
                if (start > end) {
                    throw new Exception("结束高度不能大于开始高度");
                }
                if (start.equals(end)) {
                    isSingleHeight = 1L;
                    maxHeight = start;
                } else {
                    isSingleHeight = 0L;
                    maxHeight = end;
                }
            }
            List<TransactionInfo> list = new ArrayList<>();
            //如果只传入一个高度,无需循环高度
            if (isSingleHeight == 1) {
                EthBlock ethBlock = admin.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(maxHeight)), true).send();
                list = handleBlockTransactions(list, ethBlock, address, tokens);
            } else {
                for (Long i = start; i < end; i++) {
                    EthBlock ethBlock = admin.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(i)), true).send();
                    list = handleBlockTransactions(list, ethBlock, address, tokens);
                }
            }
            return list;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void initToken(Map<String, TokenInfo> tokens) {
        this.tokens.clear();
        this.tokens = tokens;
    }
}
