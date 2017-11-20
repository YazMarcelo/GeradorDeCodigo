package teste;

import classededados.Propriedades;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Persistencia {

    Propriedades props = new Propriedades();
    String tableName = "";
    String schemaName = "";

    public static void main(String[] args) throws IOException, Exception {
        Persistencia cc = new Persistencia();
        String caminho = "C:/Users/HELM/Documents/NetBeansProjects/PetTop/PetTop/src/persistencia";
        cc.gerarArquivoEntidade(caminho, "exemplo", "exemplo", "public", "persistencia");
    }

    public void gerarArquivoEntidade(String caminho, String nomeEntidade, String tableName, String schemaName, String nomePakage) throws IOException, Exception {
        this.tableName = tableName;
        this.schemaName = schemaName;

        String persistencia = primeiraMaiuscula(retirarUnderline(1, nomeEntidade));
        File f = new File(caminho + "/" + persistencia + "DAO.java");
        f.createNewFile();

        FileWriter fileEntidade = null;
        BufferedWriter bufferEntidade = null;

        try {
            fileEntidade = new FileWriter(caminho + "/" + persistencia + "DAO.java", true);
            bufferEntidade = new BufferedWriter(fileEntidade);

            bufferEntidade.write(gerarPersistencia(persistencia, props.getColunas(tableName, schemaName), nomePakage));
        } catch (Exception e) {
            throw e;
        } finally {
            if (bufferEntidade != null) {
                bufferEntidade.close();
            }
        }

    }

    private String gerarPersistencia(String nomeEntidade, ArrayList<String> colunas, String nomePacote) throws SQLException {

        String conteudo = "package " + nomePacote + ";\n"
                + "\n"
                + "import entidade." + primeiraMaiuscula(nomeEntidade) + ";\n"
                + "import interfaces.CRUD;\n"
                + "import java.sql.Connection;\n"
                + "import java.sql.PreparedStatement;\n"
                + "import java.sql.ResultSet;\n"
                + "import java.sql.Statement;\n"
                + "import java.util.ArrayList;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Marcelo\n"
                + " */\n"
                + "public class ExemploDAO implements CRUD{\n"
                + gerarIncluir(nomeEntidade, colunas)
                + "\n}";

        return conteudo;
    }

    private String gerarIncluir(String nomeEntidade, ArrayList<String> colunas) throws SQLException {

        String atributos = "";
        String qtdAtribustos = "";
        String prepStat = "";
        String colunaId = "";
        String padraoTabela = (colunas.get(0).split("-")[1]).substring(0, 5);

        for (int i = 0; i < colunas.size(); i++) {
            String linha = colunas.get(i);
            String[] vetor = linha.split("-");
            String nomeMetodo = vetor[1].substring(0, 1).toUpperCase() + vetor[1].substring(1, vetor[1].length());

            if (i > 0) {
                if (i + 1 != colunas.size()) {
                    atributos += vetor[1] + ",";
                    qtdAtribustos += "?,";
                } else {
                    atributos += vetor[1];
                    qtdAtribustos += "?";
                }
                if (props.isForeignKey(vetor[1].replace(padraoTabela, ""), tableName)) {
                    nomeMetodo = primeiraMaiuscula(nomeMetodo.substring(10,nomeMetodo.length()));
                    String tableReference = props.getTabelaForeignKey(vetor[1].replace(padraoTabela, ""));
                    prepStat += "prd.set" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0]))) + 
                            "(" + i + ", obj.get"+primeiraMaiuscula(tableReference)+"().get" + nomeMetodo + "());\n";
                } else {
                    prepStat += "prd.set" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0]))) + "(" + i + ", obj.get" + retirarUnderline(2, nomeMetodo) + "());\n";
                }
            } else {
                colunaId = nomeMetodo;
            }

        }

        String chavePrimaria = props.getPrimaryKey(tableName, schemaName);

        String conteudo = "@Override\n"
                + "    public void incluir(Object objeto) throws Exception {\n"
                + "        " + nomeEntidade + " obj = (" + nomeEntidade + ")(objeto);\n"
                + "        \n"
                + "        String sql = \"insert into " + tableName + " (" + atributos + ") VALUES (" + qtdAtribustos + ");\";\n"
                + "\n"
                + "        Connection cnn = util.Conexao.getConexao();\n"
                + "\n"
                + "        PreparedStatement prd = cnn.prepareStatement(sql);\n"
                + "\n"
                + prepStat
                + "\n"
                + "\n"
                + "        prd.execute();\n"
                + "\n"
                + "        String sql2 = \"select currval('" + tableName + "_" + chavePrimaria + "_seq') as " + chavePrimaria + "\";\n"
                + "\n"
                + "        Statement stm = cnn.createStatement();\n"
                + "\n"
                + "        ResultSet rs = stm.executeQuery(sql2);\n"
                + "\n"
                + "        if (rs.next()) {\n"
                + "            int codigo = rs.getInt(\"" + chavePrimaria + "\");\n"
                + "            obj.set" + primeiraMaiuscula(colunaId.replace(padraoTabela, "")) + "(codigo);\n"
                + "        }\n"
                + "\n"
                + "        rs.close();\n"
                + "        cnn.close();\n"
                + "    }";
        return conteudo;

    }

    public String primeiraMaiuscula(String str) {
        str = str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
        return str;
    }

    public String getTipo(int tipo) {
        switch (tipo) {
            case 5:
                return "int";
            case 12:
                return "String";
            case 93:
                return "Date";
        }

        return null;
    }

    public String retirarUnderline(int tipo, String str) {
        String[] vetor = str.split("_");
        String retorno = "";

        if (vetor.length > 1) {
            if (tipo == 1) {
                for (int i = 0; i < vetor.length; i++) {
                    retorno += primeiraMaiuscula(vetor[i]);
                }
            } else if (tipo == 2) {
                retorno += vetor[0].toLowerCase();
                for (int i = 1; i < vetor.length; i++) {
                    retorno += primeiraMaiuscula(vetor[i]);
                }
            }

        } else {
            return str;
        }
        return retorno;
    }
}
