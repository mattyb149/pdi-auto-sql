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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.trans.dialog.TransDialogPlugin;
import org.pentaho.di.ui.trans.dialog.TransDialogPluginInterface;

/**
 * Adds a tab to the Transformation Settings dialog to auto-execute SQL
 */
@TransDialogPlugin(
  id = "autoSqlSettings",
  description = "Provides a tab for setting Auto-Exec SQL statements"
)
public class AutoSqlSettingsDialogPlugin implements TransDialogPluginInterface {

  private CTabItem wAutoSqlTab;
  private Button wAutoExecSQL;

  @Override
  public void addTab( TransMeta transMeta, Shell shell, CTabFolder tabFolder ) {

    PropsUI props = PropsUI.getInstance();
    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    wAutoSqlTab = new CTabItem( tabFolder, SWT.NONE );
    wAutoSqlTab.setText( "Auto SQL" );

    Composite wAutoSqlComp = new Composite( tabFolder, SWT.NONE );
    props.setLook( wAutoSqlComp );

    FormLayout dataServiceLayout = new FormLayout();
    dataServiceLayout.marginWidth = Const.FORM_MARGIN;
    dataServiceLayout.marginHeight = Const.FORM_MARGIN;
    wAutoSqlComp.setLayout( dataServiceLayout );

    // Checkbox whether to auto-exec the SQL
    Label wlAutoExecSQL = new Label( wAutoSqlComp, SWT.LEFT );
    wlAutoExecSQL.setText( "Automatically execute needed SQL?" );
    props.setLook( wlAutoExecSQL );
    FormData fdlServiceName = new FormData();
    fdlServiceName.left = new FormAttachment( 0, 0 );
    fdlServiceName.right = new FormAttachment( middle, -margin );
    fdlServiceName.top = new FormAttachment( 0, 0 );
    wlAutoExecSQL.setLayoutData( fdlServiceName );
    wAutoExecSQL = new Button( wAutoSqlComp, SWT.CHECK );
    wAutoExecSQL.setToolTipText( "This setting will execute any SQL statements necessary before the "
      + "transformation will run successfully" );
    FormData fdAutoExecSQL = new FormData();
    fdAutoExecSQL.top = new FormAttachment( 0, margin );
    fdAutoExecSQL.left = new FormAttachment( middle, 0 );
    fdAutoExecSQL.right = new FormAttachment( 100, 0 );
    wAutoExecSQL.setLayoutData( fdAutoExecSQL );

    wAutoSqlComp.layout();
    wAutoSqlTab.setControl( wAutoSqlComp );
  }

  @Override
  public void getData( TransMeta transMeta ) throws KettleException {
    String autoExecSQL = transMeta.getAttribute( "autosql", "autosql" );
    wAutoExecSQL.setSelection( autoExecSQL != null && Boolean.valueOf( autoExecSQL ) );
  }

  @Override
  public void ok( TransMeta transMeta ) throws KettleException {
    String currentAutoExecSQL = transMeta.getAttribute( "autosql", "autosql" );
    String newAutoExecSQL = Boolean.toString( wAutoExecSQL.getSelection() );
    if ( !newAutoExecSQL.equals( currentAutoExecSQL ) ) {
      transMeta.setAttribute( "autosql", "autosql", newAutoExecSQL );
      transMeta.setChanged( true );
    }
  }

  @Override
  public CTabItem getTab() {
    return wAutoSqlTab;
  }
}
