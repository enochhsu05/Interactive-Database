import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Vector;

public class UI extends JFrame{
    private DatabaseConnectionHandler databaseController = null;
    private JPanel animePanel;
    private JTable animeTable;
    private JButton addAnimeBtn, editAnimeBtn, deleteAnimeBtn, fetchAnimeBtn, filterAnimeBtn, joinAnimeBtn, grpAnimeBtn, nestedgrpAnimeBtn, aggrHAnimeBtn, divAnimeBtn;
    private JCheckBox nameCheckBox,authorCheckBox,genreCheckBox,studioCheckBox,statusCheckBox,yearAiredCheckBox;


    public UI() {
        databaseController = new DatabaseConnectionHandler();
        this.setTitle("Anime Database Management");
        this.setSize(1920, 1080);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupAnimePanel();
        this.add(animePanel, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            {
                    databaseController.close();
                    System.exit(0);
            }
        });
    }

    private void setupAnimePanel() {
        animePanel = new JPanel(new BorderLayout());
        JPanel optionsPanel = new JPanel();
        nameCheckBox = new JCheckBox("Name", true);
        authorCheckBox = new JCheckBox("Author id", true);
        studioCheckBox = new JCheckBox("Studio", true);
        genreCheckBox = new JCheckBox("Genre", true);
        yearAiredCheckBox = new JCheckBox("Year Aired", true);
        statusCheckBox = new JCheckBox("Status", true);
        optionsPanel.add(nameCheckBox);
        optionsPanel.add(authorCheckBox);
        optionsPanel.add(studioCheckBox);
        optionsPanel.add(genreCheckBox);
        optionsPanel.add(yearAiredCheckBox);
        optionsPanel.add(statusCheckBox);
        animeTable = new JTable();
        JScrollPane animeScrollPane = new JScrollPane(animeTable);
        JPanel animeBtnPanel = new JPanel();
        addAnimeBtn = new JButton("Add Anime");
        editAnimeBtn = new JButton("Edit Anime");
        deleteAnimeBtn = new JButton("Delete Anime");
        fetchAnimeBtn = new JButton("Refresh");
        filterAnimeBtn = new JButton("Filter");
        joinAnimeBtn = new JButton("What did a studio produce?");
        grpAnimeBtn = new JButton("Amount of anime from each genre");
        nestedgrpAnimeBtn = new JButton("Amount of anime produced by each Studio in last 4 years");
        aggrHAnimeBtn = new JButton("Show Experienced Studio");
        divAnimeBtn = new JButton("Show popular anime");
        optionsPanel.add(fetchAnimeBtn);
        fetchAnimeBtn.addActionListener(e -> fetchData(nameCheckBox.isSelected(), authorCheckBox.isSelected(),
                studioCheckBox.isSelected(), genreCheckBox.isSelected(),
                yearAiredCheckBox.isSelected(), statusCheckBox.isSelected()));
        optionsPanel.add(addAnimeBtn);
        addAnimeBtn.addActionListener(e -> showAddAnimeDialog());
        optionsPanel.add(editAnimeBtn);
        editAnimeBtn.addActionListener(e -> showEditAnimeDialog());
        optionsPanel.add(deleteAnimeBtn);
        deleteAnimeBtn.addActionListener(e -> showDeleteAnimeDialog());
        optionsPanel.add(filterAnimeBtn);
        filterAnimeBtn.addActionListener(e -> showFilterAnimeDialog());
        animeBtnPanel.add(joinAnimeBtn);
        joinAnimeBtn.addActionListener(e -> showJoinAnimeDialog());
        animeBtnPanel.add(grpAnimeBtn);
        grpAnimeBtn.addActionListener(e -> grpAnimeDialog());
        animeBtnPanel.add(nestedgrpAnimeBtn);
        nestedgrpAnimeBtn.addActionListener(e -> nestedgrpAnimeDialog());
        animeBtnPanel.add(aggrHAnimeBtn);
        aggrHAnimeBtn.addActionListener(e->showExperiencedDialog());
        animeBtnPanel.add(divAnimeBtn);
        divAnimeBtn.addActionListener(e->showPopularDialog());
        animePanel.add(optionsPanel, BorderLayout.NORTH);
        animePanel.add(animeScrollPane, BorderLayout.CENTER);
        animePanel.add(animeBtnPanel, BorderLayout.SOUTH);
    }

    private void showPopularDialog() {
        Vector<String> result = databaseController.getAnimeReviewedByAllUsers();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Name", result);
        animeTable.setModel(model);
    }

    private void showExperiencedDialog() {
        Vector<String> result = databaseController.getStudiosWithMultipleAnime();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Studio", result);
        animeTable.setModel(model);
    }

    private void nestedgrpAnimeDialog() {
        Vector<Vector<String>> result = databaseController.nestedGroupByStudioCountAnime();
        Vector<String> animeColumns = new Vector<>();
        animeColumns.add("Studio");
        animeColumns.add("Amount");
        DefaultTableModel model = new DefaultTableModel(result, animeColumns);
        animeTable.setModel(model);
    }

    private void grpAnimeDialog() {
        Vector<Vector<String>> result = databaseController.groupByGenreCountAnime();
        Vector<String> animeColumns = new Vector<>();
        animeColumns.add("Genre");
        animeColumns.add("Amount");
        DefaultTableModel model = new DefaultTableModel(result, animeColumns);
        animeTable.setModel(model);
    }

    private void showFilterAnimeDialog() {
        JPanel dialogPanel = new JPanel();
        JTextField condition = new JTextField(20);
        dialogPanel.add(new JLabel("Enter Condition Here:"));
        dialogPanel.add(condition);
        int result = JOptionPane.showConfirmDialog(null, dialogPanel, "Filter Anime", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String cond = condition.getText();
            Vector<String> animeColumns = new Vector<>();
            try {
                Vector<Vector<String>> temp = databaseController.filterAnime(cond);
                if (temp.isEmpty()) {
                    System.out.println("No anime found");
                    warningNR();
                } else {
                    animeColumns.add("Name");
                    animeColumns.add("Author id");
                    animeColumns.add("Studio");
                    animeColumns.add("Genre");
                    animeColumns.add("Year Aired");
                    animeColumns.add("Status");
                    DefaultTableModel model = new DefaultTableModel(temp, animeColumns);
                    animeTable.setModel(model);
                }
            } catch (Exception e) {
                warningException(e.getMessage());
            }
        }
    }

    private void showJoinAnimeDialog() {
        JPanel dialogPanel = new JPanel();
        JTextField studioField = new JTextField(10);
        dialogPanel.add(new JLabel("Studio:"));
        dialogPanel.add(studioField);

        int result = JOptionPane.showConfirmDialog(null, dialogPanel, "What did a studio produce?",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String studio = studioField.getText();
            Vector<String> temp = databaseController.getAnimeByStudio(studio);
            if (temp.isEmpty()) {
                warningSDNE();
            } else {
                DefaultTableModel model = new DefaultTableModel();
                model.addColumn("Name",temp);
                animeTable.setModel(model);
            }

        }
    }

    private void showAddAnimeDialog() {
        JTextField nameField = new JTextField(10);
        JTextField authorField = new JTextField(10);
        JTextField studioField = new JTextField(10);
        JTextField genreField = new JTextField(10);
        JTextField yearField = new JTextField(10);
        JComboBox<String> statusComboBox = new JComboBox<>(new String[] {"Airing", "Aired", "Unaired"});
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new GridLayout(0, 2));
        dialogPanel.add(new JLabel("Anime Name:"));
        dialogPanel.add(nameField);
        dialogPanel.add(new JLabel("Author id:"));
        dialogPanel.add(authorField);
        dialogPanel.add(new JLabel("Studio:"));
        dialogPanel.add(studioField);
        dialogPanel.add(new JLabel("Genre:"));
        dialogPanel.add(genreField);
        dialogPanel.add(new JLabel("Year Released:"));
        dialogPanel.add(yearField);
        dialogPanel.add(new JLabel("Status:"));
        dialogPanel.add(statusComboBox);
        int result = JOptionPane.showConfirmDialog(null, dialogPanel, "Add Anime",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (nameField.getText().isEmpty() || authorField.getText().isEmpty() || studioField.getText().isEmpty() || genreField.getText().isEmpty() || yearField.getText().isEmpty()) {
                warningEmpty();
            } else {
                try {
                    String name = nameField.getText();
                    int author = Integer.parseInt(authorField.getText());
                    String studio = studioField.getText();
                    String genre = genreField.getText();
                    int year = Integer.parseInt(yearField.getText());
                    String status = (String) statusComboBox.getSelectedItem();
                    Boolean added = databaseController.insertAnime(name, author, studio, genre, year, status);
                    if (added) {
                        confirmation();
                        fetchData(nameCheckBox.isSelected(),authorCheckBox.isSelected(),studioCheckBox.isSelected(),genreCheckBox.isSelected(),yearAiredCheckBox.isSelected(),statusCheckBox.isSelected());
                    }
                } catch (SQLException e) {
                    warningWI();
                } catch (Exception e) {
                    warningException(e.getMessage());
                }
            }
        }
    }

    private void showDeleteAnimeDialog() {
        JPanel dialogPanel = new JPanel();
        JComboBox<String> nameComboBox = new JComboBox<>();
        Vector<String> list = databaseController.printAnime();
        for (int i = 0; i < list.size(); i++) {
            nameComboBox.addItem(list.get(i));
        }
        dialogPanel.setLayout(new GridLayout(0, 2));
        dialogPanel.add(new JLabel("Anime Name:"));
        dialogPanel.add(nameComboBox);
        int result = JOptionPane.showConfirmDialog(null, dialogPanel, "Delete Anime",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameComboBox.getSelectedItem().toString();
            databaseController.deleteAnime(name);
            confirmation();
            fetchData(nameCheckBox.isSelected(),authorCheckBox.isSelected(),studioCheckBox.isSelected(),genreCheckBox.isSelected(),yearAiredCheckBox.isSelected(),statusCheckBox.isSelected());
        }
    }

    public static void warningEmpty() {
        JPanel warningFrame = new JPanel();
        JLabel title = new JLabel("Fields cannot be empty!");
        warningFrame.add(title);
        JOptionPane.showConfirmDialog(null, warningFrame, "Warning", JOptionPane.PLAIN_MESSAGE);
        warningFrame.setVisible(true);
    }

    public static void warningWI() {
        JPanel warningFrame = new JPanel();
        JLabel title = new JLabel("Wrong input, please retry");
        warningFrame.add(title);
        JOptionPane.showConfirmDialog(null, warningFrame, "Warning", JOptionPane.PLAIN_MESSAGE);
        warningFrame.setVisible(true);
    }

    public static void warningDNE() {
        JPanel warningFrame = new JPanel();
        JLabel title = new JLabel("The anime does not exist in our database");
        warningFrame.add(title);
        JOptionPane.showConfirmDialog(null, warningFrame, "Warning", JOptionPane.PLAIN_MESSAGE);
        warningFrame.setVisible(true);
    }

    public static void warningAE() {
        JPanel warningFrame = new JPanel();
        JLabel title = new JLabel("The Studio/Author/Genre does not exist in our database");
        warningFrame.add(title);
        JOptionPane.showConfirmDialog(null, warningFrame, "Warning", JOptionPane.PLAIN_MESSAGE);
        warningFrame.setVisible(true);
    }

    public static void warningSDNE() {
        JPanel warningFrame = new JPanel();
        JLabel title = new JLabel("The studio does not exist in our database");
        warningFrame.add(title);
        JOptionPane.showConfirmDialog(null, warningFrame, "Warning", JOptionPane.PLAIN_MESSAGE);
        warningFrame.setVisible(true);
    }

    public static void warningException(String s) {
        JPanel warningFrame = new JPanel();
        JLabel title = new JLabel(s);
        warningFrame.add(title);
        JOptionPane.showConfirmDialog(null, warningFrame, "Warning", JOptionPane.PLAIN_MESSAGE);
        warningFrame.setVisible(true);
    }
    
    public static void warningNR() {
        JPanel warningFrame = new JPanel();
        JLabel title = new JLabel("No such anime exist in our database");
        warningFrame.add(title);
        JOptionPane.showConfirmDialog(null, warningFrame, "Warning", JOptionPane.PLAIN_MESSAGE);
        warningFrame.setVisible(true);
    }

    public static void confirmation() {
        JPanel warningFrame = new JPanel();
        JLabel title = new JLabel("Record has been updated");
        warningFrame.add(title);
        JOptionPane.showConfirmDialog(null, warningFrame, "Confirmation", JOptionPane.PLAIN_MESSAGE);
        warningFrame.setVisible(true);
    }

    private void showEditAnimeDialog() {
        JComboBox<String> nameComboBox = new JComboBox<>();
        Vector<String> list = databaseController.printAnime();
        for (int i = 0; i < list.size(); i++) {
            nameComboBox.addItem(list.get(i));
        }
        JTextField authorField = new JTextField(10);
        JTextField studioField = new JTextField(10);
        JTextField genreField = new JTextField(10);
        JTextField yearField = new JTextField(10);
        JComboBox<String> statusComboBox = new JComboBox<>(new String[] {"Airing", "Aired", "Unaired"});
        JPanel dialogFirstPanel = new JPanel();
        dialogFirstPanel.setLayout(new GridLayout(0, 2));
        dialogFirstPanel.add(new JLabel("Anime Name:"));
        dialogFirstPanel.add(nameComboBox);
        int result = JOptionPane.showConfirmDialog(null, dialogFirstPanel, "Edit Anime",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Vector<String> vector = databaseController.getAnimeAttributes(nameComboBox.getSelectedItem().toString());
            JPanel dialogPanel = new JPanel();
            authorField.setText(vector.get(0));
            studioField.setText(vector.get(1));
            genreField.setText(vector.get(2));
            yearField.setText(vector.get(3));
            statusComboBox.setSelectedItem(vector.get(4));
            dialogPanel.add(new JLabel("Author id:"));
            dialogPanel.add(authorField);
            dialogPanel.add(new JLabel("Studio:"));
            dialogPanel.add(studioField);
            dialogPanel.add(new JLabel("Genre:"));
            dialogPanel.add(genreField);
            dialogPanel.add(new JLabel("Year Aired:"));
            dialogPanel.add(yearField);
            dialogPanel.add(new JLabel("Status:"));
            dialogPanel.add(statusComboBox);
            int result2 = JOptionPane.showConfirmDialog(null, dialogPanel, "Edit Anime",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result2 == JOptionPane.OK_OPTION) {
                if (authorField.getText().isEmpty() || studioField.getText().isEmpty() || genreField.getText().isEmpty() || yearField.getText().isEmpty()) {
                    warningEmpty();
                } else {
                    try {
                        String name = nameComboBox.getSelectedItem().toString();
                        int author = Integer.parseInt(authorField.getText());
                        String studio = studioField.getText();
                        String genre = genreField.getText();
                        int year = Integer.parseInt(yearField.getText());
                        String status = (String) statusComboBox.getSelectedItem();
                        // Pass to backend from here
                        Boolean done = databaseController.updateAnime(name, author, studio, genre, year, status);
                        if (!done) {
                            warningDNE();
                        } else {
                            confirmation();
                            fetchData(nameCheckBox.isSelected(),authorCheckBox.isSelected(),studioCheckBox.isSelected(),genreCheckBox.isSelected(),yearAiredCheckBox.isSelected(),statusCheckBox.isSelected());
                        }
                    } catch (Exception e) {
                        warningAE();
                    }
                }
        }
        }
    }


    private void fetchData(boolean showName, boolean showAuthor, boolean showStudio, boolean showGenre, boolean showYearAired, boolean showStatus) {
        Vector<String> animeColumns = new Vector<>();
        if (showName) {
            animeColumns.add("Name");
        }
        if (showAuthor){
            animeColumns.add("Author ID");
        }
        if (showStudio) {
            animeColumns.add("Studio");
        }
        if (showGenre) {
            animeColumns.add("Genre");
        }
        if (showYearAired) {
            animeColumns.add("Year Aired");
        }
        if (showStatus) {
            animeColumns.add("Status");
        }
        Vector<Vector<String>> temp = databaseController.getAnimeColumns(showName,showAuthor,showStudio,showGenre,showYearAired,showStatus);
        DefaultTableModel model = new DefaultTableModel(temp,animeColumns);
        // Call Enoch's projection function
        animeTable.setModel(model);
    }

    public static void main(String[] args) {
        System.out.println("cunt");
        SwingUtilities.invokeLater(() -> {
            UI ui = new UI();
            ui.setVisible(true);
        });
    }
}
