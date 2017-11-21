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

public class Entidade {

    Propriedades props = new Propriedades();
    String tableName = "";
    String schemaName = "";

    public static void main(String[] args) throws IOException, Exception {
        Entidade cc = new Entidade();
        String caminho = "C:/Users/HELM/Documents/NetBeansProjects/GeradorDeCodigo/GeradorDeCodigo/src/arquivos";
        cc.gerarArquivoEntidade(caminho, "cliente", "cliente", "public", "arquivos");
    }

    public void gerarArquivoEntidade(String caminho, String nomeEntidade, String tableName, String schemaName, String nomePakage) throws IOException, Exception {
        this.tableName = tableName;
        this.schemaName = schemaName;

        nomeEntidade = retirarUnderline(1, nomeEntidade);

        String entidade = nomeEntidade.substring(0, 1).toUpperCase() + nomeEntidade.substring(1, nomeEntidade.length());
        File f = new File(caminho + "/" + entidade + ".java");
        f.createNewFile();

        FileWriter fileEntidade = null;
        BufferedWriter bufferEntidade = null;

        try {
            fileEntidade = new FileWriter(caminho + "/" + entidade + ".java", true);
            bufferEntidade = new BufferedWriter(fileEntidade);

            bufferEntidade.write(gerarTextoEntidade(entidade, props.getColunas(tableName, schemaName), nomePakage));

        } catch (Exception e) {
            throw e;
        } finally {
            if (bufferEntidade != null) {
                bufferEntidade.close();
            }
        }

    }

    private String gerarTextoEntidade(String nomeEntidade, ArrayList<String> colunas, String nomePacote) throws SQLException {
        String atributos = "";
        String metodos = "";
        String padraoTabela = (colunas.get(0).split("-")[1]).substring(0, 5);
        String importar = "";

        for (int i = 0; i < colunas.size(); i++) {
            String linha = colunas.get(i);
            String[] vetor = linha.split("-");
            vetor[1] = vetor[1].replace(padraoTabela, "");

            String nomeMetodo = vetor[1].substring(0, 1).toUpperCase() + vetor[1].substring(1, vetor[1].length());
            String tipo = "";

            if (props.isForeignKey(vetor[1], tableName)) {
                String tableReference = props.getTabelaForeignKey(vetor[1]);
                atributos += "private " + primeiraMaiuscula(tableReference) + " " + tableReference + ";\n";
                tipo = primeiraMaiuscula(props.getTabelaForeignKey(vetor[1]));
                nomeMetodo = primeiraMaiuscula(tableReference);
                vetor[1] = tableReference;
            } else {
                vetor[1] = retirarUnderline(2, vetor[1]);
                atributos += "private " + getTipo(Integer.parseInt(vetor[0])) + " " + vetor[1] + ";\n";
                tipo = getTipo(Integer.parseInt(vetor[0]));
                if(tipo.equals("Date")) importar = "import java.util."+tipo+";\n";
            }

            //getter
            metodos += "public " + tipo + " get" + primeiraMaiuscula(retirarUnderline(2,nomeMetodo)) + "() {\n";
            metodos += "return " + vetor[1] + ";\n";
            metodos += "}\n\n";

            //setter
            metodos += "public void set" + primeiraMaiuscula(retirarUnderline(2,nomeMetodo)) + "(" + tipo + " " + vetor[1] + ") {\n";
            metodos += "this." + vetor[1] + " = " + vetor[1] + ";\n";
            metodos += "}\n\n";
        }

        String conteudo = "package " + nomePacote + ";\n"
                + "\n"
                + importar + "\n"
                + "public class " + nomeEntidade + " {\n"
                + "\n"
                + atributos
                + "\n"
                + metodos + "\n"
                + "}";
        return conteudo;
    }

    public String primeiraMaiuscula(String str) {
        str = str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
        return str;
    }

    public String getTipo(int tipo) {
        switch (tipo) {
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
