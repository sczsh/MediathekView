package mediathek.gui.dialogEinstellungen.allgemein;

import mediathek.config.Daten;
import mediathek.config.Icons;
import mediathek.config.MVConfig;
import mediathek.gui.dialog.DialogHilfe;
import mediathek.gui.messages.*;
import mediathek.mainwindow.MediathekGui;
import mediathek.tool.ApplicationConfiguration;
import mediathek.tool.MVSenderIconCache;
import net.engio.mbassy.listener.Handler;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.NoSuchElementException;

@SuppressWarnings("serial")
public class PanelEinstellungen extends JPanel {
    private final static String ALLE = " Alle ";
    private final Configuration config = ApplicationConfiguration.getConfiguration();
    private final JFrame parent;
    private final Daten daten;

    private void setupProxySettings() {

        jtfProxyHost.setText(config.getString(ApplicationConfiguration.HTTP_PROXY_HOSTNAME, ""));
        var listener = new TextFieldConfigWriter(jtfProxyHost,ApplicationConfiguration.HTTP_PROXY_HOSTNAME);
        jtfProxyHost.getDocument().addDocumentListener(new TimedDocumentListener(listener));

        jtfProxyPort.setText(config.getString(ApplicationConfiguration.HTTP_PROXY_PORT, ""));
        listener = new TextFieldConfigWriter(jtfProxyPort,ApplicationConfiguration.HTTP_PROXY_PORT);
        jtfProxyPort.getDocument().addDocumentListener(new TimedDocumentListener(listener));

        jtfProxyUser.setText(config.getString(ApplicationConfiguration.HTTP_PROXY_USERNAME, ""));
        listener = new TextFieldConfigWriter(jtfProxyUser,ApplicationConfiguration.HTTP_PROXY_USERNAME);
        jtfProxyUser.getDocument().addDocumentListener(new TimedDocumentListener(listener));

        jpfProxyPassword.setText(config.getString(ApplicationConfiguration.HTTP_PROXY_PASSWORD, ""));
        listener = new TextFieldConfigWriter(jpfProxyPassword,ApplicationConfiguration.HTTP_PROXY_PASSWORD);
        jpfProxyPassword.getDocument().addDocumentListener(new TimedDocumentListener(listener));
    }

    private void setupUserAgentSettings() {
        jtfUserAgent.setText(ApplicationConfiguration.getConfiguration().getString(ApplicationConfiguration.APPLICATION_USER_AGENT));
        var listener = new TextFieldConfigWriter(jtfUserAgent,ApplicationConfiguration.APPLICATION_USER_AGENT);
        jtfUserAgent.getDocument().addDocumentListener(new TimedDocumentListener(listener));
    }

    private void cbUseWikipediaSenderLogosActionPerformed(java.awt.event.ActionEvent evt) {
        ApplicationConfiguration.getConfiguration().setProperty(MVSenderIconCache.CONFIG_USE_LOCAL_SENDER_ICONS,!cbUseWikipediaSenderLogos.isSelected());
        daten.getMessageBus().publishAsync(new SenderIconStyleChangedEvent());
    }

    private void setupDays() {
        jButtonHelpDays.setIcon(Icons.ICON_BUTTON_HELP);
        jButtonHelpDays.addActionListener(e -> new DialogHilfe(parent, true, '\n'
                + "Es werden nur Filme der letzten\n"
                + "xx Tage geladen."
                + '\n'
                + "Bei \"Alle\" werden alle Filme geladen.\n"
                + '\n'
                + "(Eine kleinere Filmliste\n"
                + "kann bei Rechnern mit wenig\n"
                + "Speicher hilfreich sein.)"
                + "\n\n"
                + "Auswirkung hat das erst nach dem\n"
                + "Neuladen der kompletten Filmliste.").setVisible(true));

        SpinnerListModel lm = new SpinnerListModel(new Object[]{ALLE, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "12", "14", "16", "18", "20", "25", "30"});
        jSpinnerDays.setModel(lm);
        ((JSpinner.DefaultEditor) jSpinnerDays.getEditor()).getTextField().setEditable(false);
        initSpinner();
        jSpinnerDays.addChangeListener(new BeobSpinnerDays());
    }

