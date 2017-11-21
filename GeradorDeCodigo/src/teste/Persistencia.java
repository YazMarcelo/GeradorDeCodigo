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
    String packageEntidade = "";
    String packageNegocio = "";

    public static void main(String[] args) throws IOException, Exception {
        Persistencia cc = new Persistencia();
        String caminho = "C:/Users/HELM/Documents/NetBeansProjects/GeradorDeCodigo/GeradorDeCodigo/src/arquivos";
//        cc.gerarArquivoEntidade(caminho, "animal", "animal", "public", "arquivos");
    }

    public void gerarArquivoPersistencia(String caminho, String nomeEntidade, String tableName, String schemaName, String nomePakage, String packageEntidade, String packageNegocio) throws IOException, Exception {
        this.tableName = tableName;
        this.schemaName = schemaName;
        this.packageEntidade = packageEntidade;
        this.packageNegocio = packageNegocio;

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
                + "import "+packageEntidade+".*;\n"
                + "import "+packageNegocio+".*;\n"
                + "import interfaces.CRUD;\n"
                + "import java.sql.*;\n"
                + "import java.util.ArrayList;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Marcelo\n"
                + " */\n"
                + "public class " + primeiraMaiuscula(nomeEntidade) + "DAO implements CRUD{\n"
                + gerarIncluir(nomeEntidade, colunas) + "\n"
                + gerarExcluir(nomeEntidade) + "\n"
                + gerarAlterar(nomeEntidade, colunas) + "\n"
                + gerarListar(nomeEntidade, colunas) + "\n"
                + gerarConsultar(nomeEntidade, colunas) + "\n"
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
            String nomeMetodo = vetor[1];

            if (i > 0) {
                if (i + 1 != colunas.size()) {
                    atributos += vetor[1] + ",";
                    qtdAtribustos += "?,";
                } else {
                    atributos += vetor[1];
                    qtdAtribustos += "?";
                }
                if (props.isForeignKey(vetor[1].replace(padraoTabela, ""), tableName)) {
                    nomeMetodo = primeiraMaiuscula(nomeMetodo.substring(10, nomeMetodo.length()));
                    String tableReference = props.getTabelaForeignKey(vetor[1].replace(padraoTabela, ""));
                    prepStat += "prd.set" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0])))
                            + "(" + i + ", obj.get" + primeiraMaiuscula(tableReference) + "().get" + nomeMetodo + "());\n";
                } else {
                    nomeMetodo = nomeMetodo.replace(padraoTabela, "");
                    prepStat += "prd.set" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0]))) + "(" + i + ",";
                    
                    if(getTipo(Integer.parseInt(vetor[0])).equals("Date")){
                        prepStat += " (Date) obj.get"+ primeiraMaiuscula(retirarUnderline(2, nomeMetodo)) + "());\n";
                    }else{
                        prepStat += " obj.get"+ primeiraMaiuscula(retirarUnderline(2, nomeMetodo)) + "());\n";
                    }
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

    private String gerarAlterar(String nomeEntidade, ArrayList<String> colunas) throws SQLException {

        String atributos = "\"\n+\"";
        String qtdAtribustos = "";
        String prepStat = "";
        String colunaId = "";
        String padraoTabela = (colunas.get(0).split("-")[1]).substring(0, 5);

        int i = 0;
        while (i < colunas.size()) {
            String linha = colunas.get(i);
            String[] vetor = linha.split("-");
            String nomeMetodo = vetor[1];

            if (i > 0) {
                if (i + 1 != colunas.size()) {
                    atributos += vetor[1] + " = ?,\"\n+\"";
                } else {
                    atributos += vetor[1] + " = ?\"\n+\"";
                }
                if (props.isForeignKey(vetor[1].replace(padraoTabela, ""), tableName)) {
                    nomeMetodo = primeiraMaiuscula(nomeMetodo.substring(10, nomeMetodo.length()));
                    String tableReference = props.getTabelaForeignKey(vetor[1].replace(padraoTabela, ""));
                    prepStat += "prd.set" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0])))
                            + "(" + i + ", obj.get" + primeiraMaiuscula(tableReference) + "().get" + nomeMetodo + "());\n";
                } else {
                    nomeMetodo = nomeMetodo.replace(padraoTabela, "");
                    prepStat += "prd.set" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0]))) + "(" + i + ",";
                    
                    if(getTipo(Integer.parseInt(vetor[0])).equals("Date")){
                        prepStat += " (Date) obj.get"+ primeiraMaiuscula(retirarUnderline(2, nomeMetodo)) + "());\n";
                    }else{
                        prepStat += " obj.get"+ primeiraMaiuscula(retirarUnderline(2, nomeMetodo)) + "());\n";
                    }
                }
            } else {
                colunaId = nomeMetodo;
            }
            i++;
        }

        String chavePrimaria = props.getPrimaryKey(tableName, schemaName);

        prepStat += "prd.setInt(" + i + ", obj.get" + primeiraMaiuscula(chavePrimaria.replace(padraoTabela, "")) + "());\n";

        String conteudo = "    @Override\n"
                + "    public void alterar(Object objeto) throws Exception {\n"
                + "        \n"
                + "        " + nomeEntidade + " obj = (" + nomeEntidade + ")(objeto);\n"
                + "        \n"
                + "        String sql = \"update " + schemaName + "." + tableName + " set " + atributos + " where " + chavePrimaria + " = ?;\";\n"
                + "\n"
                + "        Connection cnn = util.Conexao.getConexao();\n"
                + "\n"
                + "        PreparedStatement prd = cnn.prepareStatement(sql);\n"
                + "\n"
                + prepStat
                + "\n"
                + "        prd.execute();\n"
                + "\n"
                + "        prd.close();\n"
                + "        cnn.close(); \n"
                + "    }";
        return conteudo;
    }

    private String gerarListar(String nomeEntidade, ArrayList<String> colunas) throws SQLException {
        String atributos = "";
        String padraoTabela = (colunas.get(0).split("-")[1]).substring(0, 5);

        for (int i = 0; i < colunas.size(); i++) {
            String linha = colunas.get(i);
            String[] vetor = linha.split("-");
            String nomeMetodo = vetor[1];

            if (props.isForeignKey(vetor[1].replace(padraoTabela, ""), tableName)) {
                String tableReference = props.getTabelaForeignKey(vetor[1].replace(padraoTabela, ""));
                atributos += "objeto.set" + primeiraMaiuscula(tableReference) + "(new N" + primeiraMaiuscula(tableReference) + "().consultar"
                        + "(String.valueOf(rs.get" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0]))) + "(\"" + vetor[1] + "\"))));\n";
            } else {
                nomeMetodo = nomeMetodo.replace(padraoTabela, "");
                atributos += "objeto.set" + primeiraMaiuscula(retirarUnderline(2, nomeMetodo)) + "(rs.get" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0]))) + "(\"" + vetor[1] + "\"));\n";
            }

        }

        String chavePrimaria = props.getPrimaryKey(tableName, schemaName);

        String conteudo = "@Override\n"
                + "    public ArrayList<Object> listar() throws Exception {\n"
                + "        \n"
                + "        ArrayList<Object> listaObjs = new ArrayList<>();\n"
                + "\n"
                + "        String sql = \"select * from " + schemaName + "." + tableName + " order by " + chavePrimaria + "\";\n"
                + "\n"
                + "        Connection cnn = util.Conexao.getConexao();\n"
                + "        Statement stm = cnn.createStatement();\n"
                + "        ResultSet rs = stm.executeQuery(sql);\n"
                + "\n"
                + "        " + primeiraMaiuscula(nomeEntidade) + " objeto;\n"
                + "\n"
                + "        while (rs.next()) {\n"
                + "            objeto = new " + primeiraMaiuscula(nomeEntidade) + "();\n"
                + atributos
                + "            listaObjs.add(objeto);\n"
                + "        }\n"
                + "\n"
                + "        rs.close();\n"
                + "        cnn.close();\n"
                + "\n"
                + "        return listaObjs;\n"
                + "    }";
        return conteudo;
    }

    private String gerarConsultar(String nomeEntidade, ArrayList<String> colunas) throws SQLException {
        String atributos = "";
        String qtdAtribustos = "";
        String prepStat = "";
        String colunaId = "";
        String padraoTabela = (colunas.get(0).split("-")[1]).substring(0, 5);

        for (int i = 0; i < colunas.size(); i++) {
            String linha = colunas.get(i);
            String[] vetor = linha.split("-");
            String nomeMetodo = vetor[1];

            if (props.isForeignKey(vetor[1].replace(padraoTabela, ""), tableName)) {
                String tableReference = props.getTabelaForeignKey(vetor[1].replace(padraoTabela, ""));
                atributos += "objeto.set" + primeiraMaiuscula(tableReference) + "(new N" + primeiraMaiuscula(tableReference) + "().consultar"
                        + "(String.valueOf(rs.get" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0]))) + "(\"" + vetor[1] + "\"))));\n";
            } else {
                nomeMetodo = nomeMetodo.replace(padraoTabela, "");
                atributos += "objeto.set" + primeiraMaiuscula(retirarUnderline(2, nomeMetodo)) + "(rs.get" + primeiraMaiuscula(getTipo(Integer.parseInt(vetor[0]))) + "(\"" + vetor[1] + "\"));\n";
            }

        }

        String chavePrimaria = props.getPrimaryKey(tableName, schemaName);

        String conteudo = "@Override\n"
                + "    public Object consultar(String id) throws Exception {\n"
                + "        String sql = \"select * from " + schemaName + "." + tableName + " where " + chavePrimaria + " = ?;\";\n"
                + "\n"
                + "        Connection cnn = util.Conexao.getConexao();\n"
                + "\n"
                + "        PreparedStatement prd = cnn.prepareStatement(sql);\n"
                + "\n"
                + "        //Seta os valores para o procedimento\n"
                + "        prd.setInt(1, Integer.parseInt(id));\n"
                + "\n"
                + "        ResultSet rs = prd.executeQuery();\n"
                + "\n"
                + "        "+primeiraMaiuscula(nomeEntidade)+" objeto = new "+primeiraMaiuscula(nomeEntidade)+"();\n"
                + "\n"
                + "        if (rs.next()) {\n"
                + atributos
                + "        }\n"
                + "\n"
                + "        prd.execute();\n"
                + "\n"
                + "        prd.close();\n"
                + "        cnn.close();\n"
                + "\n"
                + "        return objeto;\n"
                + "    }";
        return conteudo;
    }

    private String gerarExcluir(String nomeEntidade) throws SQLException {
        String chavePrimaria = props.getPrimaryKey(tableName, schemaName);

        String conteudo = "    @Override\n"
                + "    public void excluir(String id) throws Exception {\n"
                + "        String sql = \"UPDATE " + schemaName + "." + tableName + " set excluido = true WHERE " + chavePrimaria + " = ?;\";\n"
                + "\n"
                + "        Connection cnn = util.Conexao.getConexao();\n"
                + "\n"
                + "        PreparedStatement prd = cnn.prepareStatement(sql);\n"
                + "\n"
                + "        prd.setInt(1, Integer.parseInt(id));\n"
                + "\n"
                + "        prd.execute();\n"
                + "\n"
                + "        prd.close();\n"
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
