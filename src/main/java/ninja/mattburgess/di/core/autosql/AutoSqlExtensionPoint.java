/*******************************************************************************
 *
 * Copyright (C) 2015 by Matt Burgess
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package ninja.mattburgess.di.core.autosql;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.util.List;

/**
 * An extension point to execute any needed SQL statements before the transformation itself executes
 */
@ExtensionPoint(
  description = "Auto-exec needed SQL statements",
  extensionPointId = "TransformationPrepareExecution",
  id = "autoSqlExtensionPoint" )
public class AutoSqlExtensionPoint implements ExtensionPointInterface {

  /**
   * This method is called by the Kettle code before a transformation initalizes
   *
   * @param log    the logging channel to log debugging information to
   * @param object The subject object that is passed to the plugin code
   * @throws KettleException In case the plugin decides that an error has occurred and the parent process should stop.
   */
  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    // Transformation Started listeners get called after the extension point is invoked, so just add a trans listener
    if ( object instanceof Trans ) {
      Trans trans = ( (Trans) object );
      TransMeta transMeta = trans.getTransMeta();

      // Check to see if this trans wants to execute SQL automatically
      String autoExecSQL = transMeta.getAttribute( "autosql", "autosql" );
      if ( autoExecSQL != null && Boolean.valueOf( autoExecSQL ) ) {

        List<SQLStatement> statements =
          transMeta.getSQLStatements( new ProgressMonitorAdapter( new NullProgressMonitor() ) );

        if ( statements != null ) {
          for ( SQLStatement statement : statements ) {
            String sql = statement.getSQL();
            DatabaseMeta databaseMeta = statement.getDatabase();
            if ( databaseMeta != null && !statement.hasError() ) {
              try {
                Database db = new Database( trans, databaseMeta );
                db.connect();
                db.execStatements( sql );
                db.disconnect();
              } catch ( KettleDatabaseException kde ) {
                // Some DBs/metas don't return indexes correctly, so PDI will assume it needs to re-create
                // (possibly unique) indexes. If the statement starts with CREATE UNIQUE INDEX and we get an
                // "Index already exists" message, then ignore and move on. Otherwise throw the exception
                Throwable cause = kde.getCause();
                String causeMessage = ( cause == null ) ? "" : cause.getMessage();
                if ( !( sql.startsWith( "CREATE UNIQUE INDEX" ) || sql.startsWith( "CREATE INDEX" ) )
                  || !causeMessage.startsWith( "Index" )
                  || !causeMessage.contains( "already exists" ) ) {

                  throw kde;
                }
              }
            } else {
              throw new KettleDatabaseException( "Autoexec SQL found errors with statement: " + sql );
            }
          }
        }
      }
    }
  }
}

