/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teste;

import classededados.Propriedades;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Negocio {

    Propriedades props = new Propriedades();
    String tableName = "";
    String schemaName = "";
    String packageEntidade = "";
    String packagePersistencia = "";

    public static void main(String[] args) throws IOException, Exception {
        Negocio cc = new Negocio();
        String caminho = "C:/Users/HELM/Documents/NetBeansProjects/GeradorDeCodigo/GeradorDeCodigo/src/arquivos";
//        cc.gerarArquivoEntidade(caminho, "cliente", "cliente", "public", "arquivos");
    }

    public void gerarArquivoNegocio(String caminho, String nomeEntidade, String tableName, String schemaName, String nomePakage, String packageEntidade, String packagePersistencia) throws IOException, Exception {
        this.tableName = tableName;
        this.schemaName = schemaName;
        this.packageEntidade = packageEntidade;
        this.packagePersistencia = packagePersistencia;

        nomeEntidade = retirarUnderline(1, nomeEntidade);

        String entidade = nomeEntidade.substring(0, 1).toUpperCase() + nomeEntidade.substring(1, nomeEntidade.length());
        File f = new File(caminho + "/N" + entidade + ".java");
        f.createNewFile();

        FileWriter fileEntidade = null;
        BufferedWriter bufferEntidade = null;

        try {
            fileEntidade = new FileWriter(caminho + "/N" + entidade + ".java", true);
            bufferEntidade = new BufferedWriter(fileEntidade);

            bufferEntidade.write(gerarTextoNegocio(entidade, props.getColunas(tableName, schemaName), nomePakage));

        } catch (Exception e) {
            throw e;
        } finally {
            if (bufferEntidade != null) {
                bufferEntidade.close();
            }
        }

    }

    private String gerarTextoNegocio(String nomeEntidade, ArrayList<String> colunas, String nomePacote) throws SQLException {
        String chavePrimaria = props.getPrimaryKey(tableName, schemaName);
        String padraoTabela = (chavePrimaria).substring(0, 5);
        String conteudo = "package negocio;\n"
                + "\n"
                + "import "+packageEntidade+".*;\n"
                + "import "+packagePersistencia+".*;\n"
                + "import java.sql.SQLException;\n"
                + "import java.util.ArrayList;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Marcelo\n"
                + " */\n"
                + "public class N"+primeiraMaiuscula(nomeEntidade)+" {\n"
                + "\n"
                + "    "+primeiraMaiuscula(nomeEntidade)+"DAO dao;\n"
                + "\n"
                + "    public N"+primeiraMaiuscula(nomeEntidade)+"() {\n"
                + "        dao = new "+primeiraMaiuscula(nomeEntidade)+"DAO();\n"
                + "    }\n"
                + "\n"
                + "    public void salvar("+primeiraMaiuscula(nomeEntidade)+" obj) throws SQLException, Exception {\n"
                + "        if (obj.get"+primeiraMaiuscula(chavePrimaria.replace(padraoTabela, ""))+"() == 0) {\n"
                + "            dao.incluir(obj);\n"
                + "        } else {\n"
                + "            dao.alterar(obj);\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public void excluir(String codigo) throws SQLException, Exception {\n"
                + "        dao.excluir(codigo);\n"
                + "    }\n"
                + "\n"
                + "    public "+primeiraMaiuscula(nomeEntidade)+" consultar(String codigo) throws SQLException, Exception {\n"
                + "        return ("+primeiraMaiuscula(nomeEntidade)+") dao.consultar(codigo);\n"
                + "    }\n"
                + "\n"
                + "    public ArrayList<Object> listar() throws SQLException, Exception {\n"
                + "        return dao.listar();\n"
                + "    }\n"
                + "}\n"
                + "";
        return conteudo;
    }

    public String primeiraMaiuscula(String str) {
        str = str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
        return str;
    }

    public String getTipo(int tipo) {
        switch (tipo) {
            case 2:
                return "int";
            case 4:
                return "int";
            case 5:
                return "int";
            case 12:
                return "String";
            case 91:
                return "Date";
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
