uri = "some uri"
queryParams = list("name"="Alfred","sin"=0,"cos"=0,"tan"=1.55)

doGet <- function(uri, queryParams) {

output = ""
output = paste(
    output,
    "<!DOCTYPE html>
    <html>
    <body>")
output = paste(output,"<b>Here are some evaluations.  (Alfred edit)(againzzz)  Valid functions are name, sin, cos and tan:</b> <br /><br />")

  if(!is.null(queryParams$name)) {
    output = paste(output,"Hello", queryParams$name,"<br />")
  }

  if(!is.null(queryParams$sin)) {
    output = paste(output,"sin(",queryParams$sin,")=",sin(as.numeric(queryParams$sin)),"<br />")
  }

  if(!is.null(queryParams$cos)) {
    output = paste(output,"cos(",queryParams$cos,")=",cos(as.numeric(queryParams$cos)),"<br />")
  }

  if(!is.null(queryParams$tan)) {
    output = paste(output,"tan(",queryParams$tan,")=",tan(as.numeric(queryParams$tan)),"<br />")
  }

    output = paste(
        output,
        "</body>
        </html>")
    #return(json)
    return(output)

}