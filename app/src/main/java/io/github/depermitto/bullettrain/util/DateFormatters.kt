package io.github.depermitto.bullettrain.util

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern

object DateFormatters {
  val MMMM_yyyy: DateTimeFormatter = ofPattern("MMMM yyyy")
  val MMM_yyyy: DateTimeFormatter = ofPattern("MMM yyyy")
  val MMMM_d_yyyy: DateTimeFormatter = ofPattern("MMMM d yyyy")
  val MMM_d_yyyy: DateTimeFormatter = ofPattern("MMM d yyyy")
  val yyyy: DateTimeFormatter = ofPattern("yyyy")
  val MMM_dd: DateTimeFormatter = ofPattern("MMM dd")
  val EEEE_MMMM_d_yyyy: DateTimeFormatter = ofPattern("EEEE, MMMM d yyyy")
  val yyyy_MM_dd: DateTimeFormatter = ofPattern("yyy-MM-dd")

  val m_ss: DateTimeFormatter = ofPattern("m:ss").withZone(ZoneId.systemDefault())
  val mm_ss: DateTimeFormatter = ofPattern("mm:ss").withZone(ZoneId.systemDefault())
  val kk_mm: DateTimeFormatter = ofPattern("kk:mm").withZone(ZoneId.systemDefault())
}
