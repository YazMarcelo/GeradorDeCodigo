/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classededados;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import util.Conexao;

/**
 *
 * @author HELM
 */
public class Propriedades {

    Conexao cone = new Conexao();
    Connection conn;
    DatabaseMetaData meta;
    ResultSet schemas;
    ArrayList<String> tabelasEPadroes = new ArrayList<>();

    public ArrayList<String> getSchemasNames() throws SQLException {
        conn = cone.getConexao();
        meta = conn.getMetaData();
        schemas = meta.getSchemas();

        ArrayList<String> listaSchemas = new ArrayList<>();

        int i = 0;
        while (schemas.next()) {
            String tableSchema = schemas.getString(1);    // "TABLE_SCHEM"
            if (i >= 2) {
                listaSchemas.add(tableSchema);
            }
            i++;
        }
        conn.close();
        return listaSchemas;
    }

    public ArrayList<String> getTabelas(String schema) throws SQLException {
        conn = cone.getConexao();
        meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, schema, "%", null);

        ArrayList<String> listaTabelas = new ArrayList<>();
        int i = 0;
        while (rs.next()) {
            String finaldalista = rs.getString(3).substring(rs.getString(3).length()-3,rs.getString(3).length());
            if(!(finaldalista).equals("seq") && !(finaldalista).equals("key")){
            listaTabelas.add(rs.getString(3));
            }
            i++;
        }
        conn.close();
        return listaTabelas;
    }

        public ArrayList<String> getColunas(String tabela, String schema) throws SQLException {
        conn = cone.getConexao();
        meta = conn.getMetaData();
        ResultSet rs = meta.getColumns(null, schema, tabela, null);

        ArrayList<String> listaColunas = new ArrayList<>();
        while (rs.next()) {
            listaColunas.add(rs.getString("DATA_TYPE") + ";" + rs.getString("COLUMN_NAME"));
        }

        conn.close();
        return listaColunas;
    }
    
    public void getTabelaEPadroes() throws SQLException{
        ArrayList<String> lista = getTabelas("public");
        ArrayList<String> novaLista = new ArrayList<>();
        
        for (int i = 0; i < lista.size(); i++) {
            String finaldalista = lista.get(i).substring(lista.get(i).length()-3,lista.get(i).length());
            if(!(finaldalista).equals("seq") && !(finaldalista).equals("key")){
            String tabela = lista.get(i);
            String primary = getPrimaryKey(tabela, "public");
            String padrao = primary.substring(0,4);
            
            novaLista.add(tabela+"-"+padrao);
            }
            
        }
        tabelasEPadroes = novaLista;
    }
    
    public String getPrimaryKey(String tabela, String schema) throws SQLException {
        conn = cone.getConexao();
        meta = conn.getMetaData();
        ResultSet rs = meta.getPrimaryKeys(null, schema, tabela);
        
        String chave = "";
        while (rs.next()) {
        chave = rs.getString("COLUMN_NAME");
        }
        conn.close();
        return chave;
    }
    
    public ArrayList<String> getForeignKey(String tabela) throws SQLException {
        conn = cone.getConexao();
        meta = conn.getMetaData();
        ResultSet rs = meta.getImportedKeys(null, null, tabela);

        ArrayList<String> listaColunas = new ArrayList<>();
        while (rs.next()) {
            listaColunas.add(rs.getString(4));
        }

        conn.close();
        return listaColunas;
    }
    
    public boolean isForeignKey(String coluna, String tabela) throws SQLException{
        ArrayList<String> listaForeignKeys = getForeignKey(tabela);
        
        for (int i = 0; i < listaForeignKeys.size(); i++) {
            if(coluna.equals(listaForeignKeys.get(i))){
                return true;
            }
        }
        return false;
    }
    
    public String getTabelaForeignKey(String coluna) throws SQLException{
        getTabelaEPadroes();
        String padrao = coluna.substring(0,4);
        
        for (int i = 0; i < tabelasEPadroes.size(); i++) {
            String linha = tabelasEPadroes.get(i);
            String[] vetor = linha.split(";");
            
            if(padrao.equals(vetor[1])){
                return vetor[0];
            }
        }
        return null;
    }
    
    public String getAAA() throws SQLException{
        
        return null;
    }

}
