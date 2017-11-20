package geradordecodigo;

import classededados.Propriedades;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {
        Propriedades props = new Propriedades();
        ArrayList<String> listaSchemas = props.getSchemasNames();
        ArrayList<String> listaTabelas;
        ArrayList<String> listaColunas;

        for (int i = 0; i < listaSchemas.size(); i++) {
            System.out.println("----Schema----");
            System.out.println(listaSchemas.get(i));
            listaTabelas = props.getTabelas(listaSchemas.get(i));

            for (int j = 0; j < listaTabelas.size(); j++) {
                System.out.println("----Tabela----");
                System.out.println(listaTabelas.get(j));
                listaColunas = props.getColunas(listaTabelas.get(j), listaSchemas.get(i));

                for (int k = 0; k < listaColunas.size(); k++) {
                    System.out.println("----Coluna----");
                    System.out.println(listaColunas.get(k));
                }
            }
        }
    }

}
