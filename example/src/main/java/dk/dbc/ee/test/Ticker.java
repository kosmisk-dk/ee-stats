/*
 * Copyright (C) 2018 DBC A/S (http://dbc.dk/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.ee.test;

import dk.dbc.ee.stats.Timed;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import dk.dbc.ee.stats.LifeCycleMetric;
import dk.dbc.ee.stats.Metered;

/**
 *
 * @author Source (source (at) dbc.dk)
 */
@Singleton
@Startup
@LifeCycleMetric
public class Ticker {

    @Resource
    TimerService ts;

    @Timed
    public Ticker() {
    }

    @PostConstruct
    @Timed
    public void init() {
        TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        ScheduleExpression scheduleExpression = new ScheduleExpression()
                .year("*").month("*").dayOfMonth("*")
                .hour("*").minute("*").second("*/2");
        ts.createCalendarTimer(scheduleExpression, timerConfig);
    }

    @Timeout
    @Timed
//    @Metered
    public void tick() {
        System.out.println("tick tock");
    }
}
