/**
 * Sets the data source field from the JdbcUrl field before later will be deleted
*/

import java.sql.SQLException

sql.eachRow('SELECT id, JDBCurl FROM SC_ConnecteurJDBC_ConnectInfo') { row ->
  String jdbcUrl = row.JDBCUrl
  int idx = jdbcUrl.lastIndexOf('/')
  String dataSource = "java:/datasources/${jdbcUrl.substring(idx + 1)}"
  int count = sql.executeUpdate('UPDATE SC_ConnecteurJDBC_ConnectInfo SET dataSource = :dataSource WHERE id = :id',
      [id: row.id, dataSource: dataSource])
  if (count != 1) {
    throw new SQLException("Failed to update connection info of id = ${row.id} with data source = ${dataSource}")
  } else {
    log.info "Connection Info of id = ${row.id} -> set data source = ${dataSource}: [Ok]"
  }
}


