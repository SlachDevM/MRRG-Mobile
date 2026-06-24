package com.slachdevm.mrrgmobile.data.util

import java.io.IOException

/**
 * Exception thrown when the backend returns 401 (Unauthorized) or 403 (Forbidden),
 * indicating that the current session is no longer valid.
 */
class SessionExpiredException(message: String = "Session expired. Please log in again.") : IOException(message)
