final def file = new File(basedir, "custom_log.log")

if (!file.exists())
    throw new Exception("Log file not initialized")

if (!file.text.contains("Logging from TestLogHandlerService: include file not found:"))
    throw new Exception("Expected LogHandler message not found in log file")

return true