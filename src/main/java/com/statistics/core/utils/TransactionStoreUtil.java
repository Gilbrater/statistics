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
        SecondsSummary secondsSummary;
        double transactionAmount = transaction.getAmount();
        if(!transactionSummaryPerSecond.containsKey(transactionTimeInEpochSec)){
            secondsSummary = new SecondsSummary(transactionAmount, 1, transactionAmount, transactionAmount);
        }else{
            secondsSummary = transactionSummaryPerSecond.get(transactionTimeInEpochSec);
            double totalAmount = secondsSummary.getTotalAmount();
            long numberOfTransactions = secondsSummary.getNumberOfTransactions();
            double maxForSecond = secondsSummary.getMax();
            double minForSecond = secondsSummary.getMin();

            secondsSummary.setTotalAmount(totalAmount+transactionAmount);
            secondsSummary.setNumberOfTransactions(numberOfTransactions+1);
            secondsSummary.setMax((transactionAmount>maxForSecond)?transactionAmount:maxForSecond);
            secondsSummary.setMin((transactionAmount<minForSecond)?transactionAmount:minForSecond);
        }
        transactionSummaryPerSecond.put(transactionTimeInEpochSec, secondsSummary);
        expireOldRecords(currentTime, interval);
    }

    private void expireOldRecords( long currentTime, long interval){
        ConcurrentMap<Long, SecondsSummary> newestTransactions = new ConcurrentHashMap<>();
        for(long i=currentTime; i>currentTime-interval; i--){
            SecondsSummary secondsSummary = transactionSummaryPerSecond.get(i);
            if(secondsSummary!=null){
                newestTransactions.put(i, secondsSummary);
            }
        }
        transactionSummaryPerSecond=newestTransactions;
    }

    public Statistics getStatistics(long currentTime, long interval){
        Statistics statistics = new Statistics();
        double totalAmount = 0;
        long totalNumberOfTransactions = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double average = 0;

        double secondsMax;
        double secondsMin;

        for(long i=currentTime; i>currentTime-interval; i--){
            SecondsSummary secondsSummary = transactionSummaryPerSecond.get(i);
            if(secondsSummary!=null){
                totalAmount+=secondsSummary.getTotalAmount();
                totalNumberOfTransactions+=secondsSummary.getNumberOfTransactions();
                max = Math.max(max, secondsSummary.getMax());
                min = Math.min(min, secondsSummary.getMin());
            }
        }
        if(totalNumberOfTransactions==0){
            max=0;
            min=0;
        }else{
            average = totalAmount/totalNumberOfTransactions;
        }

        statistics.setSum(totalAmount);
        statistics.setAvg(average);
        statistics.setCount(totalNumberOfTransactions);
        statistics.setMax(max);
        statistics.setMin(min);
        return statistics;
    }

    public synchronized void clear() {
        transactionSummaryPerSecond = new ConcurrentHashMap<>();
    }
}
