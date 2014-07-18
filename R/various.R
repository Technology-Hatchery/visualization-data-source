## From JSON
library("jsonlite")

#Get Survey
file_path <- "../../../../../json/mobinsight/Survey.json"
file_con = file(description = file_path, open = "r", blocking = TRUE, encoding = getOption("encoding"), raw = FALSE)
lines = readLines(con = file_con, n = -1L, ok = TRUE, encoding = "unknown", skipNul = FALSE)
survey <- fromJSON(lines)

#Get Responses
file_path <- "../../../../../json/mobinsight/Analytics.json"
file_con = file(description = file_path, open = "r", blocking = TRUE, encoding = getOption("encoding"), raw = FALSE)
lines = readLines(con = file_con, n = -1L, ok = TRUE, encoding = "unknown", skipNul = FALSE)
responses <- fromJSON(lines)