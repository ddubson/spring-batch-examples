package com.ddubson.batch

class CustomRetryableException(override val message: String) : RuntimeException(message)