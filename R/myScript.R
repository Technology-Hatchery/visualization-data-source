# Testing
x <- 2
y <- 4
m <- 1:10
dim(m) <- c(2,5)


x<-data.frame(foo=1:4,bar=c(T,T,F,F))
nrow(x)
ncol(x)

m <- matrix(1:9,nrow=3,ncol=3)
dimnames(m) <- list(c("a","b","c"),c("d","e","f"))

setwd("./")

# Testing function
myFunction = function(bool) {
    if(bool) {
        return("Hello World")
    } else {
        return("Hello Nobody")
    }
}

# Read from mySQL database

ucscDb <- dbConnect(MySQL(),user="genome",host="genome-mysql.cse.ucsc.edu")
result <- dbGetQuery(ucscDb,"show databases;")
dbDisconnect(ucscDb)

# Get Google Map Data
library(ggmap)
## example of map of Dhangadhi, Nepal  
dhanmap1 = get_map(location = c(lon = 80.56410278, lat = 28.7089375), zoom = 12, maptype = 'roadmap', source = "google")
dhanmap1 = ggmap(dhanmap1)
dhanmap1 

## plotting over map 

dhanmap5 = get_map(location = c(lon = 80.56410278, lat = 28.7089375), zoom = 14, maptype = 'roadmap', source = "google")
dhanmap5 = ggmap(dhanmap5)

## data
set.seed(1234)
lon <- runif (40, 80.54, 80.59)
lat <- runif (40, 28.69, 28.73) 
varA = rnorm (40, 20, 10)
myd <- data.frame (lon, lat, varA)

## the bubble chart
library(grid)
dhanmap5 +   geom_point(aes(x = lon, y = lat, colour = varA, size = varA, alpha = 0.9), data = myd)  + scale_colour_gradient(low="yellow", high="red")



# plotting world cities
require(maps)
map("world")
data(world.cities)
## cities with minimum 20,000 population
map.cities(world.cities, country = "", minpop = 20000, maxpop = Inf,
           pch = ".", col = "red")

# Read from text file

## Using readLines
file_path <- paste(getwd(),"Data/Textual Analysis/Text.txt",sep="/")
file_con = file(description = file_path, open = "r", blocking = TRUE, encoding = getOption("encoding"), raw = FALSE)
lines = readLines(con = file_con, n = -1L, ok = TRUE, warn = TRUE, encoding = "unknown", skipNul = FALSE)
x <- 7

## Using read.table
file_path <- paste(getwd(),"Data/Sample - Superstore Sales (Excel).csv",sep="/")
save_path <- paste(getwd(),"Data/saved.Rda",sep="/")
library(Defaults)
setDefaults('as.Date.character', format = '%m/%d/%Y')
colClasses_values <- c("numeric","numeric","Date","character","numeric","numeric","numeric","character","numeric","numeric","numeric","character","character","character","character","character","character","character","character","numeric","Date")
dataFrame <- read.csv(file_path,header=TRUE,sep=",",quote="\"",dec=".",comment.char="",row.names=NULL,colClasses=colClasses_values)
#dataFrame <- read.csv(file_path,header=TRUE,sep=",",quote="\"",dec=".",comment.char="",row.names=NULL)
save(dataFrame,file=save_path)
# Subset the results
str(dataFrame[dataFrame$Row.ID %in% c(1,49),])

## From JSON
file_path <- paste(getwd(),"Data/square.csv",sep="/")
file_con = file(description = file_path, open = "r", blocking = TRUE, encoding = getOption("encoding"), raw = FALSE)
lines = readLines(con = file_con, n = -1L, ok = TRUE, warn = TRUE, encoding = "unknown", skipNul = FALSE)
jsonData <- fromJSON(lines)

## From Twitter
API_key <- "M0QtsPiAT92DoFKCZZ7QlRitZ"
API_secret <- "99UqDRqGK0Vit1VJspIbKqKCrVhlNYdQvIG99dkFhyNSH0si8f"
Access_token <- "2521122996-2Ct0A0YMalz0YbatR2A6xZzMg9NSWLTQUYHVqbD"
Access_token_secret <- "6gjXNlTTLUZGnKEzbkazMcipNbgmXM5uOhwP5s3LDfHRY"
getTwitterOAuth(consumer_key=API_key,consumer_secret=API_secret)

requestURL <-  "https://api.twitter.com/oauth/request_token"
accessURL =    "https://api.twitter.com/oauth/access_token"
authURL =      "https://api.twitter.com/oauth/authorize"
consumerKey =   "M0QtsPiAT92DoFKCZZ7QlRitZ"
consumerSecret = "99UqDRqGK0Vit1VJspIbKqKCrVhlNYdQvIG99dkFhyNSH0si8f"
twitCred <- OAuthFactory$new(consumerKey=consumerKey,
                             consumerSecret=consumerSecret,
                             requestURL=requestURL,
                             accessURL=accessURL,
                             authURL=authURL)
download.file(url="http://curl.haxx.se/ca/cacert.pem",
              destfile="cacert.pem")
twitCred$handshake(cainfo="cacert.pem")
#save for later use for Windows
save(twitCred, file="twitter_authentication.Rdata")
registerTwitterOAuth(twitCred)

# Grab information
me <- getUser("DiscoveryDash", cainfo="cacert.pem")
followers <- me$getFollowers(cainfo="cacert.pem")

# Loop
for(i in 1:10) {
    print(i)  
}

# Function Creation
add2 <- function(x, y) {
    x+y
}

# Word Cloud
words <- Corpus(DirSource("X:/Dropbox/Technology Hatchery Inc/technical/Git/Rcode/Data/Textual Analysis/"))
words <- tm_map(wsj,stripWhitespace)
words <- tm_map(wsj,tolower)
words <- tm_map(wsj,removeWords,stopwords("english"))
words <- tm_map(wsj,stemDocument)

wordcloud(lords, scale=c(5,0.5), max.words=100, random.order=FALSE, rot.per=0.35, use.r.layout=FALSE, colors=brewer.pal(8, “Dark2″))