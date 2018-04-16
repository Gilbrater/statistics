package com.statistics.core.services;

import com.statistics.core.models.Statistics;
import com.statistics.core.models.Transaction;
import com.statistics.core.utils.TransactionStoreUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class StatisticsService {
    private TransactionStoreUtil transactionStoreUtil;

    @Value("${statistics.interval.in.seconds}")
    private long statisticsInterval;

    @Value("${old.transactions.time.interval}")
    private long oldTransactionsTimeInterval;

    public StatisticsService(){
        transactionStoreUtil = TransactionStoreUtil.getInstance();
    }

    //Transaction transaction, Long transactionTimeInEpochSec, long currentTime, long interval
    public boolean saveTransaction(Transaction transaction, Long currentTime){
        Instant transactionTimeInstant = Instant.ofEpochMilli(transaction.getTimestamp());
        long transactionTime = transactionTimeInstant.getEpochSecond();
        boolean isValid = isWithinValidTime(transactionTime, currentTime);
        if(isValid){
            transactionStoreUtil.addTransaction(transaction, transactionTime, currentTime, oldTransactionsTimeInterval);
        }
        return isValid;
    }

    public Statistics getStatistics(long currentTime){
        return transactionStoreUtil.getStatistics(currentTime, statisticsInterval);
    }

    private boolean isWithinValidTime(Long transactionTime, Long currentTime){
        return ((currentTime - transactionTime) <= oldTransactionsTimeInterval);
    }
}
