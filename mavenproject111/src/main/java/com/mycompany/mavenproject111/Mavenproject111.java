/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mavenproject111;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author Thinkpad
 */
public class Mavenproject111 {

  private Connection connection;

    public Mavenproject111() {
        try {
            // Attempt to establish connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/GestionLivres", "root", "r-a-d-30-jaa-11-08");
            System.out.println("Connexion à la base de données établie avec succès");
        } catch (SQLException e) {
            // Print detailed error and set connection to null
            System.err.println("Échec de la connexion à la base de données: " + e.getMessage());
            connection = null;
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    // Method to get table data
    public ResultSet getTableData(String tableName) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("No database connection");
        }
        Statement stmt = connection.createStatement();
        return stmt.executeQuery("SELECT * FROM " + tableName);
    }

    public void AjoutAuteur(int idAuteur, String nom) {
        if (connection == null) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO Auteur VALUES (?, ?)");
            ps.setInt(1, idAuteur);
            ps.setString(2, nom);
            ps.executeUpdate();
            System.out.println("Auteur ajouté avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur SQL lors de l'ajout de l'auteur: " + e.getMessage());
        }
    }

    public void ModifAuteur(int idAuteur, String nomAuteur) throws SQLException {
        String query = "UPDATE Auteur SET Nom = ? WHERE ID_Auteur = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, nomAuteur);
        pstmt.setInt(2, idAuteur);
        pstmt.executeUpdate();
    }
    // Method to add an editor if it doesn't exist
    public void AjoutEditeur(String nomEditeur) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        try {
            // Check if editor exists
            String checkQuery = "SELECT COUNT(*) FROM Editeur WHERE Nom_Editeur = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, nomEditeur.trim()); // Trim to avoid whitespace issues
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            System.out.println("Nombre d'éditeurs trouvés avec Nom_Editeur '" + nomEditeur + "': " + count);

            if (count == 0) { // Editor doesn't exist
                String insertQuery = "INSERT INTO Editeur (Nom_Editeur) VALUES (?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setString(1, nomEditeur.trim());
                int rowsAffected = insertStmt.executeUpdate();
                System.out.println("Éditeur ajouté avec succès: " + nomEditeur + " (" + rowsAffected + " ligne(s) affectée(s))");
            } else {
                System.out.println("Éditeur '" + nomEditeur + "' existe déjà.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'éditeur: " + e.getMessage());
            throw e; // Re-throw to be caught by the caller
        }
    }
    

    public void AjoutLivre(int idLivre, String titre, String nomEditeur, int nbrExemplaires) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        // Ensure the editor exists before adding the book
        AjoutEditeur(nomEditeur);
        
        String query = "INSERT INTO Livre (Id_Livre, Titre, Nom_Editeur, Nbr_exemplaires) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, idLivre);
        pstmt.setString(2, titre);
        pstmt.setString(3, nomEditeur.trim()); // Trim to match the editor added
        pstmt.setInt(4, nbrExemplaires);
        pstmt.executeUpdate();
        System.out.println("Livre ajouté avec succès: " + titre);
    }

    public void ModifLivre(int idLivre, String titre, String nomEditeur, int nbrExemplaires) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        // Ensure the editor exists before modifying the book
        AjoutEditeur(nomEditeur);
        
        String query = "UPDATE Livre SET Titre = ?, Nom_Editeur = ?, Nbr_exemplaires = ? WHERE Id_Livre = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, titre);
        pstmt.setString(2, nomEditeur.trim());
        pstmt.setInt(3, nbrExemplaires);
        pstmt.setInt(4, idLivre);
        pstmt.executeUpdate();
        System.out.println("Livre modifié avec succès: " + titre);
    }

    public String AfficheAuteurs(int idLivre) {
        StringBuilder result = new StringBuilder();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT Nom FROM Auteur A JOIN Ecrit E ON A.ID_Auteur = E.ID_Auteur WHERE E.ID_Livre = ?");
            ps.setInt(1, idLivre);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.append(rs.getString("Nom")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    // Modified AfficheLivresAuteurs - already correctly implemented
    public String AfficheLivresAuteurs(String nomAuteur) {
        StringBuilder result = new StringBuilder();
        try {
            if (!isConnected()) {
                return "Erreur: Pas de connexion à la base de données";
            }
            PreparedStatement ps = connection.prepareStatement(
                "SELECT L.Titre, L.Nom_Editeur, L.Nbr_exemplaires " +
                "FROM Livre L " +
                "JOIN Ecrit E ON L.Id_Livre = E.ID_Livre " +
                "JOIN Auteur A ON A.ID_Auteur = E.ID_Auteur " +
                "WHERE A.Nom = ?");
            ps.setString(1, nomAuteur.trim());
            ResultSet rs = ps.executeQuery();
            if (!rs.isBeforeFirst()) { // Check if result set is empty
                result.append("Aucun livre trouvé pour l'auteur: ").append(nomAuteur);
            } else {
                result.append("Livres de ").append(nomAuteur).append(":\n");
                while (rs.next()) {
                    result.append("Titre: ").append(rs.getString("Titre"))
                          .append(", Éditeur: ").append(rs.getString("Nom_Editeur"))
                          .append(", Exemplaires: ").append(rs.getInt("Nbr_exemplaires"))
                          .append("\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur SQL: " + e.getMessage();
        }
        return result.toString();
    }

    // Modified AfficheLivresEditeur
    public String AfficheLivresEditeur(String nomEditeur) {
        StringBuilder result = new StringBuilder();
        try {
            if (!isConnected()) {
                return "Erreur: Pas de connexion à la base de données";
            }
            PreparedStatement ps = connection.prepareStatement(
                "SELECT Id_Livre, Titre, Nbr_exemplaires " +
                "FROM Livre " +
                "WHERE Nom_Editeur = ?");
            ps.setString(1, nomEditeur.trim());
            ResultSet rs = ps.executeQuery();
            if (!rs.isBeforeFirst()) { // Check if result set is empty
                result.append("Aucun livre trouvé pour l'éditeur: ").append(nomEditeur);
            } else {
                result.append("Livres publiés par ").append(nomEditeur).append(":\n");
                while (rs.next()) {
                    result.append("ID: ").append(rs.getInt("Id_Livre"))
                          .append(", Titre: ").append(rs.getString("Titre"))
                          .append(", Exemplaires: ").append(rs.getInt("Nbr_exemplaires"))
                          .append("\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erreur SQL: " + e.getMessage();
        }
        return result.toString();
    }

    public static void main(String[] args) {
        new GestionLivresGUI();
    }
}

class GestionLivresGUI {
    private Mavenproject111 gestion;
    private JTextField idAuteurField, nomAuteurField, idLivreField, titreField, nomEditeurField, nbrExemplairesField;
    private JTextArea outputArea;
    private JTable auteurTable, livreTable;
    private DefaultTableModel auteurModel, livreModel;
    private JScrollPane outputScrollPane;

    public GestionLivresGUI() {
        gestion = new Mavenproject111();
        
        // Check if connection was established
        if (!gestion.isConnected()) {
            JOptionPane.showMessageDialog(null, 
                "Impossible de se connecter à la base de données. Vérifiez les paramètres de connexion.",
                "Erreur de Connexion", 
                JOptionPane.ERROR_MESSAGE);
        }

        JFrame frame = new JFrame("Gestion de Livres");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        
        // Create table models
        auteurModel = new DefaultTableModel(new String[]{"ID_Auteur", "Nom"}, 0);
        livreModel = new DefaultTableModel(new String[]{"Id_Livre", "Titre", "Nom_Editeur", "Nbr_exemplaires"}, 0);
        
        // Create tables
        auteurTable = new JTable(auteurModel);
        livreTable = new JTable(livreModel);
        
        // Create scroll panes for tables
        JScrollPane auteurScrollPane = new JScrollPane(auteurTable);
        JScrollPane livreScrollPane = new JScrollPane(livreTable);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(9, 2)); // Increased to 9 rows to accommodate new button

        idAuteurField = new JTextField();
        nomAuteurField = new JTextField();
        idLivreField = new JTextField();
        titreField = new JTextField();
        nomEditeurField = new JTextField();
        nbrExemplairesField = new JTextField();
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputScrollPane = new JScrollPane(outputArea);
        JButton showBooksByEditorButton = new JButton("Afficher livres par Editeur"); // New button
        
        JButton addAuteurButton = new JButton("Ajouter Auteur");
        JButton modifAuteurButton = new JButton("Modifier Auteur");
        JButton addLivreButton = new JButton("Ajouter Livre");
        JButton modifLivreButton = new JButton("Modifier Livre");
        JButton refreshButton = new JButton("Rafraîchir Tables");
        JButton showBooksByAuthorButton = new JButton("Afficher livres par Auteurs"); // New button

        inputPanel.add(new JLabel("ID Auteur:"));
        inputPanel.add(idAuteurField);
        inputPanel.add(new JLabel("Nom Auteur:"));
        inputPanel.add(nomAuteurField);
        inputPanel.add(new JLabel("ID Livre:"));
        inputPanel.add(idLivreField);
        inputPanel.add(new JLabel("Titre Livre:"));
        inputPanel.add(titreField);
        inputPanel.add(new JLabel("Nom Éditeur:"));
        inputPanel.add(nomEditeurField);
        inputPanel.add(new JLabel("Nombre Exemplaires:"));
        inputPanel.add(nbrExemplairesField);
        inputPanel.add(addAuteurButton);
        inputPanel.add(modifAuteurButton);
        inputPanel.add(addLivreButton);
        inputPanel.add(modifLivreButton);
        inputPanel.add(refreshButton);
        inputPanel.add(showBooksByAuthorButton); // Added new button
        inputPanel.add(showBooksByEditorButton); // Added new button

        // Split pane for tables
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
            auteurScrollPane, livreScrollPane);
        splitPane.setDividerLocation(400);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(outputScrollPane, BorderLayout.SOUTH);

        // Method to refresh tables
        refreshTables();

        // Action Listeners with table refresh
        addAuteurButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                int idAuteur = Integer.parseInt(idAuteurField.getText().trim());
                String nomAuteur = nomAuteurField.getText().trim();
                
                if (nomAuteur.isEmpty()) {
                    outputArea.setText("Erreur: Le nom de l'auteur ne peut pas être vide");
                    return;
                }
                
                gestion.AjoutAuteur(idAuteur, nomAuteur);
                outputArea.setText("Auteur ajouté avec succès: " + nomAuteur);
                idAuteurField.setText("");
                nomAuteurField.setText("");
                refreshTables();
            } catch (Exception ex) {
                outputArea.setText("Erreur: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        modifAuteurButton.addActionListener(e -> {
            try {
                gestion.ModifAuteur(Integer.parseInt(idAuteurField.getText()), 
                    nomAuteurField.getText());
                outputArea.setText("Auteur modifié avec succès");
                refreshTables();
            } catch (Exception ex) {
                outputArea.setText("Erreur: " + ex.getMessage());
            }
        });

        // Action Listener for Ajouter Livre
        addLivreButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                int idLivre = Integer.parseInt(idLivreField.getText().trim());
                String titre = titreField.getText().trim();
                String nomEditeur = nomEditeurField.getText().trim();
                int nbrExemplaires = Integer.parseInt(nbrExemplairesField.getText().trim());

                if (titre.isEmpty() || nomEditeur.isEmpty()) {
                    outputArea.setText("Erreur: Le titre et le nom de l'éditeur ne peuvent pas être vides");
                    return;
                }
                if (nbrExemplaires < 0) {
                    outputArea.setText("Erreur: Le nombre d'exemplaires ne peut pas être négatif");
                    return;
                }

                gestion.AjoutLivre(idLivre, titre, nomEditeur, nbrExemplaires);
                outputArea.setText("Livre ajouté avec succès: " + titre);
                idLivreField.setText("");
                titreField.setText("");
                nomEditeurField.setText("");
                nbrExemplairesField.setText("");
                refreshTables();
            } catch (NumberFormatException ex) {
                outputArea.setText("Erreur: ID Livre et Nombre Exemplaires doivent être des nombres valides");
            } catch (SQLException ex) {
                outputArea.setText("Erreur SQL: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Action Listener for Modifier Livre
        modifLivreButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                int idLivre = Integer.parseInt(idLivreField.getText().trim());
                String titre = titreField.getText().trim();
                String nomEditeur = nomEditeurField.getText().trim();
                int nbrExemplaires = Integer.parseInt(nbrExemplairesField.getText().trim());

                if (titre.isEmpty() || nomEditeur.isEmpty()) {
                    outputArea.setText("Erreur: Le titre et le nom de l'éditeur ne peuvent pas être vides");
                    return;
                }
                if (nbrExemplaires < 0) {
                    outputArea.setText("Erreur: Le nombre d'exemplaires ne peut pas être négatif");
                    return;
                }

                gestion.ModifLivre(idLivre, titre, nomEditeur, nbrExemplaires);
                outputArea.setText("Livre modifié avec succès: " + titre);
                refreshTables();
            } catch (NumberFormatException ex) {
                outputArea.setText("Erreur: ID Livre et Nombre Exemplaires doivent être des nombres valides");
            } catch (SQLException ex) {
                outputArea.setText("Erreur SQL: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        // New Action Listener for Afficher livres par Auteurs
        showBooksByAuthorButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                String nomAuteur = nomAuteurField.getText().trim();
                
                if (nomAuteur.isEmpty()) {
                    outputArea.setText("Erreur: Veuillez entrer le nom de l'auteur");
                    return;
                }
                
                String result = gestion.AfficheLivresAuteurs(nomAuteur);
                outputArea.setText(result);
            } catch (Exception ex) {
                outputArea.setText("Erreur: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        // New Action Listener for Afficher livres par Editeur
        showBooksByEditorButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                String nomEditeur = nomEditeurField.getText().trim();
                
                if (nomEditeur.isEmpty()) {
                    outputArea.setText("Erreur: Veuillez entrer le nom de l'éditeur");
                    return;
                }
                
                String result = gestion.AfficheLivresEditeur(nomEditeur);
                outputArea.setText(result);
            } catch (Exception ex) {
                outputArea.setText("Erreur: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        refreshButton.addActionListener(e -> refreshTables());

        frame.setVisible(true);
    }

    private void refreshTables() {
        try {
            ResultSet rsAuteur = gestion.getTableData("Auteur");
            auteurModel.setRowCount(0);
            while (rsAuteur.next()) {
                auteurModel.addRow(new Object[]{
                    rsAuteur.getInt("ID_Auteur"),
                    rsAuteur.getString("Nom")
                });
            }

            ResultSet rsLivre = gestion.getTableData("Livre");
            livreModel.setRowCount(0);
            while (rsLivre.next()) {
                livreModel.addRow(new Object[]{
                    rsLivre.getInt("Id_Livre"),
                    rsLivre.getString("Titre"),
                    rsLivre.getString("Nom_Editeur"),
                    rsLivre.getInt("Nbr_exemplaires")
                });
            }
        } catch (SQLException e) {
            outputArea.setText("Erreur lors du rafraîchissement des tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
