package com.statistics.core.controllers.v1;

import com.statistics.core.models.Statistics;
import com.statistics.core.models.Transaction;
import com.statistics.core.services.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.Instant;

@RestController
@RequestMapping()
public class StatisticsController {
    @Autowired
    private StatisticsService statisticsService;
    private int success = 201;
    private int failureOldTransaction = 204;


    @RequestMapping(value = "/transaction", method= RequestMethod.POST)
    @ResponseBody
    public void saveTransaction(HttpServletResponse response, @RequestBody Transaction transaction){
        long currentTime = Instant.now().getEpochSecond();
        boolean done = statisticsService.saveTransaction(transaction, currentTime);

        if(done){
            response.setStatus(success);
        }else{
            response.setStatus(failureOldTransaction);
        }
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public Statistics getStatistics() {
        long currentTime = Instant.now().getEpochSecond();
        return statisticsService.getStatistics(currentTime);
    }
}

