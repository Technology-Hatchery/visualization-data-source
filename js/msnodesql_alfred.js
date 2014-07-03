/**
 * Created by Alfred on 6/30/2014.
 */
function databaseData() {
    var sql = require('msnodesql');
    var util = require('util');
//
    var connStr = "Driver={SQL Server Native Client 11.0};Server=http://54.85.42.208,1433;Database=DB;UID=alfred.wechselberger@technologyhatchery.com;PWD=Cc17931793;";
    var query = 'SELECT TOP 1000 [name], [location], [qty] FROM [alfred].[dbo].[coffee]';

    sql.open(connStr, function (err, conn) {
        if (err) {
            return console.error("Could not connect to sql: ", err);
        }

        conn.query(query, function (err, results) {

            if (err) {
                return console.error("Error running query: ", err);
            }
            console.log(results);
            console.log(results.length);
            for (var i = 0; i <= results.length; i++) {
                util.inspect(results[i]);
            }
        });
    });
};