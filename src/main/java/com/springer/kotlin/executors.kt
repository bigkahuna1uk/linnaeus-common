package com.springer.kotlin

import java.time.Duration
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit.MILLISECONDS


fun ScheduledExecutorService.schedule(delay: Duration, task: ()->Unit) = schedule(task, delay.toMillis(), MILLISECONDS)