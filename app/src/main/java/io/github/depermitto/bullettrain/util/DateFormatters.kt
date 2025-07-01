package io.github.depermitto.bullettrain.util

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern

object DateFormatters {
  val MMMM_yyyy: DateTimeFormatter = ofPattern("MMMM yyyy")
  val MMMM_d_yyyy: DateTimeFormatter = ofPattern("MMMM d, yyyy")
  val MMM_dd_yyyy: DateTimeFormatter = ofPattern("MMM dd yyyy")
  val MMM_dd: DateTimeFormatter = ofPattern("MMM dd")
  val EEEE_MMM_dd_yyyy: DateTimeFormatter = ofPattern("EEEE, MMM dd yyyy")
  val yyyy_MM_dd: DateTimeFormatter = ofPattern("yyy-MM-dd")

  val m_ss: DateTimeFormatter = ofPattern("m:ss").withZone(ZoneId.systemDefault())
  val mm_ss: DateTimeFormatter = ofPattern("mm:ss").withZone(ZoneId.systemDefault())
}
