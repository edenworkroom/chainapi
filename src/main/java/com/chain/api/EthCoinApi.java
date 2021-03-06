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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EthCoinApi implements BlockChainApi {

    private static final String URL = "http://127.0.0.1:8545/";

    static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(50);
    public List<TransactionInfo> handleBlockTransactions(List<TransactionInfo> list, EthBlock ethBlock, String address, Map<String, TokenInfo> wantedToken) {
        if (ethBlock != null && ethBlock.getBlock() != null) {
            List<EthBlock.TransactionResult> result = ethBlock.getBlock().getTransactions();

            if (result != null) {
                List<Future<TransactionInfo>> futures = new ArrayList<Future<TransactionInfo>>();
                for (EthBlock.TransactionResult transactionResult : result) {
                    Transaction tx = (Transaction) transactionResult;
                    futures.add(fixedThreadPool.submit(new Callable<TransactionInfo>(){
                        @Override
                        public TransactionInfo call() throws Exception {
                            TransactionInfo targetTransaction = null;
                            try {
                                targetTransaction = isTargetTransaction(admin, address, wantedToken, tx);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return targetTransaction;
                        }
                    }));
                }

                for (Future<TransactionInfo> future : futures) {
                    TransactionInfo tx;
                    try {
                        tx = future.get();
                        if (tx != null) {
                            list.add(tx);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
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
        String inputStr = tx.getInput().toLowerCase();

        if (isNotEmpty(to)) {
            if(wantedToken.containsKey(to)) {
                TokenInfo tokenInfo = wantedToken.get(to);
                token = tokenInfo.getTokenName();
                decimals = tokenInfo.getDecimal();
    
                /**???10?????????????????? ???ERC20??? 0xa9059cbb 0x23b872dd ?????????????????? */
                String pre10CharActions = inputStr.substring(0, 10);
                if ("0xa9059cbb".equals(pre10CharActions)) {
                    to = "0x" + inputStr.substring(34, 74);
                    value = BigInteger.valueOf(Long.parseLong(inputStr.substring(74), 16));
                } else if ("0x23b872dd".equals(pre10CharActions)) {
                    from = "0x" + inputStr.substring(34, 74);
                    to = "0x" + inputStr.substring(98, 138);
                    value = BigInteger.valueOf(Long.valueOf(inputStr.substring(138), 16));
                } else {
                    return null;
                }
            }
           
            if (to.equals(address.toLowerCase())) {
                TransactionInfo transactionInfo = new TransactionInfo();
                transactionInfo.setBlockNumber(tx.getBlockNumber());
                transactionInfo.setFee(tx.getGasPrice().multiply(tx.getGas()));
                transactionInfo.setFromPk(from);
                transactionInfo.setToken(token);
                transactionInfo.setToPk(to);
                transactionInfo.setTxId(tx.getHash());
                transactionInfo.setValue(String.valueOf(value));
                transactionInfo.setStatus(0);
                transactionInfo.setDecimal(decimals);

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
            System.out.println("??????RPC URL IS: " + rpcUrl);
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
            // 0: ???????????????????????? 1: ???????????????????????????
            Long isSingleHeight = 0L;
            // ???????????????????????????????????????
            Long maxHeight = 0L;

            if (start == null && end == null) {
                throw new Exception("??????????????????????????????");
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
                    throw new Exception("????????????????????????????????????");
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
            //???????????????????????????,??????????????????
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
