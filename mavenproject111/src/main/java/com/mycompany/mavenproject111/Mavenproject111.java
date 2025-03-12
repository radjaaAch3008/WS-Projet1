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
    // Modified AjoutEditeur to include address and phone
    public void AjoutEditeur(String nomEditeur, String adresse, String telephone) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        try {
            String checkQuery = "SELECT COUNT(*) FROM Editeur WHERE Nom_Editeur = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, nomEditeur.trim());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count == 0) {
                String insertQuery = "INSERT INTO Editeur (Nom_Editeur, Adresse, Telephone) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setString(1, nomEditeur.trim());
                insertStmt.setString(2, adresse.trim());
                insertStmt.setString(3, telephone.trim());
                int rowsAffected = insertStmt.executeUpdate();
                System.out.println("Éditeur ajouté avec succès: " + nomEditeur + " (" + rowsAffected + " ligne(s) affectée(s))");
            } else {
                System.out.println("Éditeur '" + nomEditeur + "' existe déjà.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'éditeur: " + e.getMessage());
            throw e;
        }
    }

    // New method to modify editor details
    public void ModifEditeur(String nomEditeur, String adresse, String telephone) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        String query = "UPDATE Editeur SET Adresse = ?, Telephone = ? WHERE Nom_Editeur = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, adresse.trim());
        pstmt.setString(2, telephone.trim());
        pstmt.setString(3, nomEditeur.trim());
        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Éditeur modifié avec succès");
        } else {
            System.out.println("Aucun éditeur trouvé avec ce nom");
        }
    }
    

    public void AjoutLivre(int idLivre, String titre, String nomEditeur, int nbrExemplaires) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        AjoutEditeur(nomEditeur, "", ""); // Add empty address and phone if editor doesn't exist
        String query = "INSERT INTO Livre (Id_Livre, Titre, Nom_Editeur, Nbr_exemplaires) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, idLivre);
        pstmt.setString(2, titre);
        pstmt.setString(3, nomEditeur.trim());
        pstmt.setInt(4, nbrExemplaires);
        pstmt.executeUpdate();
        System.out.println("Livre ajouté avec succès: " + titre);
    }

    public void ModifLivre(int idLivre, String titre, String nomEditeur, int nbrExemplaires) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        AjoutEditeur(nomEditeur, "", ""); // Add empty address and phone if editor doesn't exist
        String query = "UPDATE Livre SET Titre = ?, Nom_Editeur = ?, Nbr_exemplaires = ? WHERE Id_Livre = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, titre);
        pstmt.setString(2, nomEditeur.trim());
        pstmt.setInt(3, nbrExemplaires);
        pstmt.setInt(4, idLivre);
        pstmt.executeUpdate();
        System.out.println("Livre modifié avec succès: " + titre);
    }
    
    public void AjoutEcrit(int idLivre, int idAuteur) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        String query = "INSERT INTO Ecrit (ID_Livre, ID_Auteur) VALUES (?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, idLivre);
        pstmt.setInt(2, idAuteur);
        pstmt.executeUpdate();
        System.out.println("Relation Ecrit ajoutée avec succès");
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
    
    // New delete methods
    public void SupprimeAuteur(int idAuteur) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        String query = "DELETE FROM Auteur WHERE ID_Auteur = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, idAuteur);
        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Auteur supprimé avec succès");
        } else {
            System.out.println("Aucun auteur trouvé avec cet ID");
        }
    }

    public void SupprimeLivre(int idLivre) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        String query = "DELETE FROM Livre WHERE Id_Livre = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, idLivre);
        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Livre supprimé avec succès");
        } else {
            System.out.println("Aucun livre trouvé avec cet ID");
        }
    }

    public void SupprimeEditeur(String nomEditeur) throws SQLException {
        if (!isConnected()) {
            throw new IllegalStateException("Pas de connexion à la base de données");
        }
        String query = "DELETE FROM Editeur WHERE Nom_Editeur = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, nomEditeur.trim());
        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Éditeur supprimé avec succès");
        } else {
            System.out.println("Aucun éditeur trouvé avec ce nom");
        }
    }

    public static void main(String[] args) {
        new GestionLivresGUI();
    }
}

