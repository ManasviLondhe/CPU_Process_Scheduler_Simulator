package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import controller.ApplicationController;
import controller.PageId;
import ui.components.NavigationPanel;
import ui.pages.AboutPanel;
import ui.pages.AlgorithmConfigurationPanel;
import ui.pages.BasePage;
import ui.pages.ComparisonPanel;
import ui.pages.DashboardPanel;
import ui.pages.HistoryPanel;
import ui.pages.ProcessManagementPanel;
import ui.pages.ResultsPanel;
import ui.pages.SimulationPanel;
import util.Constants;
import util.UIUtils;

/** Main window: sidebar navigation + card layout holding one panel per page. */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(cardLayout);
    private final Map<PageId, BasePage> pages = new EnumMap<>(PageId.class);
    private final NavigationPanel navigation;

    public MainFrame(ApplicationController app) {
        super(Constants.APP_NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1320, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.WHITE);

        pages.put(PageId.DASHBOARD, new DashboardPanel(app));
        pages.put(PageId.PROCESSES, new ProcessManagementPanel(app));
        pages.put(PageId.ALGORITHMS, new AlgorithmConfigurationPanel(app));
        pages.put(PageId.SIMULATION, new SimulationPanel(app));
        pages.put(PageId.RESULTS, new ResultsPanel(app));
        pages.put(PageId.COMPARE, new ComparisonPanel(app));
        pages.put(PageId.HISTORY, new HistoryPanel(app));
        pages.put(PageId.ABOUT, new AboutPanel(app));
        for (Map.Entry<PageId, BasePage> e : pages.entrySet()) {
            pageHost.add(e.getValue(), e.getKey().name());
        }

        navigation = new NavigationPanel(this::showPage);
        app.setNavigator(this::showPage);
        add(navigation, BorderLayout.WEST);
        add(pageHost, BorderLayout.CENTER);
        showPage(PageId.DASHBOARD);
    }

    private void showPage(PageId id) {
        pages.get(id).onShow();
        cardLayout.show(pageHost, id.name());
        navigation.select(id);
    }
}
