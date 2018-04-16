package com.statistics.core.models;

public class SecondsSummary {
    private double totalAmount;
    private long numberOfTransactions;
    private double max;
    private double min;

    public SecondsSummary(double totalAmount, long numberOfTransactions, double max, double min){
        this.totalAmount = totalAmount;
        this.numberOfTransactions=numberOfTransactions;
        this.max=max;
        this.min=min;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(long numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }
}
