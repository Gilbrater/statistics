package com.statistics.core;

import com.statistics.core.models.Statistics;
import com.statistics.core.models.Transaction;
import com.statistics.core.utils.TransactionStoreUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CoreApplicationTests {
    @Value("${statistics.interval.in.seconds}")
    private long statisticsInterval;

    @Value("${old.transactions.time.interval}")
    private long oldTransactionsTimeInterval;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private StringBuilder base;

    private double randomAmountMin = 5;
    private double randomAmountMax = 2000;

    @Before
    public void setUp() {
        this.base = new StringBuilder("http://localhost:").append(port).append("/");
        TransactionStoreUtil.getInstance().clear();
    }

    @Test
    public void makeSuccessfulTransactionCall() {
        base.append("transaction");
        Transaction transaction = new Transaction();
        Random random = new Random();

        double randomAmount = random.nextDouble();
        long currentTime = Instant.now().toEpochMilli();


        transaction.setAmount(randomAmount);
        transaction.setTimestamp(currentTime);

        ResponseEntity<String> response = restTemplate.postForEntity(base.toString(), transaction, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(201));
    }

    @Test
    public void makeUnsuccessfulTransactionCall() {
        base.append("transaction");
        Transaction transaction = new Transaction();
        Random random = new Random();

        double randomAmount = random.nextDouble();
        long currentTime = Instant.now().toEpochMilli();
        //Subtract Valid Time in Milliseconds
        currentTime -= (oldTransactionsTimeInterval * 1100);

        transaction.setAmount(randomAmount);
        transaction.setTimestamp(currentTime);

        ResponseEntity<String> response = restTemplate.postForEntity(base.toString(), transaction, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(204));
    }


    @Test
    public void statisticsCallWithNoTransactionWithinValidTimeFrame() {
        base.append("statistics");
        double empty = 0;
        ResponseEntity<Statistics> response = restTemplate.getForEntity(base.toString(), Statistics.class);
        Statistics statistics = response.getBody();
        assertThat(statistics.getAvg(), equalTo(empty));
        assertThat(statistics.getCount(), equalTo(0L));
        assertThat(statistics.getMax(), equalTo(empty));
        assertThat(statistics.getMin(), equalTo(empty));
        assertThat(statistics.getSum(), equalTo(empty));
    }

    @Test
    public void statisticsCallWithOneValidTransaction() {
        int baseUrlLength = base.length();
        base.append("transaction");
        int transactionUrlLength = base.length();

        Transaction transaction = new Transaction();
        Random random = new Random();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        double randomAmount = randomAmountMin + (randomAmountMax - randomAmountMin) * random.nextDouble();
        randomAmount = Double.valueOf(decimalFormat.format(randomAmount));
        long currentTime = Instant.now().toEpochMilli();

        transaction.setAmount(randomAmount);
        transaction.setTimestamp(currentTime);

        ResponseEntity<String> transactionResponse = restTemplate.postForEntity(base.toString(), transaction, String.class);
        assertThat(transactionResponse.getStatusCodeValue(), equalTo(201));

        base.replace(baseUrlLength, transactionUrlLength, "statistics");
        ResponseEntity<Statistics> statisticsResponse = restTemplate.getForEntity(base.toString(), Statistics.class);
        Statistics statistics = statisticsResponse.getBody();

        assertThat(statisticsResponse.getStatusCodeValue(), equalTo(200));
        assertThat(statistics.getAvg(), equalTo(transaction.getAmount()));
        assertThat(statistics.getCount(), equalTo(1L));
        assertThat(statistics.getMax(), equalTo(transaction.getAmount()));
        assertThat(statistics.getMin(), equalTo(transaction.getAmount()));
        assertThat(statistics.getSum(), equalTo(transaction.getAmount()));
    }

    @Test
    public void statisticsCallWithMultipleValidTransactions() throws Exception {
        int baseUrlLength = base.length();
        base.append("transaction");
        int transactionUrlLength = base.length();

        Transaction transaction = new Transaction();
        Random random = new Random();

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double average;
        long count = 0;
        double sum = 0;
        long currentTime;
        double randomAmount;
        ResponseEntity<String> transactionResponse;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        //Make 500 transaction calls within valid time
        for (int i = 0; i < 5; i++) {
            currentTime = Instant.now().toEpochMilli();
            for (int j = 0; j < 100; j++) {
                randomAmount = randomAmountMin + (randomAmountMax - randomAmountMin) * random.nextDouble();
                randomAmount = Double.valueOf(decimalFormat.format(randomAmount));
                transaction.setAmount(randomAmount);
                transaction.setTimestamp(currentTime);

                sum += randomAmount;
                count++;

                max = (randomAmount > max) ? randomAmount : max;
                min = (randomAmount < min) ? randomAmount : min;

                transactionResponse = restTemplate.postForEntity(base.toString(), transaction, String.class);
                assertThat(transactionResponse.getStatusCodeValue(), equalTo(201));
            }
            TimeUnit.SECONDS.sleep(1);
        }
        average = sum / count;

        //Make Statistics Call
        base.replace(baseUrlLength, transactionUrlLength, "statistics");
        ResponseEntity<Statistics> statisticsResponse = restTemplate.getForEntity(base.toString(), Statistics.class);
        Statistics statistics = statisticsResponse.getBody();

        assertThat(statisticsResponse.getStatusCodeValue(), equalTo(200));
        assertThat(Math.round(statistics.getAvg()), equalTo(Math.round(average)));
        assertThat(statistics.getCount(), equalTo(count));
        assertThat(Math.round(statistics.getSum()), equalTo(Math.round(sum)));
        assertThat(statistics.getMax(), equalTo(max));
        assertThat(statistics.getMin(), equalTo(min));
    }
}
