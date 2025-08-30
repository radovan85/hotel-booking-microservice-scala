package com.radovan.play.utils

import jakarta.inject.Singleton
import java.sql.Timestamp
import java.time._
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.temporal.ChronoUnit

@Singleton
class TimeConversionUtils {

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  private val zoneId: ZoneId = ZoneId.of("UTC")

  def calculateNumberOfNights(checkInStr: String, checkOutStr: String): Int = {
    try {
      val checkInLocal = LocalDateTime.parse(checkInStr, formatter)
      val checkOutLocal = LocalDateTime.parse(checkOutStr, formatter)

      val checkIn = checkInLocal.atZone(zoneId)
      val checkOut = checkOutLocal.atZone(zoneId)

      val days = ChronoUnit.DAYS.between(checkIn.toLocalDate, checkOut.toLocalDate)
      days.toInt
    } catch {
      case ex: DateTimeParseException =>
        println(s"❌ ERROR calculating nights: ${ex.getMessage}")
        0
    }
  }

  def getCurrentUTCTimestamp: Timestamp = {
    val currentTime = Instant.now().atZone(zoneId)
    Timestamp.valueOf(currentTime.toLocalDateTime)
  }

  def isValidPeriod(checkInStr: String, checkOutStr: String): Boolean = {
    try {
      val checkIn = LocalDateTime.parse(checkInStr, formatter)
      val checkOut = LocalDateTime.parse(checkOutStr, formatter)
      checkOut.isAfter(checkIn)
    } catch {
      case ex: DateTimeParseException =>
        println(s"❌ ERROR validating period: ${ex.getMessage}")
        false
    }
  }

  def stringToTimestamp(str: String): Timestamp = {
    try {
      val zoned = LocalDateTime.parse(str, formatter).atZone(zoneId)
      Timestamp.valueOf(zoned.toLocalDateTime)
    } catch {
      case ex: DateTimeParseException =>
        println(s"❌ ERROR parsing to timestamp: ${ex.getMessage}")
        null
    }
  }

  def timestampToString(timestamp: Timestamp): String = {
    val utcTime = timestamp.toLocalDateTime.atZone(zoneId)
    utcTime.format(formatter)
  }
}
