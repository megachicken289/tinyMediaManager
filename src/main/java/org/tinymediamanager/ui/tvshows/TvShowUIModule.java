/*
 * Copyright 2012 - 2017 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.tvshows;

import java.awt.CardLayout;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.AbstractTmmUIModule;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.settings.TmmSettingsNode;
import org.tinymediamanager.ui.tvshows.actions.TvShowBulkEditAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowChangeSeasonPosterAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowClearImageCacheAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowDeleteAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowEditAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowExportAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowMediaInformationAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowReadEpisodeNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowReadNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRemoveAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRenameAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRewriteEpisodeNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRewriteNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowScrapeEpisodesAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowScrapeEpisodesWoArtworkAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowScrapeMissingEpisodesAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowScrapeNewItemsAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSelectedScrapeAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSetWatchedFlagAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSingleScrapeAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSyncTraktTvAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSyncWatchedTraktTvAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowUpdateAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowUpdateDatasourcesAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowUpdateSingleDatasourceAction;
import org.tinymediamanager.ui.tvshows.panels.TvShowExtendedSearchPanel;
import org.tinymediamanager.ui.tvshows.panels.TvShowTreePanel;
import org.tinymediamanager.ui.tvshows.panels.episode.TvShowEpisodeCastPanel;
import org.tinymediamanager.ui.tvshows.panels.episode.TvShowEpisodeInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.episode.TvShowEpisodeMediaInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.season.TvShowSeasonInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.season.TvShowSeasonMediaFilesPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowArtworkPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowCastPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowMediaInformationPanel;
import org.tinymediamanager.ui.tvshows.settings.TvShowDatasourceSettingsPanel;
import org.tinymediamanager.ui.tvshows.settings.TvShowImageSettingsPanel;
import org.tinymediamanager.ui.tvshows.settings.TvShowRenamerSettingsPanel;
import org.tinymediamanager.ui.tvshows.settings.TvShowScraperSettingsPanel;
import org.tinymediamanager.ui.tvshows.settings.TvShowSettingsPanel;
import org.tinymediamanager.ui.tvshows.settings.TvShowSubtitleSettingsPanel;

import net.miginfocom.swing.MigLayout;

public class TvShowUIModule extends AbstractTmmUIModule {
  private final static String             ID       = "tvShows";

  private static TvShowUIModule           instance = null;

  final TvShowSelectionModel              tvShowSelectionModel;
  final TvShowSeasonSelectionModel        tvShowSeasonSelectionModel;
  final TvShowEpisodeSelectionModel       tvShowEpisodeSelectionModel;

  private final TvShowTreePanel           listPanel;
  private final JPanel                    dataPanel;
  private final TvShowExtendedSearchPanel filterPanel;

  private TmmSettingsNode                 settingsNode;

  private TvShowUIModule() {

    tvShowSelectionModel = new TvShowSelectionModel();
    tvShowSeasonSelectionModel = new TvShowSeasonSelectionModel();
    tvShowEpisodeSelectionModel = new TvShowEpisodeSelectionModel();

    listPanel = new TvShowTreePanel(tvShowSelectionModel);
    super.listPanel = listPanel;

    detailPanel = new JPanel();
    detailPanel.setOpaque(false);
    detailPanel.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));

    // layeredpane for displaying the filter dialog at the top
    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));
    detailPanel.add(layeredPane, "cell 0 0, grow");

    dataPanel = new JPanel();
    dataPanel.setOpaque(false);
    dataPanel.setLayout(new CardLayout());

    // panel for TV shows
    JTabbedPane tvShowDetailPanel = new MainTabbedPane();
    tvShowDetailPanel.add(BUNDLE.getString("metatag.details"), new TvShowInformationPanel(tvShowSelectionModel));//$NON-NLS-1$
    tvShowDetailPanel.add(BUNDLE.getString("metatag.cast"), new TvShowCastPanel(tvShowSelectionModel));//$NON-NLS-1$
    tvShowDetailPanel.add(BUNDLE.getString("metatag.mediafiles"), new TvShowMediaInformationPanel(tvShowSelectionModel));//$NON-NLS-1$
    tvShowDetailPanel.add(BUNDLE.getString("metatag.artwork"), new TvShowArtworkPanel(tvShowSelectionModel)); //$NON-NLS-1$
    dataPanel.add(tvShowDetailPanel, "tvShow");

    // panel for seasons
    JTabbedPane tvShowSeasonDetailPanel = new MainTabbedPane();
    tvShowSeasonDetailPanel.add(BUNDLE.getString("metatag.details"), new TvShowSeasonInformationPanel(tvShowSeasonSelectionModel));//$NON-NLS-1$
    tvShowSeasonDetailPanel.add(BUNDLE.getString("metatag.mediafiles"), new TvShowSeasonMediaFilesPanel(tvShowSeasonSelectionModel)); //$NON-NLS-1$
    dataPanel.add(tvShowSeasonDetailPanel, "tvShowSeason");

    // panel for episodes
    JTabbedPane tvShowEpisodeDetailPanel = new MainTabbedPane();
    tvShowEpisodeDetailPanel.add(BUNDLE.getString("metatag.details"), new TvShowEpisodeInformationPanel(tvShowEpisodeSelectionModel));//$NON-NLS-1$
    tvShowEpisodeDetailPanel.add(BUNDLE.getString("metatag.cast"), new TvShowEpisodeCastPanel(tvShowEpisodeSelectionModel)); //$NON-NLS-1$
    tvShowEpisodeDetailPanel.add(BUNDLE.getString("metatag.mediafiles"), new TvShowEpisodeMediaInformationPanel(tvShowEpisodeSelectionModel));//$NON-NLS-1$
    dataPanel.add(tvShowEpisodeDetailPanel, "tvShowEpisode");

    layeredPane.add(dataPanel, "cell 0 0, grow");
    layeredPane.setLayer(dataPanel, 0);

    // glass pane for searching/filtering
    filterPanel = new TvShowExtendedSearchPanel(listPanel.getTreeTable());
    filterPanel.setVisible(false);
    layeredPane.add(filterPanel, "pos 0 0");
    layeredPane.setLayer(filterPanel, 1);

    // create actions and menus
    createActions();
    createPopupMenu();
    registerAccelerators();

    // build settings node
    settingsNode = new TmmSettingsNode(BUNDLE.getString("Settings.tvshow"), new TvShowSettingsPanel());//$NON-NLS-1$
    settingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.source"), new TvShowDatasourceSettingsPanel()));//$NON-NLS-1$
    settingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.scraper"), new TvShowScraperSettingsPanel()));//$NON-NLS-1$
    settingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.images"), new TvShowImageSettingsPanel()));//$NON-NLS-1$
    settingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.subtitle"), new TvShowSubtitleSettingsPanel()));//$NON-NLS-1$
    settingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.renamer"), new TvShowRenamerSettingsPanel()));//$NON-NLS-1$

    // further initializations
    init();
  }

  private void init() {
    // re-set filters
    if (TvShowModuleManager.SETTINGS.isStoreUiFilters()) {
      SwingUtilities.invokeLater(() -> {
        listPanel.getTreeTable().setFilterValues(TvShowModuleManager.SETTINGS.getUiFilters());
      });
    }
  }

  public static TvShowUIModule getInstance() {
    if (instance == null) {
      instance = new TvShowUIModule();
    }
    return instance;
  }

  public void setFilterMenuVisible(boolean visible) {
    filterPanel.setVisible(visible);
  }

  @Override
  public String getModuleId() {
    return ID;
  }

  @Override
  public JPanel getTabPanel() {
    return listPanel;
  }

  @Override
  public String getTabTitle() {
    return BUNDLE.getString("tmm.tvshows"); //$NON-NLS-1$
  }

  public TvShowSelectionModel getSelectionModel() {
    return tvShowSelectionModel;
  }

  @Override
  public TmmSettingsNode getSettingsNode() {
    return settingsNode;
  }

  private void createActions() {
    searchAction = createAndRegisterAction(TvShowSingleScrapeAction.class);
    editAction = createAndRegisterAction(TvShowEditAction.class);
    updateAction = createAndRegisterAction(TvShowUpdateDatasourcesAction.class);
    exportAction = createAndRegisterAction(TvShowExportAction.class);
    renameAction = createAndRegisterAction(TvShowRenameAction.class);
  }

  private void createPopupMenu() {
    // popup menu
    popupMenu = new JPopupMenu();
    popupMenu.add(createAndRegisterAction(TvShowSingleScrapeAction.class));
    popupMenu.add(createAndRegisterAction(TvShowSelectedScrapeAction.class));
    popupMenu.add(createAndRegisterAction(TvShowScrapeEpisodesAction.class));
    popupMenu.add(createAndRegisterAction(TvShowScrapeEpisodesWoArtworkAction.class));
    popupMenu.add(createAndRegisterAction(TvShowScrapeNewItemsAction.class));
    popupMenu.add(createAndRegisterAction(TvShowScrapeMissingEpisodesAction.class));

    popupMenu.addSeparator();

    popupMenu.add(createAndRegisterAction(TvShowUpdateAction.class));

    popupMenu.addSeparator();

    popupMenu.add(createAndRegisterAction(TvShowEditAction.class));
    popupMenu.add(createAndRegisterAction(TvShowChangeSeasonPosterAction.class));
    popupMenu.add(createAndRegisterAction(TvShowBulkEditAction.class));
    popupMenu.add(createAndRegisterAction(TvShowSetWatchedFlagAction.class));
    popupMenu.add(createAndRegisterAction(TvShowRewriteNfoAction.class));
    popupMenu.add(createAndRegisterAction(TvShowReadNfoAction.class));
    popupMenu.add(createAndRegisterAction(TvShowRewriteEpisodeNfoAction.class));
    popupMenu.add(createAndRegisterAction(TvShowReadEpisodeNfoAction.class));
    popupMenu.add(createAndRegisterAction(TvShowRenameAction.class));
    popupMenu.add(createAndRegisterAction(TvShowMediaInformationAction.class));
    popupMenu.add(createAndRegisterAction(TvShowExportAction.class));
    popupMenu.add(createAndRegisterAction(TvShowClearImageCacheAction.class));

    popupMenu.addSeparator();

    popupMenu.add(createAndRegisterAction(TvShowSyncTraktTvAction.class));
    popupMenu.add(createAndRegisterAction(TvShowSyncWatchedTraktTvAction.class));

    popupMenu.addSeparator();

    popupMenu.add(createAndRegisterAction(TvShowRemoveAction.class));
    popupMenu.add(createAndRegisterAction(TvShowDeleteAction.class));

    listPanel.setPopupMenu(popupMenu);

    // update popup menu
    updatePopupMenu = new JPopupMenu();
    updatePopupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        updatePopupMenu.removeAll();
        updatePopupMenu.add(createAndRegisterAction(TvShowUpdateDatasourcesAction.class));
        updatePopupMenu.addSeparator();
        for (String ds : TvShowModuleManager.SETTINGS.getTvShowDataSource()) {
          updatePopupMenu.add(new TvShowUpdateSingleDatasourceAction(ds));
        }
        updatePopupMenu.addSeparator();
        updatePopupMenu.add(new TvShowUpdateAction());
        updatePopupMenu.pack();
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
      }
    });

    // scrape popup menu
    searchPopupMenu = new JPopupMenu();
    searchPopupMenu.add(createAndRegisterAction(TvShowSingleScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(TvShowSelectedScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(TvShowScrapeEpisodesAction.class));
    searchPopupMenu.add(createAndRegisterAction(TvShowScrapeEpisodesWoArtworkAction.class));
    searchPopupMenu.add(createAndRegisterAction(TvShowScrapeNewItemsAction.class));
    searchPopupMenu.add(createAndRegisterAction(TvShowScrapeMissingEpisodesAction.class));

    // edit popupmenu
    editPopupMenu = new JPopupMenu();
    editPopupMenu.add(createAndRegisterAction(TvShowEditAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowChangeSeasonPosterAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowBulkEditAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowSetWatchedFlagAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowRewriteNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowReadNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowRewriteEpisodeNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowReadEpisodeNfoAction.class));
  }

  /**
   * set the selected TV shows. This causes the right sided panel to switch to the TV show information panel
   * 
   * @param tvShow
   *          the selected TV show
   */
  public void setSelectedTvShow(TvShow tvShow) {
    tvShowSelectionModel.setSelectedTvShow(tvShow);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "tvShow");
  }

  /**
   * set the selected TV show season. This causes the right sided panel to switch to the season information panel
   * 
   * @param tvShowSeason
   *          the selected season
   */
  public void setSelectedTvShowSeason(TvShowSeason tvShowSeason) {
    tvShowSeasonSelectionModel.setSelectedTvShowSeason(tvShowSeason);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "tvShowSeason");
  }

  /**
   * set the selected TV show episode. This cases the right sided panel to switch to the episode information panel
   * 
   * @param tvShowEpisode
   *          the selected episode
   */
  public void setSelectedTvShowEpisode(TvShowEpisode tvShowEpisode) {
    tvShowEpisodeSelectionModel.setSelectedTvShowEpisode(tvShowEpisode);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "tvShowEpisode");
  }
}