    private void setupTabUI() {
        final boolean tabPositionTop = config.getBoolean(ApplicationConfiguration.APPLICATION_UI_TAB_POSITION_TOP, true);
        jCheckBoxTabsTop.setSelected(tabPositionTop);
        jCheckBoxTabsTop.addActionListener(ae -> {
            config.setProperty(ApplicationConfiguration.APPLICATION_UI_TAB_POSITION_TOP, jCheckBoxTabsTop.isSelected());
            Daten.getInstance().getMessageBus().publishAsync(new TabVisualSettingsChangedEvent());
        });

        var config = ApplicationConfiguration.getConfiguration();
        jCheckBoxTabIcon.setSelected(config.getBoolean(ApplicationConfiguration.APPLICATION_UI_MAINWINDOW_TAB_ICONS,false));
        jCheckBoxTabIcon.addActionListener(ae -> {
            config.setProperty(ApplicationConfiguration.APPLICATION_UI_MAINWINDOW_TAB_ICONS, jCheckBoxTabIcon.isSelected());
            Daten.getInstance().getMessageBus().publishAsync(new TabVisualSettingsChangedEvent());
        });
    }

    @Handler
    private void handleTrayIconEvent(TrayIconEvent e) {
        SwingUtilities.invokeLater(() -> jCheckBoxTray.setSelected(Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_USE_TRAY))));
    }

    private void setupTray() {
        if (SystemUtils.IS_OS_MAC_OSX) {
            jCheckBoxTray.setSelected(false);
            jCheckBoxTray.setEnabled(false);
        } else {
            daten.getMessageBus().subscribe(this);

            jCheckBoxTray.setSelected(Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_USE_TRAY)));
            jCheckBoxTray.addActionListener(ae -> {
                MVConfig.add(MVConfig.Configs.SYSTEM_USE_TRAY, Boolean.toString(jCheckBoxTray.isSelected()));
                MediathekGui.ui().initializeSystemTray();
            });
        }
    }

    private void setupDatabaseCleanerCheckbox() {
        final Configuration config = ApplicationConfiguration.getConfiguration();
        cbUseDatabaseCleaner.setSelected(config.getBoolean(ApplicationConfiguration.DATABASE_USE_CLEANER_INTERFACE, false));
        cbUseDatabaseCleaner.addActionListener(l -> config.setProperty(ApplicationConfiguration.DATABASE_USE_CLEANER_INTERFACE, cbUseDatabaseCleaner.isSelected()));
    }

    private void setupSaveHumanReadableFilmlistCheckbox() {
        final Configuration config = ApplicationConfiguration.getConfiguration();
        cbSaveHumanReadableFilmlist.setSelected(config.getBoolean(ApplicationConfiguration.FILMLISTE_SAVE_HUMAN_READABLE, false));
        cbSaveHumanReadableFilmlist.addActionListener(l -> config.setProperty(ApplicationConfiguration.FILMLISTE_SAVE_HUMAN_READABLE, cbSaveHumanReadableFilmlist.isSelected()));
    }

    public PanelEinstellungen(Daten d, JFrame parent) {
        super();
        this.parent = parent;
        daten = d;

        initComponents();

        setupUserAgentSettings();

        setupProxySettings();

        setupDatabaseCleanerCheckbox();

        setupSaveHumanReadableFilmlistCheckbox();

        jButtonLoad.addActionListener(ae -> {
            daten.getListeFilme().clear(); // sonst wird evtl. nur eine Diff geladen
            daten.getFilmeLaden().loadFilmlist("");
        });

        setupDays();

        setupTabUI();

        setupTray();

        setupTabSwitchListener();

        cbUseWikipediaSenderLogos.addActionListener(this::cbUseWikipediaSenderLogosActionPerformed);
        final boolean useLocalSenderLogos = ApplicationConfiguration.getConfiguration().getBoolean(MVSenderIconCache.CONFIG_USE_LOCAL_SENDER_ICONS,false);
        cbUseWikipediaSenderLogos.setSelected(!useLocalSenderLogos);
    }

    @Handler
    private void handleParallelDownloadNumberChanged(ParallelDownloadNumberChangedEvent e) {
        SwingUtilities.invokeLater(this::initSpinner);
    }

    private void setupTabSwitchListener() {
        if (SystemUtils.IS_OS_MAC_OSX) {
            //deactivated on OS X
            cbAutomaticMenuTabSwitching.setEnabled(false);
            config.setProperty(ApplicationConfiguration.APPLICATION_INSTALL_TAB_SWITCH_LISTENER, false);
        } else {
            boolean installed;
            try {
                installed = config.getBoolean(ApplicationConfiguration.APPLICATION_INSTALL_TAB_SWITCH_LISTENER);
            } catch (NoSuchElementException ex) {
                installed = true;
                config.setProperty(ApplicationConfiguration.APPLICATION_INSTALL_TAB_SWITCH_LISTENER, true);
            }
            cbAutomaticMenuTabSwitching.setSelected(installed);

            cbAutomaticMenuTabSwitching.addActionListener(e -> {
                final boolean isOn = cbAutomaticMenuTabSwitching.isSelected();
                config.setProperty(ApplicationConfiguration.APPLICATION_INSTALL_TAB_SWITCH_LISTENER, isOn);
                final InstallTabSwitchListenerEvent evt = new InstallTabSwitchListenerEvent();
                if (isOn) {
                    evt.event = InstallTabSwitchListenerEvent.INSTALL_TYPE.INSTALL;
                } else {
                    evt.event = InstallTabSwitchListenerEvent.INSTALL_TYPE.REMOVE;
                }
                daten.getMessageBus().publishAsync(evt);
            });
        }
    }

    private void initSpinner() {
        if (MVConfig.get(MVConfig.Configs.SYSTEM_ANZ_TAGE_FILMLISTE).isEmpty()) {
            MVConfig.add(MVConfig.Configs.SYSTEM_ANZ_TAGE_FILMLISTE, "0");
        }
        String s = MVConfig.get(MVConfig.Configs.SYSTEM_ANZ_TAGE_FILMLISTE);
        if (s.equals("0")) {
            s = ALLE;
        }
        jSpinnerDays.setValue(s);
    }

    private class BeobSpinnerDays implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent arg0) {
            String s = jSpinnerDays.getModel().getValue().toString();
            if (s.equals(ALLE)) {
                s = "0";
            }
            MVConfig.add(MVConfig.Configs.SYSTEM_ANZ_TAGE_FILMLISTE, s);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner non-commercial license
    private void initComponents() {
        var jPanel5 = new JPanel();
        jCheckBoxTabsTop = new JCheckBox();
        jCheckBoxTabIcon = new JCheckBox();
        cbAutomaticMenuTabSwitching = new JCheckBox();
        var jPanel3 = new JPanel();
        var jLabel3 = new JLabel();
        jtfUserAgent = new JTextField();
        var jPanel4 = new JPanel();
        var jLabel4 = new JLabel();
        jtfProxyHost = new JTextField();
        var jLabel5 = new JLabel();
        jtfProxyPort = new JTextField();
        var jLabel7 = new JLabel();
        jtfProxyUser = new JTextField();
        var jLabel8 = new JLabel();
        jpfProxyPassword = new JPasswordField();
        var jPanel2 = new JPanel();
        var jPanel6 = new JPanel();
        var jLabel6 = new JLabel();
        jSpinnerDays = new JSpinner();
        jButtonLoad = new JButton();
        jButtonHelpDays = new JButton();
        var jPanel7 = new JPanel();
        cbUseDatabaseCleaner = new JCheckBox();
        var jPanel8 = new JPanel();
        cbSaveHumanReadableFilmlist = new JCheckBox();
        jCheckBoxTray = new JCheckBox();
        cbUseWikipediaSenderLogos = new JCheckBox();

        //======== this ========
        setMaximumSize(new Dimension(10, 10));

        //======== jPanel5 ========
        {
            jPanel5.setBorder(new TitledBorder("Tab-Verhalten")); //NON-NLS

            //---- jCheckBoxTabsTop ----
            jCheckBoxTabsTop.setText("Tabs oben anzeigen"); //NON-NLS

            //---- jCheckBoxTabIcon ----
            jCheckBoxTabIcon.setText("Icons anzeigen"); //NON-NLS
            jCheckBoxTabIcon.setToolTipText("Im Tab keine Icons anzeigen"); //NON-NLS

            //---- cbAutomaticMenuTabSwitching ----
            cbAutomaticMenuTabSwitching.setText("Tabs schalten automatisch bei Men\u00fcnutzung um"); //NON-NLS

            GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
            jPanel5.setLayout(jPanel5Layout);
            jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup()
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jCheckBoxTabsTop)
                        .addGap(5, 5, 5)
                        .addComponent(jCheckBoxTabIcon)
                        .addGap(5, 5, 5)
                        .addComponent(cbAutomaticMenuTabSwitching)
                        .addContainerGap(20, Short.MAX_VALUE))
            );
            jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup()
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(jPanel5Layout.createParallelGroup()
                            .addComponent(jCheckBoxTabsTop)
                            .addComponent(jCheckBoxTabIcon)
                            .addComponent(cbAutomaticMenuTabSwitching)))
            );
        }

        //======== jPanel3 ========
        {
            jPanel3.setBorder(new TitledBorder("Download")); //NON-NLS

            //---- jLabel3 ----
            jLabel3.setText("User-Agent:"); //NON-NLS

            //---- jtfUserAgent ----
            jtfUserAgent.setMinimumSize(new Dimension(200, 26));
            jtfUserAgent.setPreferredSize(new Dimension(520, 26));

            GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
            jPanel3.setLayout(jPanel3Layout);
            jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup()
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel3)
                        .addGap(5, 5, 5)
                        .addComponent(jtfUserAgent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(15, Short.MAX_VALUE))
            );
            jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup()
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel3))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jtfUserAgent, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            );
        }

        //======== jPanel4 ========
        {
            jPanel4.setBorder(new TitledBorder("HTTP-Proxy (Neustart erforderlich!)")); //NON-NLS
            jPanel4.setToolTipText(""); //NON-NLS

            //---- jLabel4 ----
            jLabel4.setText("Host:"); //NON-NLS

            //---- jLabel5 ----
            jLabel5.setText("Port:"); //NON-NLS

            //---- jLabel7 ----
            jLabel7.setText("User:"); //NON-NLS

            //---- jLabel8 ----
            jLabel8.setText("Passwort:"); //NON-NLS

            GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
            jPanel4.setLayout(jPanel4Layout);
            jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup()
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtfProxyHost, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtfProxyUser)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel5))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup()
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jtfProxyPort, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jpfProxyPassword))
                        .addContainerGap())
            );
            jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup()
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jtfProxyHost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(jtfProxyPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jtfProxyUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(jpfProxyPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        //======== jPanel2 ========
        {
            jPanel2.setBorder(new TitledBorder("")); //NON-NLS

            //======== jPanel6 ========
            {

                //---- jLabel6 ----
                jLabel6.setText("Nur die Filme der letzten Tage laden:"); //NON-NLS

                //---- jSpinnerDays ----
                jSpinnerDays.setModel(new SpinnerListModel(new String[] {"Alles", "1", "2", "10", "15"})); //NON-NLS

                //---- jButtonLoad ----
                jButtonLoad.setText("Filmliste jetzt neu laden"); //NON-NLS

                //---- jButtonHelpDays ----
                jButtonHelpDays.setIcon(new ImageIcon(getClass().getResource("/mediathek/res/muster/button-help.png"))); //NON-NLS
                jButtonHelpDays.setToolTipText("Hilfe anzeigen"); //NON-NLS

                GroupLayout jPanel6Layout = new GroupLayout(jPanel6);
                jPanel6.setLayout(jPanel6Layout);
                jPanel6Layout.setHorizontalGroup(
                    jPanel6Layout.createParallelGroup()
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addGap(5, 5, 5)
                            .addComponent(jLabel6)
                            .addGap(5, 5, 5)
                            .addComponent(jSpinnerDays, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonLoad)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButtonHelpDays)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel6Layout.setVerticalGroup(
                    jPanel6Layout.createParallelGroup()
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addGroup(jPanel6Layout.createParallelGroup()
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGap(11, 11, 11)
                                    .addComponent(jLabel6))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGap(6, 6, 6)
                                    .addGroup(jPanel6Layout.createParallelGroup()
                                        .addComponent(jButtonHelpDays)
                                        .addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(jSpinnerDays, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jButtonLoad)))))
                            .addGap(2, 2, 2))
                );
            }

            GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
            jPanel2.setLayout(jPanel2Layout);
            jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup()
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jPanel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            );
        }

        //======== jPanel7 ========
        {
            jPanel7.setBorder(new TitledBorder("Datenbank (Neustart erforderlich!)")); //NON-NLS

            //---- cbUseDatabaseCleaner ----
            cbUseDatabaseCleaner.setText("Bereinigung w\u00e4hrend Laufzeit"); //NON-NLS
            cbUseDatabaseCleaner.setToolTipText("<html>Wenn aktiviert werden ung\u00fcltige Datenbankeintr\u00e4ge sofort aus der Datenbank gel\u00f6scht um Speicher zu sparen.<br/>Dies wird f\u00fcr Rechner mit wenig Arbeitsspeicher empfohlen, verringert jedoch die Performance von MediathekView deutlich beim Laden einer neuen Filmliste.</html>"); //NON-NLS

            GroupLayout jPanel7Layout = new GroupLayout(jPanel7);
            jPanel7.setLayout(jPanel7Layout);
            jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup()
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cbUseDatabaseCleaner)
                        .addContainerGap(408, Short.MAX_VALUE))
            );
            jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup()
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cbUseDatabaseCleaner)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        //======== jPanel8 ========
        {
            jPanel8.setBorder(new TitledBorder("Speicherung der Filmliste")); //NON-NLS

            //---- cbSaveHumanReadableFilmlist ----
            cbSaveHumanReadableFilmlist.setText("in les- und editierbarem Format speichern"); //NON-NLS

            GroupLayout jPanel8Layout = new GroupLayout(jPanel8);
            jPanel8.setLayout(jPanel8Layout);
            jPanel8Layout.setHorizontalGroup(
                jPanel8Layout.createParallelGroup()
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cbSaveHumanReadableFilmlist)
                        .addContainerGap(335, Short.MAX_VALUE))
            );
            jPanel8Layout.setVerticalGroup(
                jPanel8Layout.createParallelGroup()
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cbSaveHumanReadableFilmlist)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        //---- jCheckBoxTray ----
        jCheckBoxTray.setText("Programm ins Tray minimieren"); //NON-NLS

        //---- cbUseWikipediaSenderLogos ----
        cbUseWikipediaSenderLogos.setText("Senderlogos von Wikipedia verwenden"); //NON-NLS

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(jPanel7, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel8, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jPanel2, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel4, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(jCheckBoxTray)
                                .addComponent(cbUseWikipediaSenderLogos))
                            .addGap(0, 1, Short.MAX_VALUE))
                        .addComponent(jPanel5, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBoxTray)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cbUseWikipediaSenderLogos)
                    .addContainerGap(7, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JCheckBox jCheckBoxTabsTop;
    private JCheckBox jCheckBoxTabIcon;
    private JCheckBox cbAutomaticMenuTabSwitching;
    private JTextField jtfUserAgent;
    private JTextField jtfProxyHost;
    private JTextField jtfProxyPort;
    private JTextField jtfProxyUser;
    private JPasswordField jpfProxyPassword;
    private JSpinner jSpinnerDays;
    private JButton jButtonLoad;
    private JButton jButtonHelpDays;
    private JCheckBox cbUseDatabaseCleaner;
    private JCheckBox cbSaveHumanReadableFilmlist;
    private JCheckBox jCheckBoxTray;
    private JCheckBox cbUseWikipediaSenderLogos;
    // End of variables declaration//GEN-END:variables
}
