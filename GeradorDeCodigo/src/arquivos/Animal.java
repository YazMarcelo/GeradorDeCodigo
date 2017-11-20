package arquivos;

public class Animal {

private int id;
private String nome;
private String rga;
private String raca;
private String porteAnimal;
private Especie especie;
private Cliente cliente;

public int getId() {
return id;
}

public void setId(int id) {
this.id = id;
}

public String getNome() {
return nome;
}

public void setNome(String nome) {
this.nome = nome;
}

public String getRga() {
return rga;
}

public void setRga(String rga) {
this.rga = rga;
}

public String getRaca() {
return raca;
}

public void setRaca(String raca) {
this.raca = raca;
}

public String getPorte_animal() {
return porteAnimal;
}

public void setPorte_animal(String porteAnimal) {
this.porteAnimal = porteAnimal;
}

public Especie getEspecie() {
return especie;
}

public void setEspecie(Especie especie) {
this.especie = especie;
}

public Cliente getCliente() {
return cliente;
}

public void setCliente(Cliente cliente) {
this.cliente = cliente;
}


}