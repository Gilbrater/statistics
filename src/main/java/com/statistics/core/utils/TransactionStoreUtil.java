package com.statistics.core.utils;

import com.statistics.core.models.SecondsSummary;
import com.statistics.core.models.Statistics;
import com.statistics.core.models.Transaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TransactionStoreUtil{
    private static com.statistics.core.utils.TransactionStoreUtil instance;
    private static ConcurrentMap<Long, SecondsSummary> transactionSummaryPerSecond;

    private TransactionStoreUtil(){
        transactionSummaryPerSecond = new ConcurrentHashMap<>();
    }

    public static TransactionStoreUtil getInstance(){
        if (instance==null) {
            instance = new com.statistics.core.utils.TransactionStoreUtil();
        }
        return instance;
    }

    public synchronized void addTransaction(Transaction transaction, Long transactionTimeInEpochSec, long currentTime, long interval){

    }

    public Statistics getStatistics(long currentTime, long interval){
        return new Statistics();
    }
}
