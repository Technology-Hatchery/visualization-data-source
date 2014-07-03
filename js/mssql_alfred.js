/**
 * Created by Alfred on 6/30/2014.
 */
function databaseData() {

document.write("testing3");
document.write(location.pathname);
//var test = require('./js/webserver');
document.write("testing4");
//var sql = require('X:/Dropbox/Technology Hatchery Inc/technical/Git/visualization-data-source/js/node_modules/mssql');
    var sql = require('http');
    var sql = require('./js/node_modules/mssql/index.js');
    var sql = require('../js/node_modules/mssql');
    var sql = require('mssql');
    var sql = require('./node_modules/mssql');
    var sql = require('./js/node_modules/mssql');
document.write("testing5");


var config = {
    user: 'alfred.wechselberger@technologyhatchery.com.',
    password: 'Cc17931793',
    server: 'http://54.85.42.208:1433', // You can use 'localhost\\instance' to connect to named instance
    database: 'master',

    options: {
        encrypt: true // Use this if you're on Windows Azure
    }
}

var connection = new sql.Connection(config, function(err) {
        // ... error checks

        // Query
        var request = new sql.Request(connection); // or: var request = connection.request();
        var command = 'SELECT TOP 1000 [name], [location], [qty] FROM [alfred].[dbo].[coffee]';
        request.query(command, function(err, recordset) {
            // ... error checks

            //draw the chart based on the results from the SQL query
            drawChart(recordset);
            //console.dir(recordset);
        });

        // Stored Procedure

        //var request = new sql.Request(connection);
        //request.input('input_parameter', sql.Int, 10);
        //request.output('output_parameter', sql.VarChar(50));
        //request.execute('procedure_name', function(err, recordsets, returnValue) {
            // ... error checks

            //console.dir(recordsets);
        });


    function drawChart(recordset) {
        // Create our data table out of JSON data loaded from server.
        var data = new google.visualization.DataTable(recordset);

        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.PieChart(document.getElementById('chart_div'));
        chart.draw(data, {width: 400, height: 240});
    };
};