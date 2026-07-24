package api

class NavigationRequestException(
  private val code: String,
  message: String,
  private val details: java.util.Map[String, Object]
) extends RuntimeException(message) {
  def getCode: String = code
  def getDetails: java.util.Map[String, Object] = details
}
