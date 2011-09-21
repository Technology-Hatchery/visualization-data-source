package com.mapr.metrics;

import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.query.parser.ParseException;
import com.google.visualization.datasource.query.parser.QueryParser;
import com.google.visualization.datasource.util.SqlDataSourceHelper;
import com.google.visualization.datasource.util.SqlDatabaseDescription;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class DataServlet extends DataSourceServlet {
  private Logger log = Log.getLog();

  // we expect that this will be initialized by the framework
  private ConnectionPoolDataSource dataSource;

  private Query selectAllQuery;

  public DataServlet() throws InvalidQueryException, ParseException {
    selectAllQuery = QueryParser.parseString("select *");
  }

  // for dependency injection
  public void setDataSource(ConnectionPoolDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  protected boolean isRestrictedAccessMode() {
    // all cross site scripting for simplicity
    return false;
  }

  @Override
  public DataTable generateDataTable(Query query, HttpServletRequest request) throws DataSourceException {
    // the tail of our URL is the table name
    String table = request.getRequestURI().replaceAll(".*/", "");
    log.info("Table = " + table);
    if (table.length() > 0) {
      if (query == null) {
        query = selectAllQuery;
      }
      log.info("Query = " + query);
      Connection connection = null;
      try {
        PooledConnection pc = dataSource.getPooledConnection();
        connection = pc.getConnection();
        return SqlDataSourceHelper.executeQuery(query, table, connection);
      } catch (SQLException e) {
        throw new DataSourceException(ReasonType.INTERNAL_ERROR, e.getMessage());
      } finally {
        if (connection != null) {
          try {
            connection.close();
          } catch (SQLException e) {
            throw new DataSourceException(ReasonType.INTERNAL_ERROR, e.getMessage());
          }
        }
      }
    } else {
      log.warn("Bad table name");
      throw new IllegalArgumentException("Needed table name at the end of the URL");
    }
  }
}