class GestionLivresGUI {
    private Mavenproject111 gestion;
    private JTextField idAuteurField, nomAuteurField, idLivreField, titreField, nomEditeurField, 
                       nbrExemplairesField, adresseField, telephoneField;
    private JTextArea outputArea;
    private JTable auteurTable, livreTable, editeurTable, ecritTable;
    private DefaultTableModel auteurModel, livreModel, editeurModel, ecritModel;
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
        editeurModel = new DefaultTableModel(new String[]{"Nom_Editeur", "Adresse", "Telephone"}, 0);
        ecritModel = new DefaultTableModel(new String[]{"ID_Livre", "ID_Auteur"}, 0);
        
        // Create tables
        auteurTable = new JTable(auteurModel);
        livreTable = new JTable(livreModel);
        editeurTable = new JTable(editeurModel);
        ecritTable = new JTable(ecritModel);
        
        // Create scroll panes for tables
        JScrollPane auteurScrollPane = new JScrollPane(auteurTable);
        JScrollPane livreScrollPane = new JScrollPane(livreTable);
        JScrollPane editeurScrollPane = new JScrollPane(editeurTable);
        JScrollPane ecritScrollPane = new JScrollPane(ecritTable);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(14, 2)); // Increased to 12 rows for 3 new buttons

        idAuteurField = new JTextField();
        nomAuteurField = new JTextField();
        idLivreField = new JTextField();
        titreField = new JTextField();
        nomEditeurField = new JTextField();
        nbrExemplairesField = new JTextField();
        adresseField = new JTextField();      // New address field
        telephoneField = new JTextField();    // New phone field
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputScrollPane = new JScrollPane(outputArea);
        
        JButton addAuteurButton = new JButton("Ajouter Auteur");
        JButton modifAuteurButton = new JButton("Modifier Auteur");
        JButton addLivreButton = new JButton("Ajouter Livre");
        JButton modifLivreButton = new JButton("Modifier Livre");
        JButton addEditeurButton = new JButton("Ajouter Editeur");// New button
        JButton modifEditeurButton = new JButton("Modifier Editeur");// New button
        JButton refreshButton = new JButton("Rafraîchir Tables");
        JButton showBooksByAuthorButton = new JButton("Afficher livres par Auteurs");
        JButton showBooksByEditorButton = new JButton("Afficher livres par Editeur");
       

        // New delete buttons
        JButton deleteAuteurButton = new JButton("Supprimer Auteur");
        JButton deleteLivreButton = new JButton("Supprimer Livre");
        JButton deleteEditeurButton = new JButton("Supprimer Editeur");
        
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
        inputPanel.add(new JLabel("Adresse Éditeur:"));
        inputPanel.add(adresseField);
        inputPanel.add(new JLabel("Téléphone Éditeur:"));
        inputPanel.add(telephoneField);
        inputPanel.add(addAuteurButton);
        inputPanel.add(modifAuteurButton);
        inputPanel.add(addLivreButton);
        inputPanel.add(modifLivreButton);
        inputPanel.add(addEditeurButton);
        inputPanel.add(modifEditeurButton);
        inputPanel.add(refreshButton);
        inputPanel.add(showBooksByAuthorButton);
        inputPanel.add(showBooksByEditorButton);
        inputPanel.add(deleteAuteurButton);
        inputPanel.add(deleteLivreButton);
        inputPanel.add(deleteEditeurButton);

        // Create nested split panes for four tables
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
            auteurScrollPane, livreScrollPane);
        JSplitPane bottomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
            editeurScrollPane, ecritScrollPane);
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
            topSplitPane, bottomSplitPane);
        
        topSplitPane.setDividerLocation(400);
        bottomSplitPane.setDividerLocation(400);
        mainSplitPane.setDividerLocation(300);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(mainSplitPane, BorderLayout.CENTER);
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

      //outputArea.setText("Erreur: Le nombre d'exemplaires ne peut pas être négatif");


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
        // New Action Listener for Ajouter Editeur
        addEditeurButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                String nomEditeur = nomEditeurField.getText().trim();
                String adresse = adresseField.getText().trim();
                String telephone = telephoneField.getText().trim();

                if (nomEditeur.isEmpty()) {
                    outputArea.setText("Erreur: Le nom de l'éditeur ne peut pas être vide");
                    return;
                }

                gestion.AjoutEditeur(nomEditeur, adresse, telephone);
                outputArea.setText("Éditeur ajouté avec succès: " + nomEditeur);
                nomEditeurField.setText("");
                adresseField.setText("");
                telephoneField.setText("");
                refreshTables();
            } catch (SQLException ex) {
                outputArea.setText("Erreur SQL: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // New Action Listener for Modifier Editeur
        modifEditeurButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                String nomEditeur = nomEditeurField.getText().trim();
                String adresse = adresseField.getText().trim();
                String telephone = telephoneField.getText().trim();

                if (nomEditeur.isEmpty()) {
                    outputArea.setText("Erreur: Le nom de l'éditeur ne peut pas être vide");
                    return;
                }

                gestion.ModifEditeur(nomEditeur, adresse, telephone);
                outputArea.setText("Éditeur modifié avec succès: " + nomEditeur);
                refreshTables();
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
       // New Action Listeners for delete buttons
        deleteAuteurButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                String idAuteurText = idAuteurField.getText().trim();
                if (idAuteurText.isEmpty()) {
                    outputArea.setText("Erreur: Veuillez entrer l'ID de l'auteur à supprimer");
                    return;
                }
                int idAuteur = Integer.parseInt(idAuteurText);
                gestion.SupprimeAuteur(idAuteur);
                outputArea.setText("Auteur supprimé avec succès");
                refreshTables();
            } catch (NumberFormatException ex) {
                outputArea.setText("Erreur: L'ID auteur doit être un nombre valide");
            } catch (SQLException ex) {
                outputArea.setText("Erreur SQL: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        deleteLivreButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                String idLivreText = idLivreField.getText().trim();
                if (idLivreText.isEmpty()) {
                    outputArea.setText("Erreur: Veuillez entrer l'ID du livre à supprimer");
                    return;
                }
                int idLivre = Integer.parseInt(idLivreText);
                gestion.SupprimeLivre(idLivre);
                outputArea.setText("Livre supprimé avec succès");
                refreshTables();
            } catch (NumberFormatException ex) {
                outputArea.setText("Erreur: L'ID livre doit être un nombre valide");
            } catch (SQLException ex) {
                outputArea.setText("Erreur SQL: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        deleteEditeurButton.addActionListener(e -> {
            try {
                if (!gestion.isConnected()) {
                    outputArea.setText("Erreur: Pas de connexion à la base de données");
                    return;
                }
                String nomEditeur = nomEditeurField.getText().trim();
                if (nomEditeur.isEmpty()) {
                    outputArea.setText("Erreur: Veuillez entrer le nom de l'éditeur à supprimer");
                    return;
                }
                gestion.SupprimeEditeur(nomEditeur);
                outputArea.setText("Éditeur supprimé avec succès");
                refreshTables();
            } catch (SQLException ex) {
                outputArea.setText("Erreur SQL: " + ex.getMessage());
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

            // Updated to include Adresse and Telephone
            ResultSet rsEditeur = gestion.getTableData("Editeur");
            editeurModel.setRowCount(0);
            while (rsEditeur.next()) {
                editeurModel.addRow(new Object[]{
                    rsEditeur.getString("Nom_Editeur"),
                    rsEditeur.getString("Adresse"),
                    rsEditeur.getString("Telephone")
                });
            }

            ResultSet rsEcrit = gestion.getTableData("Ecrit");
            ecritModel.setRowCount(0);
            while (rsEcrit.next()) {
                ecritModel.addRow(new Object[]{
                    rsEcrit.getInt("ID_Livre"),
                    rsEcrit.getInt("ID_Auteur")
                });
            }

        } catch (SQLException e) {
            outputArea.setText("Erreur lors du rafraîchissement des tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}