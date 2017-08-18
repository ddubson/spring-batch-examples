package com.ddubson.batch

import org.springframework.batch.core.SkipListener

class CustomSkipListener: SkipListener<Any,Any> {
    override fun onSkipInProcess(item: Any?, t: Throwable?) {
        println(">> Skipping $item because processing failed: $t")
    }

    override fun onSkipInWrite(item: Any?, t: Throwable?) {
        println(">> Skipping $item because writing failed: $t")
    }

    override fun onSkipInRead(t: Throwable?) {
    }
}