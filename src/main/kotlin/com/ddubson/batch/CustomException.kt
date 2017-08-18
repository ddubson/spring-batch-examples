package com.ddubson.batch

class CustomException(override val message: String): RuntimeException(message) {
}